package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.TeamResultEntry;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserTeamController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class,
        SecurityTestConfig.class, GlobalExceptionHandler.class})
class UserTeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResultService resultService;

    @Test
    @WithMockUser
    void getTeamResults_authenticated_returns200WithData() throws Exception {
        AnswerScoreDTO a = new AnswerScoreDTO();
        a.setQuestionNumber(1);
        a.setPoints(5);
        a.setChanged(false);

        TeamResultEntry entry = new TeamResultEntry();
        entry.setQuizDate("2026-03-15");
        entry.setTotalPoints(5);
        entry.setAnswers(List.of(a));

        when(resultService.getResultsForTeam("TestTeam")).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/teams/TestTeam/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].quizDate").value("2026-03-15"))
                .andExpect(jsonPath("$[0].totalPoints").value(5));
    }

    @Test
    @WithMockUser
    void getTeamResults_unknownTeam_returns200WithEmptyArray() throws Exception {
        when(resultService.getResultsForTeam("Unknown")).thenReturn(List.of());

        mockMvc.perform(get("/api/teams/Unknown/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getTeamResults_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/teams/TestTeam/results"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
