package com.ande.pubquizzz.service;

import com.ande.pubquizzz.dto.AdminLogEntryDTO;
import com.ande.pubquizzz.dto.AdminLogResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdminLogService {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final Set<String> ALLOWED_LEVELS = Set.of("DEBUG", "INFO", "WARN", "ERROR");
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+"
                    + "(?<level>TRACE|DEBUG|INFO|WARN|ERROR)\\s+"
                    + "(?<source>.*?)\\s-\\s(?<message>.*)$"
    );
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter RESPONSE_TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Path logFilePath;

    public AdminLogService(@Value("${app.admin.log-file:/logs/pub-quizzz.log}") String logFilePath) {
        this.logFilePath = Path.of(logFilePath);
    }

    public AdminLogResponseDTO getLogs(String q,
                                       String level,
                                       String from,
                                       String to,
                                       Integer limit) {
        String normalizedQuery = normalizeTextFilter(q);
        String normalizedLevel = normalizeLevel(level);
        LocalDateTime fromTime = parseDateTime(from, "from");
        LocalDateTime toTime = parseDateTime(to, "to");
        if (fromTime != null && toTime != null && fromTime.isAfter(toTime)) {
            throw new IllegalArgumentException("from darf nicht nach to liegen.");
        }
        int appliedLimit = normalizeLimit(limit);

        List<String> lines = readLogLines();
        List<String> events = groupLinesIntoEvents(lines);
        List<AdminLogEntryDTO> entries = new ArrayList<>();

        for (int i = events.size() - 1; i >= 0; i--) {
            ParsedLogEvent parsed = parseEvent(events.get(i));
            if (!matchesWordFilter(parsed.rawEvent(), normalizedQuery)) {
                continue;
            }
            if (!matchesLevelFilter(parsed.level(), normalizedLevel)) {
                continue;
            }
            if (!matchesTimeFilter(parsed.timestamp(), fromTime, toTime)) {
                continue;
            }

            entries.add(new AdminLogEntryDTO(
                    parsed.timestamp() == null ? null : parsed.timestamp().format(RESPONSE_TIMESTAMP_FORMAT),
                    parsed.level(),
                    parsed.source(),
                    parsed.message(),
                    parsed.rawEvent()
            ));

            if (entries.size() >= appliedLimit) {
                break;
            }
        }

        return new AdminLogResponseDTO(entries, appliedLimit, entries.size());
    }

    private List<String> readLogLines() {
        if (!Files.exists(logFilePath)) {
            throw new IllegalArgumentException("Logdatei nicht gefunden: " + logFilePath);
        }
        try {
            return Files.readAllLines(logFilePath, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Logdatei konnte nicht gelesen werden: " + logFilePath, ex);
        }
    }

    private String normalizeTextFilter(String q) {
        if (q == null) {
            return null;
        }
        String trimmed = q.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeLevel(String level) {
        if (level == null || level.trim().isEmpty()) {
            return null;
        }
        String normalized = level.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_LEVELS.contains(normalized)) {
            throw new IllegalArgumentException("Ungueltiger Level-Filter: " + level);
        }
        return normalized;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit muss groesser als 0 sein.");
        }
        if (limit > MAX_LIMIT) {
            return MAX_LIMIT;
        }
        return limit;
    }

    private LocalDateTime parseDateTime(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ungueltiger Zeitwert fuer " + paramName + ": " + value);
        }
    }

    private List<String> groupLinesIntoEvents(List<String> lines) {
        List<String> events = new ArrayList<>();
        StringBuilder current = null;

        for (String line : lines) {
            if (isEventHeader(line)) {
                if (current != null) {
                    events.add(current.toString());
                }
                current = new StringBuilder(line);
                continue;
            }

            if (current == null) {
                current = new StringBuilder(line);
            } else {
                current.append('\n').append(line);
            }
        }

        if (current != null) {
            events.add(current.toString());
        }
        return events;
    }

    private boolean isEventHeader(String line) {
        return LOG_PATTERN.matcher(line).matches();
    }

    private ParsedLogEvent parseEvent(String rawEvent) {
        String[] parts = rawEvent.split("\\R", 2);
        String headerLine = parts[0];

        Matcher matcher = LOG_PATTERN.matcher(headerLine);
        if (!matcher.matches()) {
            return new ParsedLogEvent(null, "UNKNOWN", null, headerLine, rawEvent);
        }

        LocalDateTime parsedTimestamp = null;
        try {
            parsedTimestamp = LocalDateTime.parse(matcher.group("timestamp"), LOG_TIMESTAMP_FORMAT);
        } catch (DateTimeParseException ignored) {
            // handled by null timestamp fallback
        }

        String source = matcher.group("source");
        if (source != null) {
            source = source.trim();
            if (source.isEmpty()) {
                source = null;
            }
        }

        return new ParsedLogEvent(
                parsedTimestamp,
                matcher.group("level"),
                source,
                matcher.group("message"),
                rawEvent
        );
    }

    private boolean matchesWordFilter(String rawLine, String normalizedQuery) {
        if (normalizedQuery == null) {
            return true;
        }
        return rawLine.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private boolean matchesLevelFilter(String lineLevel, String filterLevel) {
        if (filterLevel == null) {
            return true;
        }
        return filterLevel.equals(lineLevel);
    }

    private boolean matchesTimeFilter(LocalDateTime timestamp, LocalDateTime fromTime, LocalDateTime toTime) {
        if (fromTime == null && toTime == null) {
            return true;
        }
        if (timestamp == null) {
            return false;
        }
        if (fromTime != null && timestamp.isBefore(fromTime)) {
            return false;
        }
        return toTime == null || !timestamp.isAfter(toTime);
    }

    private record ParsedLogEvent(LocalDateTime timestamp, String level, String source, String message,
                                  String rawEvent) {
    }
}
