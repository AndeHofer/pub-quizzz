package com.ande.pubquizzz.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaCompatibilityGuard {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureNewsSchemaCompatibility() {
        jdbcTemplate.execute("ALTER TABLE news ADD COLUMN IF NOT EXISTS show_on_home_page BOOLEAN NOT NULL DEFAULT FALSE");
        log.info("Schema compatibility check done: news.show_on_home_page");
    }
}
