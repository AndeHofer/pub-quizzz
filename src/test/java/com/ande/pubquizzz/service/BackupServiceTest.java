package com.ande.pubquizzz.service;

import com.ande.pubquizzz.exception.BusinessValidationException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class BackupServiceTest {

    @TempDir
    Path tempDir;

    private JdbcDataSource sharedDs;

    @BeforeEach
    void setUp() throws Exception {
        sharedDs = new JdbcDataSource();
        sharedDs.setURL("jdbc:h2:file:" + tempDir.resolve("testdb").toAbsolutePath() + ";AUTO_SERVER=FALSE");
        sharedDs.setUser("sa");
        sharedDs.setPassword("");
        // Materialise the H2 file
        try (var conn = sharedDs.getConnection()) { /* no-op */ }
        Files.createDirectories(tempDir.resolve("uploads"));
    }

    private BackupService service() {
        return new BackupService(sharedDs, tempDir.resolve("uploads").toString(), tempDir.resolve("restore").toString());
    }

    private Map<String, byte[]> readZipEntries(byte[] zipBytes) throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                entries.put(entry.getName(), zin.readAllBytes());
                zin.closeEntry();
            }
        }
        return entries;
    }

    @Test
    void createBackup_producesZipWithDatabaseSqlEntry() throws Exception {
        BackupService svc = service();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            svc.createBackup(zip);
        }

        Map<String, byte[]> entries = readZipEntries(baos.toByteArray());
        assertTrue(entries.containsKey("database.sql"), "ZIP should contain an entry named 'database.sql'");
    }

    @Test
    void createBackup_sqlDumpContainsDdl() throws Exception {
        BackupService svc = service();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            svc.createBackup(zip);
        }

        Map<String, byte[]> entries = readZipEntries(baos.toByteArray());
        byte[] sqlBytes = entries.get("database.sql");
        assertNotNull(sqlBytes, "database.sql entry should exist");
        String sqlContent = new String(sqlBytes, StandardCharsets.UTF_8);
        assertFalse(sqlContent.isBlank(), "database.sql must not be empty");
    }

    @Test
    void createBackup_includesUploadedImages() throws Exception {
        Path uploadsDir = tempDir.resolve("uploads");
        Files.write(uploadsDir.resolve("photo1.jpg"), "img1".getBytes(StandardCharsets.UTF_8));
        Files.write(uploadsDir.resolve("photo2.jpg"), "img2".getBytes(StandardCharsets.UTF_8));

        BackupService svc = service();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            svc.createBackup(zip);
        }

        Map<String, byte[]> entries = readZipEntries(baos.toByteArray());
        long uploadEntryCount = entries.keySet().stream().filter(k -> k.startsWith("uploads/")).count();
        assertEquals(2, uploadEntryCount, "ZIP should contain exactly 2 entries under 'uploads/'");
    }

    @Test
    void stageRestore_withValidZip_extractsToRestoreDir() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            zip.putNextEntry(new ZipEntry("database.sql"));
            zip.write("-- H2 script".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        service().stageRestore(baos.toByteArray());

        Path restoredFile = tempDir.resolve("restore").resolve("database.sql");
        assertTrue(Files.exists(restoredFile), "restoreDir/database.sql should exist after stageRestore");
        String content = Files.readString(restoredFile);
        assertEquals("-- H2 script", content);
    }

    @Test
    void stageRestore_withMissingDatabaseSql_throwsValidationException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            zip.putNextEntry(new ZipEntry("uploads/photo.jpg"));
            zip.write("imgdata".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        assertThrows(BusinessValidationException.class, () -> service().stageRestore(baos.toByteArray()),
                "Should throw BusinessValidationException when database.sql is missing from ZIP");
    }

    @Test
    void stageRestore_withZipSlipEntry_doesNotWriteOutsideRestoreDir() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ZipOutputStream zout = new ZipOutputStream(buf)) {
            zout.putNextEntry(new ZipEntry("database.sql"));
            zout.write("-- H2 script".getBytes(StandardCharsets.UTF_8));
            zout.closeEntry();
            // Zip-slip attempt
            zout.putNextEntry(new ZipEntry("../../evil.txt"));
            zout.write("evil".getBytes(StandardCharsets.UTF_8));
            zout.closeEntry();
        }

        service().stageRestore(buf.toByteArray());

        // The evil file must NOT exist outside restoreDir
        assertFalse(Files.exists(tempDir.resolve("evil.txt")),
                "Zip-slip file must not be written outside restoreDir");
    }
}
