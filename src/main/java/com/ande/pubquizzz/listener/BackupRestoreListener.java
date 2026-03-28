package com.ande.pubquizzz.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
@Component
public class BackupRestoreListener {

    private final DataSource dataSource;
    private final Path uploadDir;
    private final Path restoreDir;

    public BackupRestoreListener(
            DataSource dataSource,
            @Value("${app.upload.dir:/data/uploads}") String uploadDirPath,
            @Value("${app.backup.restore-dir:/data/pending-restore}") String restoreDirPath) {
        this.dataSource = dataSource;
        this.uploadDir = Paths.get(uploadDirPath);
        this.restoreDir = Paths.get(restoreDirPath);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        Path pendingSql = restoreDir.resolve("database.sql");
        if (!Files.exists(pendingSql)) {
            return;  // No pending restore — nothing to do
        }

        log.info("Pending restore detected at {}. Applying...", restoreDir);
        try {
            applyRestore(pendingSql);
            deleteDirectory(restoreDir);
            log.info("Restore applied successfully.");
        } catch (Exception e) {
            log.error("Restore FAILED — leaving {} in place for diagnosis. Application continues with old state.", restoreDir, e);
            // Do NOT rethrow — application must continue running
        }
    }

    private void applyRestore(Path sqlFile) throws Exception {
        String sqlPath = sqlFile.toAbsolutePath().toString().replace("\\", "/");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS DELETE FILES");
            stmt.execute("RUNSCRIPT FROM '" + sqlPath + "'");
        }

        // Replace uploads directory
        Path pendingUploads = restoreDir.resolve("uploads");
        if (Files.exists(uploadDir)) {
            deleteDirectory(uploadDir);
        }
        if (Files.exists(pendingUploads)) {
            Files.move(pendingUploads, uploadDir);
        } else {
            // Backup contained no images — create empty directory
            Files.createDirectories(uploadDir);
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.warn("Could not delete {}: {}", p, e.getMessage());
                        }
                    });
        }
    }
}
