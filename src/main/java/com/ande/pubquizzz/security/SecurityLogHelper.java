package com.ande.pubquizzz.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

final class SecurityLogHelper {

    private static final int MAX_LOGGED_QUERY_LENGTH = 4096;
    private static final String TRUNCATED_SUFFIX = "...[truncated]";

    private SecurityLogHelper() {
    }

    static String safeLogValue(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }

    static String resolveSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return safeLogValue(session.getId());
        }
        return safeLogValue(request.getRequestedSessionId());
    }

    static String resolveSessionValid(HttpServletRequest request) {
        if (request.getRequestedSessionId() == null || request.getRequestedSessionId().isBlank()) {
            return "-";
        }
        return String.valueOf(request.isRequestedSessionIdValid());
    }

    static String sanitizeQueryStringForLog(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return "-";
        }

        StringBuilder sanitized = new StringBuilder(Math.min(queryString.length(), MAX_LOGGED_QUERY_LENGTH));
        int outputLength = 0;
        for (int i = 0; i < queryString.length(); i++) {
            if (outputLength >= MAX_LOGGED_QUERY_LENGTH) {
                sanitized.append(TRUNCATED_SUFFIX);
                break;
            }

            char ch = queryString.charAt(i);
            String escaped = escapeControlCharacter(ch);
            if (escaped != null) {
                if (outputLength + escaped.length() > MAX_LOGGED_QUERY_LENGTH) {
                    sanitized.append(TRUNCATED_SUFFIX);
                    break;
                }
                sanitized.append(escaped);
                outputLength += escaped.length();
                continue;
            }

            sanitized.append(ch);
            outputLength++;
        }

        return sanitized.toString();
    }

    static int queryStringLength(String queryString) {
        if (queryString == null) {
            return 0;
        }
        return queryString.length();
    }

    private static String escapeControlCharacter(char ch) {
        return switch (ch) {
            case '\r' -> "\\r";
            case '\n' -> "\\n";
            case '\t' -> "\\t";
            default -> Character.isISOControl(ch) ? String.format("\\u%04x", (int) ch) : null;
        };
    }
}
