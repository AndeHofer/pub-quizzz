package com.ande.pubquizzz.service;

import com.ande.pubquizzz.exception.BusinessValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class BackupService {

    private static final int BUFFER_SIZE = 8192;

    private final DataSource dataSource;
    private final Path uploadDir;
    private final Path restoreDir;
    private final long maxEntries;
    private final long maxEntrySizeBytes;
    private final long maxTotalUncompressedSizeBytes;

    public BackupService(
            DataSource dataSource,
            @Value("${app.upload.dir:/data/uploads}") String uploadDirPath,
            @Value("${app.backup.restore-dir:/data/pending-restore}") String restoreDirPath,
            @Value("${app.backup.import.max-entries:5000}") long maxEntries,
            @Value("${app.backup.import.max-entry-size-bytes:52428800}") long maxEntrySizeBytes,
            @Value("${app.backup.import.max-total-uncompressed-size-bytes:524288000}") long maxTotalUncompressedSizeBytes) {
        this.dataSource = dataSource;
        this.uploadDir = Paths.get(uploadDirPath);
        this.restoreDir = Paths.get(restoreDirPath);
        this.maxEntries = maxEntries;
        this.maxEntrySizeBytes = maxEntrySizeBytes;
        this.maxTotalUncompressedSizeBytes = maxTotalUncompressedSizeBytes;
    }

    public void createBackup(ZipOutputStream zip) throws IOException {
        Path tempSql = Files.createTempFile("pubquizzz-backup-", ".sql");
        try {
            // Dump H2 database to temp SQL file
            String sqlPath = tempSql.toAbsolutePath().toString().replace("\\", "/");
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SCRIPT TO '" + sqlPath + "' TABLE quiz, question, question_hints, team, result, result_answer, quiz_document");
            } catch (Exception e) {
                throw new RuntimeException("Failed to dump H2 database: " + e.getMessage(), e);
            }

            // Add database.sql to ZIP
            zip.putNextEntry(new ZipEntry("database.sql"));
            Files.copy(tempSql, zip);
            zip.closeEntry();

            // Add uploaded images to ZIP
            if (Files.exists(uploadDir)) {
                try (var stream = Files.walk(uploadDir)) {
                    for (Path file : stream.filter(Files::isRegularFile).toList()) {
                        zip.putNextEntry(new ZipEntry("uploads/" + uploadDir.relativize(file)));
                        Files.copy(file, zip);
                        zip.closeEntry();
                    }
                }
            }

            log.info("Backup created successfully with database dump and uploads");
        } finally {
            Files.deleteIfExists(tempSql);
        }
    }

    public void stageRestore(InputStream zipInputStream) throws IOException {
        Files.createDirectories(restoreDir);
        boolean hasDatabaseSql = false;
        long entryCount = 0;
        long totalBytes = 0;
        try {
            try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(zipInputStream))) {
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    entryCount++;
                    if (entryCount > maxEntries) {
                        throw new BusinessValidationException("Ungültiges Backup: zu viele Dateien im Archiv.");
                    }
                    if ("database.sql".equals(entry.getName())) {
                        hasDatabaseSql = true;
                    }
                    Path target = restoreDir.resolve(entry.getName()).normalize();
                    if (!target.startsWith(restoreDir)) {
                        log.warn("Skipping zip-slip attempt: {}", entry.getName());
                        continue;
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        try (OutputStream out = Files.newOutputStream(target)) {
                            totalBytes = copyBounded(zin, out, totalBytes);
                        }
                    }
                    zin.closeEntry();
                }
            }
            if (!hasDatabaseSql) {
                log.warn("Restore ZIP is missing database.sql — aborting staging");
                throw new BusinessValidationException("Ungültiges Backup: database.sql nicht gefunden.");
            }
        } catch (BusinessValidationException e) {
            deleteDirectoryQuietly(restoreDir);
            throw e;
        } catch (IOException e) {
            deleteDirectoryQuietly(restoreDir);
            throw e;
        }
        log.info("Restore staged to {}", restoreDir);
    }

    private long copyBounded(InputStream in, OutputStream out, long currentTotalBytes) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long entryBytes = 0;
        long totalBytes = currentTotalBytes;
        int read;
        while ((read = in.read(buffer)) != -1) {
            entryBytes += read;
            totalBytes += read;
            if (entryBytes > maxEntrySizeBytes) {
                throw new BusinessValidationException("Ungültiges Backup: Datei im Archiv ist zu groß.");
            }
            if (totalBytes > maxTotalUncompressedSizeBytes) {
                throw new BusinessValidationException("Ungültiges Backup: Archiv ist insgesamt zu groß.");
            }
            out.write(buffer, 0, read);
        }
        return totalBytes;
    }

    private void deleteDirectoryQuietly(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ex) {
                            log.warn("Could not delete {}: {}", p, ex.getMessage());
                        }
                    });
        } catch (IOException ex) {
            log.warn("Could not clean up restoreDir {}: {}", dir, ex.getMessage());
        }
    }
}
