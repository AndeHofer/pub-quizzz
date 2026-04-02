package com.ande.pubquizzz.service;

import com.ande.pubquizzz.dto.CleanupResult;
import com.ande.pubquizzz.listener.BackupRestoreListener;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
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
        }
        Files.createDirectories(tempDir.resolve("uploads"));
        mockQuizService = mock(QuizService.class);
        when(mockQuizService.cleanupOrphanedImages()).thenReturn(new CleanupResult(0, List.of()));
    }

    private BackupRestoreListener listener() {
        return new BackupRestoreListener(
                sharedDs,
                mockQuizService,
                tempDir.resolve("uploads").toString(),
                tempDir.resolve("restore").toString());
    }

    private BackupService backupService() {
        return new BackupService(
                sharedDs,
                tempDir.resolve("uploads").toString(),
                tempDir.resolve("restore").toString());
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
        svc.stageRestore(baos.toByteArray());

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
        svc.stageRestore(baos.toByteArray());

        listener().onApplicationStarted();

        verify(mockQuizService, times(1)).cleanupOrphanedImages();
    }

    @Test
    void noPendingRestore_doesNotCallCleanup() {
        listener().onApplicationStarted();

        verify(mockQuizService, never()).cleanupOrphanedImages();
    }
}
