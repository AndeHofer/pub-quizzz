package com.ande.pubquizzz.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingAccessDeniedHandlerTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(LoggingAccessDeniedHandler.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @AfterEach
    void cleanup() {
        logger.detachAppender(appender);
        appender.stop();
        SecurityContextHolder.clearContext();
    }

    @Test
    void handle_logsWarnWithMethodPathUserAndExceptionData() throws Exception {
        LoggingAccessDeniedHandler handler = new LoggingAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/create-quiz");
        request.setUserPrincipal(() -> "admin");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit-Agent");
        request.addHeader("X-Forwarded-For", "198.51.100.55");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "quiz.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        appender.start();
        logger.addAppender(appender);

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertEquals(403, response.getStatus());
        assertFalse(appender.list.isEmpty());

        ILoggingEvent event = appender.list.get(0);
        assertEquals(Level.WARN, event.getLevel());

        String message = event.getFormattedMessage();
        assertNotNull(message);
        assertTrue(message.contains("403 Forbidden"));
        assertTrue(message.contains("method=GET"));
        assertTrue(message.contains("path=/admin/create-quiz"));
        assertTrue(message.contains("user=admin"));
        assertTrue(message.contains("sessionId=-"));
        assertTrue(message.contains("sessionValid=-"));
        assertTrue(message.contains("remoteAddr=127.0.0.1"));
        assertTrue(message.contains("userAgent=JUnit-Agent"));
        assertTrue(message.contains("forwardedFor=198.51.100.55"));
        assertTrue(message.contains("forwardedProto=https"));
        assertTrue(message.contains("forwardedHost=quiz.example.com"));
        assertTrue(message.contains("exceptionType=AccessDeniedException"));
        assertEquals("/403.html", response.getForwardedUrl());
    }

    @Test
    void handle_usesSecurityContextUsernameWhenRequestPrincipalIsMissing() throws Exception {
        LoggingAccessDeniedHandler handler = new LoggingAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/create-quiz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-from-context", "n/a")
        );

        appender.start();
        logger.addAppender(appender);

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertEquals(403, response.getStatus());
        assertFalse(appender.list.isEmpty());

        ILoggingEvent event = appender.list.get(0);
        String message = event.getFormattedMessage();
        assertNotNull(message);
        assertTrue(message.contains("user=admin-from-context"));
    }

    @Test
    void handle_postLoginWithInvalidCsrf_redirectsToLoginWithoutForward() throws Exception {
        LoggingAccessDeniedHandler handler = new LoggingAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        var expectedToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "expected-token");
        handler.handle(request, response, new InvalidCsrfTokenException(expectedToken, "actual-token"));

        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getRedirectedUrl());
        assertEquals(null, response.getForwardedUrl());
    }

    @Test
    void handle_postNonLoginForbidden_returns403WithoutForward() throws Exception {
        LoggingAccessDeniedHandler handler = new LoggingAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/results");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertEquals(403, response.getStatus());
        assertEquals(null, response.getForwardedUrl());
        assertEquals("Forbidden", response.getErrorMessage());
    }
}
