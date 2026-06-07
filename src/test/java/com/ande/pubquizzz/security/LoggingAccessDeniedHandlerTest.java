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
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/create-quiz");
        request.setUserPrincipal(() -> "admin");
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
        assertTrue(message.contains("method=POST"));
        assertTrue(message.contains("path=/admin/create-quiz"));
        assertTrue(message.contains("user=admin"));
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
}
