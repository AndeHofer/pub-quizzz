package com.ande.pubquizzz.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ande.pubquizzz.service.QuizService;
import com.ande.pubquizzz.service.BackupService;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private final QuizService quizService;
    private final BackupService backupService;
    private final Path uploadDir;
    private final Path restoreDir;
    private final Path rollbackDir;

    public BackupRestoreListener(
            DataSource dataSource,
            QuizService quizService,
            BackupService backupService,
            @Value("${app.upload.dir:/data/uploads}") String uploadDirPath,
            @Value("${app.backup.restore-dir:/data/pending-restore}") String restoreDirPath) {
        this.dataSource = dataSource;
        this.quizService = quizService;
        this.backupService = backupService;
        this.uploadDir = Paths.get(uploadDirPath);
        this.restoreDir = Paths.get(restoreDirPath);
        this.rollbackDir = this.restoreDir.resolveSibling(this.restoreDir.getFileName() + "-rollback");
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        Path pendingSql = restoreDir.resolve("database.sql");
        if (!Files.exists(pendingSql)) {
            return;  // No pending restore — nothing to do
        }

        log.info("Pending restore detected at {}. Applying...", restoreDir);
        try {
            prepareRollbackSnapshot();
            applyRestore(pendingSql, restoreDir.resolve("uploads"));
            deleteDirectory(restoreDir);
            deleteDirectory(rollbackDir);
            log.info("Restore applied successfully.");
            var cleanup = quizService.cleanupOrphanedImages();
            log.info("Post-restore cleanup: {} orphaned image(s) removed", cleanup.getDeletedCount());
        } catch (Exception e) {
            log.error("Restore failed. Attempting rollback from {}.", rollbackDir, e);
            try {
                applyRollbackSnapshot();
                log.error("Rollback succeeded. Application continues with previous state.");
            } catch (Exception rollbackEx) {
                log.error("Rollback FAILED. Manual intervention required.", rollbackEx);
            }
            log.error("Restore FAILED — leaving {} in place for diagnosis. Application continues with old state.", restoreDir);
            // Do NOT rethrow — application must continue running
        }
    }

    private void applyRestore(Path sqlFile, Path pendingUploads) throws Exception {
        applyDatabaseRestore(sqlFile);
        replaceUploads(pendingUploads);
    }

    private void applyDatabaseRestore(Path sqlFile) throws Exception {
        String sqlPath = sqlFile.toAbsolutePath().toString().replace("\\", "/");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Drop only the backed-up tables, leaving appUser untouched
            // Disable referential integrity to tolerate future FK additions without order fragility.
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("DROP TABLE IF EXISTS result_answer");
            stmt.execute("DROP TABLE IF EXISTS result");
            stmt.execute("DROP TABLE IF EXISTS question_hints");
            stmt.execute("DROP TABLE IF EXISTS question");
            stmt.execute("DROP TABLE IF EXISTS quiz_document");
            stmt.execute("DROP TABLE IF EXISTS news");
            stmt.execute("DROP TABLE IF EXISTS app_usage_event");
            stmt.execute("DROP TABLE IF EXISTS quiz");
            stmt.execute("DROP TABLE IF EXISTS team");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            stmt.execute("RUNSCRIPT FROM '" + sqlPath + "'");
            ensureSchemaCompatibility(stmt);
        }
    }

    protected void replaceUploads(Path pendingUploads) throws Exception {
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

    private void prepareRollbackSnapshot() throws Exception {
        deleteDirectory(rollbackDir);
        Files.createDirectories(rollbackDir);

        Path rollbackZip = rollbackDir.resolve("rollback.zip");
        try (OutputStream out = Files.newOutputStream(rollbackZip);
             java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(out)) {
            backupService.createBackup(zip);
        }
    }

    private void applyRollbackSnapshot() throws Exception {
        Path rollbackZip = rollbackDir.resolve("rollback.zip");
        Path rollbackStageDir = rollbackDir.resolve("staged");
        deleteDirectory(rollbackStageDir);
        Files.createDirectories(rollbackStageDir);

        try (InputStream in = Files.newInputStream(rollbackZip)) {
            backupService.stageRestoreToDirectory(in, rollbackStageDir);
        }

        applyRestore(rollbackStageDir.resolve("database.sql"), rollbackStageDir.resolve("uploads"));
    }

    private void ensureSchemaCompatibility(Statement stmt) throws Exception {
        // Backward-compatibility for restores from backups created before Phase 28/27 schema additions.
        stmt.execute("ALTER TABLE question ADD COLUMN IF NOT EXISTS answer_image_url VARCHAR(255)");
        stmt.execute("""
                CREATE TABLE IF NOT EXISTS quiz_document (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    quiz_id BIGINT NOT NULL,
                    original_filename VARCHAR(255) NOT NULL,
                    stored_filename VARCHAR(255) NOT NULL UNIQUE,
                    content_type VARCHAR(255) NOT NULL,
                    file_size BIGINT NOT NULL,
                    uploaded_at TIMESTAMP NOT NULL,
                    CONSTRAINT fk_quiz_document_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id)
                )
                """);
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
