package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.LeaderboardEntry;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.ResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminResultController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, SecurityTestConfig.class})
class AdminResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ResultService resultService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllResults_returnsListOfResults() throws Exception {
        ResultDTO dto = new ResultDTO();
        dto.setResultsId(1L);
        dto.setTeamId(2L);
        dto.setTeamName("Team A");
        dto.setQuizId(3L);
        dto.setQuizDate(LocalDate.of(2026, 1, 7));
        dto.setAnswers(List.of());
        dto.setTotalPoints(20);

        when(resultService.getResults(null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Team A"))
                .andExpect(jsonPath("$[0].totalPoints").value(20));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLeaderboard_returnsRankedEntries() throws Exception {
        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setRank(1);
        entry.setTeamName("Sieger");
        entry.setTotalPoints(50);

        when(resultService.getLeaderboard(null)).thenReturn(List.of(entry));

        mockMvc.perform(get("/admin/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].teamName").value("Sieger"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createResult_withValidRequest_returnsCreated() throws Exception {
        ResultDTO dto = new ResultDTO();
        dto.setResultsId(5L);
        dto.setTeamId(1L);
        dto.setQuizId(1L);
        dto.setTotalPoints(36);

        when(resultService.createResult(any(CreateResultRequest.class))).thenReturn(dto);

        List<CreateResultRequest.AnswerSubmission> answers = new java.util.ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            CreateResultRequest.AnswerSubmission a = new CreateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(i);
            answers.add(a);
        }
        CreateResultRequest request = new CreateResultRequest();
        request.setQuizId(1L);
        request.setTeamId(1L);
        request.setAnswers(answers);

        mockMvc.perform(post("/admin/results")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPoints").value(36));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createResult_withInvalidRequest_returnsBadRequest() throws Exception {
        when(resultService.createResult(any(CreateResultRequest.class)))
                .thenThrow(new IllegalArgumentException("Quiz und Team müssen ausgewählt werden."));

        CreateResultRequest request = new CreateResultRequest();
        request.setAnswers(List.of());

        mockMvc.perform(post("/admin/results")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllResults_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/admin/results"))
                .andExpect(status().is3xxRedirection());
    }
}
