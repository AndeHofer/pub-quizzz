package com.ande.pubquizzz.service;

import com.ande.pubquizzz.dto.AdminLogResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminLogServiceTest {

    private static final Path TEST_LOG_PATH = Path.of("target", "test-logs", "admin-log-service.log");

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(TEST_LOG_PATH);
    }

    @Test
    void parsesAndReturnsNewestFirst() throws IOException {
        writeLines(List.of(
                "2026-05-28 10:00:00 INFO  A.a:1 - Erste",
                "2026-05-28 10:01:00 WARN  B.b:2 - Zweite"
        ));

        AdminLogService service = new AdminLogService(TEST_LOG_PATH.toString());
        AdminLogResponseDTO response = service.getLogs(null, null, null, null, null);

        assertEquals(2, response.getReturnedCount());
        assertEquals(200, response.getAppliedLimit());
        assertEquals("WARN", response.getEntries().get(0).getLevel());
        assertEquals("INFO", response.getEntries().get(1).getLevel());
    }

    @Test
    void keepsUnparseableLineAsUnknown() throws IOException {
        writeLines(List.of("not-a-parseable-log-line"));

        AdminLogService service = new AdminLogService(TEST_LOG_PATH.toString());
        AdminLogResponseDTO response = service.getLogs(null, null, null, null, null);

        assertEquals(1, response.getReturnedCount());
        assertEquals("UNKNOWN", response.getEntries().get(0).getLevel());
        assertNull(response.getEntries().get(0).getTimestamp());
        assertEquals("not-a-parseable-log-line", response.getEntries().get(0).getRawLine());
    }

    @Test
    void filtersByLevelAndWordCaseInsensitive() throws IOException {
        writeLines(List.of(
                "2026-05-28 10:00:00 INFO  A.a:1 - Server gestartet",
                "2026-05-28 10:01:00 ERROR B.b:2 - FATAL Fehler",
                "2026-05-28 10:02:00 ERROR C.c:3 - anderer fehler"
        ));

        AdminLogService service = new AdminLogService(TEST_LOG_PATH.toString());
        AdminLogResponseDTO response = service.getLogs("fatal", "error", null, null, null);

        assertEquals(1, response.getReturnedCount());
        assertEquals("ERROR", response.getEntries().get(0).getLevel());
        assertTrue(response.getEntries().get(0).getRawLine().contains("FATAL"));
    }

    @Test
    void filtersByTimeRange() throws IOException {
        writeLines(List.of(
                "2026-05-27 09:59:59 INFO  A.a:1 - Vorher",
                "2026-05-27 10:00:00 INFO  B.b:2 - Innerhalb",
                "2026-05-27 10:30:00 INFO  C.c:3 - Auch innerhalb",
                "2026-05-27 11:00:01 INFO  D.d:4 - Nachher"
        ));

        AdminLogService service = new AdminLogService(TEST_LOG_PATH.toString());
        AdminLogResponseDTO response = service.getLogs(
                null,
                null,
                "2026-05-27T10:00:00",
                "2026-05-27T11:00:00",
                null
        );

        assertEquals(2, response.getReturnedCount());
        assertEquals("2026-05-27 10:30:00", response.getEntries().get(0).getTimestamp());
        assertEquals("2026-05-27 10:00:00", response.getEntries().get(1).getTimestamp());
    }

    @Test
    void appliesMaxLimitWhenRequestedLimitIsTooHigh() throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 1500; i++) {
            lines.add("2026-05-28 10:00:00 INFO  A.a:1 - Line " + i);
        }
        writeLines(lines);

        AdminLogService service = new AdminLogService(TEST_LOG_PATH.toString());
        AdminLogResponseDTO response = service.getLogs(null, null, null, null, 5000);

        assertEquals(1000, response.getAppliedLimit());
        assertEquals(1000, response.getReturnedCount());
    }

    @Test
    void rejectsInvalidFiltersAndLimit() throws IOException {
        writeLines(List.of("2026-05-28 10:00:00 INFO  A.a:1 - Start"));

        AdminLogService service = new AdminLogService(TEST_LOG_PATH.toString());

        assertThrows(IllegalArgumentException.class, () -> service.getLogs(null, "verbose", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.getLogs(null, null, "bad-time", null, null));
        assertThrows(IllegalArgumentException.class, () -> service.getLogs(null, null, "2026-05-28T11:00:00", "2026-05-28T10:00:00", null));
        assertThrows(IllegalArgumentException.class, () -> service.getLogs(null, null, null, null, 0));
    }

    @Test
    void groupsMultilineStacktraceIntoSingleEvent() throws IOException {
        writeLines(List.of(
                "2026-05-29 13:41:00 ERROR GlobalExceptionHandler.handleGenericException:70 - Unexpected error",
                "java.lang.RuntimeException: boom",
                "\tat com.ande.pubquizzz.service.SomeService.run(SomeService.java:42)",
                "Caused by: java.lang.IllegalStateException: bad state"
        ));

        AdminLogService service = new AdminLogService(TEST_LOG_PATH.toString());
        AdminLogResponseDTO response = service.getLogs(null, "ERROR", null, null, null);

        assertEquals(1, response.getReturnedCount());
        assertEquals("ERROR", response.getEntries().get(0).getLevel());
        assertTrue(response.getEntries().get(0).getRawLine().contains("RuntimeException: boom"));
        assertTrue(response.getEntries().get(0).getRawLine().contains("Caused by:"));
    }

    @Test
    void canSearchInsideStacktraceLines() throws IOException {
        writeLines(List.of(
                "2026-05-29 13:41:00 ERROR GlobalExceptionHandler.handleGenericException:70 - Unexpected error",
                "java.lang.RuntimeException: boom",
                "\tat com.ande.pubquizzz.service.SomeService.run(SomeService.java:42)",
                "Caused by: java.lang.IllegalStateException: bad state"
        ));

        AdminLogService service = new AdminLogService(TEST_LOG_PATH.toString());
        AdminLogResponseDTO response = service.getLogs("IllegalStateException", "ERROR", null, null, null);

        assertEquals(1, response.getReturnedCount());
        assertTrue(response.getEntries().get(0).getRawLine().contains("IllegalStateException"));
    }

    private void writeLines(List<String> lines) throws IOException {
        Files.createDirectories(TEST_LOG_PATH.getParent());
        Files.write(TEST_LOG_PATH, lines, StandardCharsets.UTF_8);
    }
}
