package com.ande.pubquizzz.cache;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(CacheInvalidationLoggingAspectTest.TestConfig.class)
class CacheInvalidationLoggingAspectTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(CacheInvalidationLoggingAspect.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @jakarta.annotation.Resource
    private TestService testService;

    @AfterEach
    void cleanup() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void invalidate_logsShortAutomaticInvalidationMessage() {
        appender.start();
        logger.addAppender(appender);

        testService.invalidate();

        assertFalse(appender.list.isEmpty());
        ILoggingEvent event = appender.list.getFirst();
        assertEquals(Level.INFO, event.getLevel());
        assertTrue(event.getFormattedMessage().contains("Cache invalidated: all caches (trigger=TestService.invalidate)"));
    }

    @Test
    void invalidate_whenMethodThrows_doesNotLog() {
        appender.start();
        logger.addAppender(appender);

        assertThrows(IllegalStateException.class, () -> testService.failInvalidate());

        assertTrue(appender.list.isEmpty());
    }

    @Configuration
    @EnableAspectJAutoProxy
    @Import(CacheInvalidationLoggingAspect.class)
    static class TestConfig {
        @Bean
        TestService testService() {
            return new TestService();
        }
    }

    static class TestService {
        @InvalidateAllAppCaches
        void invalidate() {
        }

        @InvalidateAllAppCaches
        void failInvalidate() {
            throw new IllegalStateException("boom");
        }
    }
}
