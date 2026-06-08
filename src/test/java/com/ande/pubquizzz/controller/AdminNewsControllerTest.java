package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CreateNewsRequest;
import com.ande.pubquizzz.dto.NewsDTO;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminNewsController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class AdminNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsService newsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllNews_admin_returnsOkWithPayload() throws Exception {
        when(newsService.getAllNewsForAdmin()).thenReturn(List.of(
                new NewsDTO(2L, "Neu 2", "Text 2", Instant.parse("2026-06-06T10:15:30Z"), true),
                new NewsDTO(1L, "Neu 1", "Text 1", Instant.parse("2026-06-05T10:15:30Z"), false)
        ));

        mockMvc.perform(get("/admin/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].newsId").value(2))
                .andExpect(jsonPath("$[0].title").value("Neu 2"))
                .andExpect(jsonPath("$[0].showOnHomePage").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createNews_validRequest_returnsCreated() throws Exception {
        NewsDTO dto = new NewsDTO(1L, "Titel", "Text", Instant.parse("2026-06-05T10:15:30Z"), true);
        when(newsService.createNews(any(CreateNewsRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/admin/news")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\":\"Titel\"," +
                                "\"text\":\"Text\"," +
                                "\"showOnHomePage\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Titel"))
                .andExpect(jsonPath("$.showOnHomePage").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createNews_blankTitle_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/news")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\":\"\"," +
                                "\"text\":\"Text\"," +
                                "\"showOnHomePage\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createNews_missingShowOnHomePage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/news")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\":\"Titel\"," +
                                "\"text\":\"Text\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateNews_validRequest_returnsOk() throws Exception {
        NewsDTO dto = new NewsDTO(1L, "Neuer Titel", "Neuer Text", Instant.parse("2026-06-05T10:15:30Z"), false);
        when(newsService.updateNews(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/admin/news/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\":\"Neuer Titel\"," +
                                "\"text\":\"Neuer Text\"," +
                                "\"showOnHomePage\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Neuer Titel"))
                .andExpect(jsonPath("$.showOnHomePage").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNews_admin_returnsOk() throws Exception {
        mockMvc.perform(delete("/admin/news/1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void adminEndpoint_nonAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(get("/admin/news"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateNews_notFound_returnsNotFound() throws Exception {
        when(newsService.updateNews(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Neuigkeit nicht gefunden: 999"));

        mockMvc.perform(put("/admin/news/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\":\"Titel\"," +
                                "\"text\":\"Text\"," +
                                "\"showOnHomePage\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNews_notFound_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Neuigkeit nicht gefunden: 999"))
                .when(newsService)
                .deleteNews(999L);

        mockMvc.perform(delete("/admin/news/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listNews_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/admin/news"))
                .andExpect(status().is3xxRedirection());
    }
}
