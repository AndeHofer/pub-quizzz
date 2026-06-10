package com.ande.pubquizzz.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingAuthenticationEntryPointTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(LoggingAuthenticationEntryPoint.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @AfterEach
    void cleanup() {
        logger.detachAppender(appender);
        appender.stop();
        appender.list.clear();
    }

    @Test
    void commence_jsonAccept_returnsJson401() throws Exception {
        LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/quizzes");
        request.setQueryString("q=abc\r\nheader:bad\tvalue\u0001");
        request.addHeader("Accept", "application/json");
        request.setRemoteAddr("10.10.10.10");
        request.addHeader("User-Agent", "JUnit-Agent");
        request.addHeader("X-Forwarded-For", "203.0.113.10");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "quiz.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        appender.start();
        logger.addAppender(appender);

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Auth required"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("Nicht authentifiziert"));
        assertFalse(appender.list.isEmpty());

        ILoggingEvent event = appender.list.get(0);
        assertEquals(Level.WARN, event.getLevel());

        String message = event.getFormattedMessage();
        assertNotNull(message);
        assertTrue(message.contains("mode=json"));
        assertTrue(message.contains("sessionId=-"));
        assertTrue(message.contains("sessionValid=-"));
        assertTrue(message.contains("remoteAddr=10.10.10.10"));
        assertTrue(message.contains("userAgent=JUnit-Agent"));
        assertTrue(message.contains("forwardedFor=203.0.113.10"));
        assertTrue(message.contains("forwardedProto=https"));
        assertTrue(message.contains("forwardedHost=quiz.example.com"));
        assertTrue(message.contains("queryString=q=abc\\r\\nheader:bad\\tvalue\\u0001"));
        assertTrue(message.contains("queryStringLength=24"));
    }

    @Test
    void commence_apiPath_returnsJson401() throws Exception {
        LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/quizzes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Auth required"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("Nicht authentifiziert"));
    }

    @Test
    void commence_htmlRequest_redirectsToLogin() throws Exception {
        LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/quizzes.html");
        request.addHeader("Accept", "text/html");
        request.setRemoteAddr("10.20.30.40");
        request.addHeader("User-Agent", "Browser-1");
        request.addHeader("X-Forwarded-For", "198.51.100.23");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "pubquizzz.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        appender.start();
        logger.addAppender(appender);

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Auth required"));

        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getRedirectedUrl());
        assertFalse(appender.list.isEmpty());

        ILoggingEvent event = appender.list.get(0);
        String message = event.getFormattedMessage();
        assertNotNull(message);
        assertTrue(message.contains("mode=redirect"));
        assertTrue(message.contains("target=/login"));
        assertTrue(message.contains("remoteAddr=10.20.30.40"));
        assertTrue(message.contains("userAgent=Browser-1"));
        assertTrue(message.contains("forwardedFor=198.51.100.23"));
        assertTrue(message.contains("forwardedProto=https"));
        assertTrue(message.contains("forwardedHost=pubquizzz.example.com"));
    }

    @Test
    void commence_truncatesVeryLongQueryStringInLogs() throws Exception {
        LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/quizzes");
        request.setQueryString("a".repeat(5000));
        MockHttpServletResponse response = new MockHttpServletResponse();

        appender.start();
        logger.addAppender(appender);

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Auth required"));

        assertEquals(401, response.getStatus());
        assertFalse(appender.list.isEmpty());

        ILoggingEvent event = appender.list.get(0);
        String message = event.getFormattedMessage();
        assertNotNull(message);
        assertTrue(message.contains("queryStringLength=5000"));
        assertTrue(message.contains("...[truncated]"));
    }
}
