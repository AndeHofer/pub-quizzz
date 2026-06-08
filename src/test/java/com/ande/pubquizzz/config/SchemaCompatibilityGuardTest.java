package com.ande.pubquizzz.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchemaCompatibilityGuardTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SchemaCompatibilityGuard schemaCompatibilityGuard;

    @Test
    void ensureNewsSchemaCompatibility_executesIdempotentAlterStatement() {
        schemaCompatibilityGuard.ensureNewsSchemaCompatibility();

        verify(jdbcTemplate).execute("ALTER TABLE news ADD COLUMN IF NOT EXISTS show_on_home_page BOOLEAN NOT NULL DEFAULT FALSE");
    }
}
