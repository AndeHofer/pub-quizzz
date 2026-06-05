package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.NewsDTO;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserNewsController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class UserNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsService newsService;

    @Test
    @WithMockUser
    void getLatestNews_authenticated_returnsLatestThreeByDefault() throws Exception {
        when(newsService.getLatestNews(3)).thenReturn(List.of(
                new NewsDTO(3L, "Neu 1", "Text", Instant.parse("2026-06-05T10:15:30Z")),
                new NewsDTO(2L, "Neu 2", "Text", Instant.parse("2026-06-04T10:15:30Z"))
        ));

        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Neu 1"));
    }

    @Test
    void getLatestNews_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/news"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    void getLatestNews_limitAboveMax_passesRequestedLimitToService() throws Exception {
        when(newsService.getLatestNews(99)).thenReturn(List.of(
                new NewsDTO(3L, "Neu 1", "Text", Instant.parse("2026-06-05T10:15:30Z"))
        ));

        mockMvc.perform(get("/api/news?limit=99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Neu 1"));

        verify(newsService).getLatestNews(99);
    }
}
