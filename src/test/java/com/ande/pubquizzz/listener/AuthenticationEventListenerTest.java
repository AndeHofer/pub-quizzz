package com.ande.pubquizzz.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationEventListenerTest {

    @Test
    void successfulLoginLogsOriginalMessageWithoutPassword() {
        Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationEventListener.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            var authentication = UsernamePasswordAuthenticationToken.unauthenticated("alice", "secret-password");

            new AuthenticationEventListener().onAuthenticationSuccess(new AuthenticationSuccessEvent(authentication));

            assertEquals(1, appender.list.size());
            ILoggingEvent logged = appender.list.getFirst();
            assertEquals(Level.INFO, logged.getLevel());
            assertEquals("User alice logged in successfully", logged.getFormattedMessage());
            assertFalse(logged.getFormattedMessage().contains("secret-password"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void failedLoginIsStillLoggedWithoutPassword() {
        Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationEventListener.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            var authentication = UsernamePasswordAuthenticationToken.unauthenticated("alice", "secret-password");
            var event = new AuthenticationFailureBadCredentialsEvent(authentication, new BadCredentialsException("Invalid credentials"));

            new AuthenticationEventListener().onAuthenticationFailure(event);

            assertEquals(1, appender.list.size());
            ILoggingEvent logged = appender.list.getFirst();
            assertEquals(Level.WARN, logged.getLevel());
            assertTrue(logged.getFormattedMessage().contains("alice"));
            assertFalse(logged.getFormattedMessage().contains("secret-password"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
