package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.QuizResultEntry;
import com.ande.pubquizzz.dto.QuizResultsResponse;
import com.ande.pubquizzz.dto.QuizSummaryDTO;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.ResultService;
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

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserQuizController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class UserQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResultService resultService;

    @Test
    @WithMockUser
    void getQuizSummaries_authenticated_returnsList() throws Exception {
        QuizSummaryDTO dto = new QuizSummaryDTO();
        dto.setQuizId(1L);
        dto.setQuizTitle("2026 März");
        dto.setPubDate("2026-03-15");
        dto.setTeamCount(3);

        when(resultService.getQuizSummaries()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(1))
                .andExpect(jsonPath("$[0].quizTitle").value("2026 März"))
                .andExpect(jsonPath("$[0].pubDate").value("2026-03-15"))
                .andExpect(jsonPath("$[0].teamCount").value(3));
    }

    @Test
    void getQuizSummaries_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    void getQuizResults_authenticated_returnsRankedList() throws Exception {
        AnswerScoreDTO a = new AnswerScoreDTO();
        a.setQuestionNumber(1);
        a.setPoints(10);
        a.setChanged(false);

        QuizResultEntry entry = new QuizResultEntry();
        entry.setRank(1);
        entry.setTeamName("Alpha");
        entry.setTotalPoints(10);
        entry.setAnswers(List.of(a));

        QuizResultsResponse response = new QuizResultsResponse();
        response.setQuizTitle("2026 März");
        response.setEntries(List.of(entry));

        when(resultService.getResultsForQuiz(42L)).thenReturn(response);

        mockMvc.perform(get("/api/quizzes/42/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizTitle").value("2026 März"))
                .andExpect(jsonPath("$.entries[0].rank").value(1))
                .andExpect(jsonPath("$.entries[0].teamName").value("Alpha"))
                .andExpect(jsonPath("$.entries[0].totalPoints").value(10))
                .andExpect(jsonPath("$.entries[0].answers[0].questionNumber").value(1));
    }

    @Test
    void getQuizResults_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/quizzes/42/results"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
