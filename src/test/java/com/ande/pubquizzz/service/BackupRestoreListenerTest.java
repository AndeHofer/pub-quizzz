package com.ande.pubquizzz.service;

import com.ande.pubquizzz.listener.BackupRestoreListener;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class BackupRestoreListenerTest {

    @TempDir
    Path tempDir;

    private JdbcDataSource sharedDs;

    @BeforeEach
    void setUp() throws Exception {
        sharedDs = new JdbcDataSource();
        sharedDs.setURL("jdbc:h2:file:" + tempDir.resolve("testdb").toAbsolutePath() + ";AUTO_SERVER=FALSE");
        sharedDs.setUser("sa");
        sharedDs.setPassword("");
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            // Create the tables referenced in SCRIPT TO ... TABLE ... (required before backup can run)
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz (id BIGINT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS question (id BIGINT PRIMARY KEY, quiz_id BIGINT, FOREIGN KEY (quiz_id) REFERENCES quiz(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS question_hints (question_id BIGINT, hint_text VARCHAR(1024))");
            stmt.execute("CREATE TABLE IF NOT EXISTS team (id BIGINT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS result (id BIGINT PRIMARY KEY, quiz_id BIGINT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS result_answer (id BIGINT PRIMARY KEY, result_id BIGINT)");
        }
        Files.createDirectories(tempDir.resolve("uploads"));
    }

    private BackupRestoreListener listener() {
        return new BackupRestoreListener(
                sharedDs,
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
        // Arrange: create appUser table with a row, and a quiz table with a row
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS appUser (id BIGINT PRIMARY KEY, username VARCHAR(255))");
            stmt.execute("INSERT INTO appUser VALUES (1, 'admin')");
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz (id BIGINT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("INSERT INTO quiz VALUES (100, 'Test Quiz')");
        }

        // Act: create a backup ZIP (excludes appUser), stage it, then trigger restore
        BackupService svc = backupService();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            svc.createBackup(zip);
        }
        svc.stageRestore(baos.toByteArray());

        // Wipe the quiz table row to simulate a "dirty" state before restore
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM quiz");
        }

        listener().onApplicationStarted();

        // Assert: appUser row must still exist
        try (var conn = sharedDs.getConnection();
             var stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM appUser WHERE username = 'admin'");
            rs.next();
            assertEquals(1, rs.getInt(1), "appUser row must survive a restore");
        }
    }
}
