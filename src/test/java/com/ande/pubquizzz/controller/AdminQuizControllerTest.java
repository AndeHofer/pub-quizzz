package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.QuizDTO;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.ImageStorageService;
import com.ande.pubquizzz.service.QuizService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminQuizController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class AdminQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllQuizzes_returnsListOfQuizzes() throws Exception {
        QuizDTO dto = new QuizDTO();
        dto.setQuizId(1L);
        dto.setPubDate(LocalDate.of(2026, 1, 7));
        dto.setSubmitDate(LocalDate.of(2026, 1, 7));
        dto.setQuestionCount(8);

        when(quizService.getAllQuizzes()).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(1))
                .andExpect(jsonPath("$[0].questionCount").value(8));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuizById_whenFound_returnsQuiz() throws Exception {
        QuizDTO dto = new QuizDTO();
        dto.setQuizId(2L);
        dto.setPubDate(LocalDate.of(2026, 2, 1));
        dto.setSubmitDate(LocalDate.of(2026, 2, 1));
        dto.setQuestionCount(8);

        when(quizService.getQuizById(2L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/admin/quiz/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuizById_whenNotFound_returns404() throws Exception {
        when(quizService.getQuizById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/quiz/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuizDetailById_whenFound_returnsDetail() throws Exception {
        QuizDetailDTO dto = new QuizDetailDTO();
        dto.setQuizId(3L);
        dto.setPubDate(LocalDate.of(2026, 3, 1));
        dto.setSubmitDate(LocalDate.of(2026, 3, 1));
        dto.setQuestions(List.of());

        when(quizService.getQuizDetailById(3L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/admin/quiz/3/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQuiz_whenFound_returnsOk() throws Exception {
        when(quizService.deleteQuiz(1L)).thenReturn(true);

        mockMvc.perform(delete("/admin/quiz/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQuiz_whenNotFound_returns404() throws Exception {
        when(quizService.deleteQuiz(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/admin/quiz/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllQuizzes_unauthenticated_returns401or302() throws Exception {
        mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuizDates_withValidDates_returnsOk() throws Exception {
        mockMvc.perform(patch("/admin/quiz/1/dates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pubDate\":\"2024-01-01\",\"submitDate\":\"2024-01-15\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQuizDates_withMissingDate_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/quiz/1/dates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pubDate\":\"2024-01-01\"}"))
                .andExpect(status().isBadRequest());
    }
}
