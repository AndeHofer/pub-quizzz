package com.ande.pubquizzz.service;

import com.ande.pubquizzz.dto.CleanupResult;
import com.ande.pubquizzz.listener.BackupRestoreListener;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BackupRestoreListenerTest {

    @TempDir
    Path tempDir;

    private JdbcDataSource sharedDs;
    private QuizService mockQuizService;

    @BeforeEach
    void setUp() throws Exception {
        sharedDs = new JdbcDataSource();
        sharedDs.setURL("jdbc:h2:file:" + tempDir.resolve("testdb").toAbsolutePath() + ";AUTO_SERVER=FALSE");
        sharedDs.setUser("sa");
        sharedDs.setPassword("");
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz (id BIGINT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS question (id BIGINT PRIMARY KEY, quiz_id BIGINT, FOREIGN KEY (quiz_id) REFERENCES quiz(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS question_hints (question_id BIGINT, hint_text VARCHAR(1024))");
            stmt.execute("CREATE TABLE IF NOT EXISTS team (id BIGINT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS result (id BIGINT PRIMARY KEY, quiz_id BIGINT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS result_answer (id BIGINT PRIMARY KEY, result_id BIGINT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz_document (id BIGINT PRIMARY KEY, quiz_id BIGINT NOT NULL, FOREIGN KEY (quiz_id) REFERENCES quiz(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS news (news_id BIGINT PRIMARY KEY, title VARCHAR(200) NOT NULL, text VARCHAR(5000) NOT NULL, created_at TIMESTAMP NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS app_usage_event (usage_event_id BIGINT PRIMARY KEY, event_type VARCHAR(64) NOT NULL, username VARCHAR(255) NOT NULL, occurred_at TIMESTAMP NOT NULL, entity_type VARCHAR(64), entity_id VARCHAR(128), metadata_json CLOB)");
        }
        Files.createDirectories(tempDir.resolve("uploads"));
        mockQuizService = mock(QuizService.class);
        when(mockQuizService.cleanupOrphanedImages()).thenReturn(new CleanupResult(0, List.of()));
    }

    private BackupRestoreListener listener() {
        return new BackupRestoreListener(
                sharedDs,
                mockQuizService,
                backupService(),
                tempDir.resolve("uploads").toString(),
                tempDir.resolve("restore").toString());
    }

    private BackupService backupService() {
        return new BackupService(
                sharedDs,
                tempDir.resolve("uploads").toString(),
                tempDir.resolve("restore").toString(),
                5000,
                50L * 1024 * 1024,
                500L * 1024 * 1024);
    }

    @Test
    void applyRestore_preservesAppUserRows() throws Exception {
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS appUser (id BIGINT PRIMARY KEY, username VARCHAR(255))");
            stmt.execute("INSERT INTO appUser VALUES (1, 'admin')");
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz (id BIGINT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("INSERT INTO quiz VALUES (100, 'Test Quiz')");
        }

        BackupService svc = backupService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            svc.createBackup(zip);
        }
        svc.stageRestore(new ByteArrayInputStream(baos.toByteArray()));

        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM quiz");
        }

        listener().onApplicationStarted();

        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM appUser WHERE username = 'admin'");
            rs.next();
            assertEquals(1, rs.getInt(1), "appUser row must survive a restore");
        }
    }

    @Test
    void applyRestore_callsCleanupAfterRestore() throws Exception {
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS appUser (id BIGINT PRIMARY KEY, username VARCHAR(255))");
        }

        BackupService svc = backupService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            svc.createBackup(zip);
        }
        svc.stageRestore(new ByteArrayInputStream(baos.toByteArray()));

        listener().onApplicationStarted();

        verify(mockQuizService, times(1)).cleanupOrphanedImages();
    }

    @Test
    void noPendingRestore_doesNotCallCleanup() {
        listener().onApplicationStarted();

        verify(mockQuizService, never()).cleanupOrphanedImages();
    }

    @Test
    void applyRestore_succeedsWhenQuizDocumentDependsOnQuiz() throws Exception {
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO quiz VALUES (100, 'Test Quiz')");
            stmt.execute("INSERT INTO quiz_document VALUES (200, 100)");
        }

        BackupService svc = backupService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            svc.createBackup(zip);
        }
        svc.stageRestore(new ByteArrayInputStream(baos.toByteArray()));

        // Mutate current DB so we can prove restore re-applies backup state.
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM quiz_document");
            stmt.execute("DELETE FROM quiz");
            stmt.execute("INSERT INTO quiz VALUES (999, 'Mutated')");
        }

        listener().onApplicationStarted();

        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            ResultSet quizCount = stmt.executeQuery("SELECT COUNT(*) FROM quiz WHERE id = 100");
            quizCount.next();
            assertEquals(1, quizCount.getInt(1), "Quiz row from backup should be restored");

            ResultSet docCount = stmt.executeQuery("SELECT COUNT(*) FROM quiz_document WHERE id = 200 AND quiz_id = 100");
            docCount.next();
            assertEquals(1, docCount.getInt(1), "quiz_document row should be restored without FK-drop failure");
        }
    }

    @Test
    void applyRestore_fromLegacyBackup_addsMissingColumnsAndTables() throws Exception {
        String legacySql = """
                CREATE TABLE quiz (
                    quiz_id BIGINT PRIMARY KEY,
                    pub_date DATE NOT NULL,
                    submit_date DATE NOT NULL,
                    title VARCHAR(255)
                );
                CREATE TABLE question (
                    quiz_id BIGINT NOT NULL,
                    question_number INTEGER NOT NULL,
                    question VARCHAR(255) NOT NULL,
                    answer VARCHAR(255) NOT NULL,
                    note VARCHAR(255),
                    PRIMARY KEY (quiz_id, question_number),
                    FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id)
                );
                CREATE TABLE question_hints (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    quiz_id BIGINT,
                    question_number INTEGER,
                    hint_order INTEGER,
                    hint_text VARCHAR(1024),
                    image_url_at_start VARCHAR(255),
                    image_url_as_hint VARCHAR(255)
                );
                CREATE TABLE team (
                    team_id BIGINT PRIMARY KEY,
                    team_name VARCHAR(255)
                );
                CREATE TABLE result (
                    results_id BIGINT PRIMARY KEY,
                    team_id BIGINT,
                    quiz_id BIGINT
                );
                CREATE TABLE result_answer (
                    id BIGINT PRIMARY KEY,
                    result_id BIGINT,
                    question_number INTEGER,
                    points INTEGER,
                    changed BOOLEAN
                );
                """;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            zip.putNextEntry(new java.util.zip.ZipEntry("database.sql"));
            zip.write(legacySql.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        backupService().stageRestore(new ByteArrayInputStream(baos.toByteArray()));
        listener().onApplicationStarted();

        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            ResultSet answerImageCol = stmt.executeQuery("""
                    SELECT COUNT(*)
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_NAME = 'QUESTION' AND COLUMN_NAME = 'ANSWER_IMAGE_URL'
                    """);
            answerImageCol.next();
            assertEquals(1, answerImageCol.getInt(1), "Legacy restore must add question.answer_image_url column");

            ResultSet quizDocumentTable = stmt.executeQuery("""
                    SELECT COUNT(*)
                    FROM INFORMATION_SCHEMA.TABLES
                    WHERE TABLE_NAME = 'QUIZ_DOCUMENT'
                    """);
            quizDocumentTable.next();
            assertEquals(1, quizDocumentTable.getInt(1), "Legacy restore must create quiz_document table");
        }
    }

    @Test
    void applyRestore_whenRunscriptFails_rollsBackToPreviousDatabaseState() throws Exception {
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS appUser (id BIGINT PRIMARY KEY, username VARCHAR(255))");
            stmt.execute("INSERT INTO quiz VALUES (100, 'Original Quiz')");
        }

        String brokenSql = "INSERT INTO quiz VALUES ('broken', 'Broken Quiz');";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            zip.putNextEntry(new java.util.zip.ZipEntry("database.sql"));
            zip.write(brokenSql.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        backupService().stageRestore(new ByteArrayInputStream(baos.toByteArray()));
        listener().onApplicationStarted();

        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            ResultSet originalQuizCount = stmt.executeQuery("SELECT COUNT(*) FROM quiz WHERE id = 100");
            originalQuizCount.next();
            assertEquals(1, originalQuizCount.getInt(1),
                    "Original live quiz row must remain after failed restore rollback");
        }
    }

    @Test
    void applyRestore_whenUploadSwapFails_rollsBackPreviousUploadsAndDatabase() throws Exception {
        Files.writeString(tempDir.resolve("uploads").resolve("original.txt"), "original");
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO quiz VALUES (100, 'Original Quiz')");
        }

        String validSql = "INSERT INTO quiz VALUES (200, 'Restored Quiz');";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            zip.putNextEntry(new java.util.zip.ZipEntry("database.sql"));
            zip.write(validSql.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new java.util.zip.ZipEntry("uploads/restored.txt"));
            zip.write("restored".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        backupService().stageRestore(new ByteArrayInputStream(baos.toByteArray()));

        BackupRestoreListener failingUploadSwapListener = new BackupRestoreListener(
                sharedDs,
                mockQuizService,
                backupService(),
                tempDir.resolve("uploads").toString(),
                tempDir.resolve("restore").toString()) {
            @Override
            protected void replaceUploads(Path pendingUploads) throws Exception {
                throw new IOException("simulated upload swap failure");
            }
        };

        failingUploadSwapListener.onApplicationStarted();

        assertTrue(Files.exists(tempDir.resolve("uploads").resolve("original.txt")),
                "Original upload should be restored after rollback");
        assertFalse(Files.exists(tempDir.resolve("uploads").resolve("restored.txt")),
                "Restored upload should not remain after rollback");

        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            ResultSet originalQuizCount = stmt.executeQuery("SELECT COUNT(*) FROM quiz WHERE id = 100");
            originalQuizCount.next();
            assertEquals(1, originalQuizCount.getInt(1),
                    "Original database rows should be restored after rollback");
        }
    }

    @Test
    void applyRestore_restoresNewsAndUsageEventsFromBackup() throws Exception {
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO news VALUES (1, 'Original News', 'Original Text', TIMESTAMP '2026-06-01 10:00:00')");
            stmt.execute("INSERT INTO app_usage_event VALUES (1, 'AUTH_SUCCESS', 'alice', TIMESTAMP '2026-06-01 10:05:00', NULL, NULL, NULL)");
        }

        BackupService svc = backupService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            svc.createBackup(zip);
        }
        svc.stageRestore(new ByteArrayInputStream(baos.toByteArray()));

        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM news");
            stmt.execute("DELETE FROM app_usage_event");
            stmt.execute("INSERT INTO news VALUES (2, 'Mutated News', 'Mutated Text', TIMESTAMP '2026-06-02 10:00:00')");
            stmt.execute("INSERT INTO app_usage_event VALUES (2, 'AUTH_SUCCESS', 'bob', TIMESTAMP '2026-06-02 10:05:00', NULL, NULL, NULL)");
        }

        listener().onApplicationStarted();

        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            ResultSet originalNews = stmt.executeQuery("SELECT COUNT(*) FROM news WHERE news_id = 1");
            originalNews.next();
            assertEquals(1, originalNews.getInt(1), "Original news row from backup should be restored");

            ResultSet mutatedNews = stmt.executeQuery("SELECT COUNT(*) FROM news WHERE news_id = 2");
            mutatedNews.next();
            assertEquals(0, mutatedNews.getInt(1), "Mutated news row should not remain after restore");

            ResultSet originalUsageEvent = stmt.executeQuery("SELECT COUNT(*) FROM app_usage_event WHERE usage_event_id = 1");
            originalUsageEvent.next();
            assertEquals(1, originalUsageEvent.getInt(1), "Original usage event row from backup should be restored");

            ResultSet mutatedUsageEvent = stmt.executeQuery("SELECT COUNT(*) FROM app_usage_event WHERE usage_event_id = 2");
            mutatedUsageEvent.next();
            assertEquals(0, mutatedUsageEvent.getInt(1), "Mutated usage event row should not remain after restore");
        }
    }
}
