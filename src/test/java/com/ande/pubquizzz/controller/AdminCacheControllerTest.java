package com.ande.pubquizzz.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.CacheAdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCacheController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class AdminCacheControllerTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(AdminCacheController.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CacheAdminService cacheAdminService;

    @AfterEach
    void cleanup() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidate_admin_returnsOkWithSummary() throws Exception {
        when(cacheAdminService.evictAllCaches()).thenReturn(Map.of(
                "success", true,
                "clearedCaches", List.of("quizzes.all", "news.latest"),
                "clearedCount", 2
        ));

        appender.start();
        logger.addAppender(appender);

        mockMvc.perform(post("/admin/cache/invalidate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.clearedCount").value(2))
                .andExpect(jsonPath("$.clearedCaches[0]").value("quizzes.all"));

        assertFalse(appender.list.isEmpty());
        assertTrue(appender.list.getFirst().getFormattedMessage().contains(
                "Cache invalidated: all caches (trigger=admin, cleared=2)"
        ));
    }

    @Test
    @WithMockUser
    void invalidate_nonAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(post("/admin/cache/invalidate").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
