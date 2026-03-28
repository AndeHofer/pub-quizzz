package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.dto.UpdateResultRequest;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminResultController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
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
    void createResult_withValidRequest_returnsCreated() throws Exception {
        ResultDTO dto = new ResultDTO();
        dto.setResultsId(5L);
        dto.setTeamId(1L);
        dto.setQuizId(1L);
        dto.setTotalPoints(28);

        when(resultService.createResult(any(CreateResultRequest.class))).thenReturn(dto);

        // Valid points: only values from {0, 1, 2, 3, 5} — max is 5
        int[] validPoints = {5, 3, 5, 3, 5, 3, 5, 0};
        List<CreateResultRequest.AnswerSubmission> answers = new java.util.ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            CreateResultRequest.AnswerSubmission a = new CreateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(validPoints[i - 1]);
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
                .andExpect(jsonPath("$.totalPoints").value(28));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createResult_withPointsAboveMax_returnsBadRequest() throws Exception {
        // points = 6 violates @Max(5) on AnswerSubmission.points
        List<CreateResultRequest.AnswerSubmission> answers = new java.util.ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            CreateResultRequest.AnswerSubmission a = new CreateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(6); // invalid — max is 5
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
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createResult_withInvalidRequest_returnsBadRequest() throws Exception {
        when(resultService.createResult(any(CreateResultRequest.class)))
                .thenThrow(new BusinessValidationException("Quiz und Team müssen ausgewählt werden."));

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

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteResult_returnsNoContent() throws Exception {
        doNothing().when(resultService).deleteResult(1L);

        mockMvc.perform(delete("/admin/results/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteResult_notFound_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Ergebnis nicht gefunden: 99"))
                .when(resultService).deleteResult(99L);

        mockMvc.perform(delete("/admin/results/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateResult_withValidRequest_returnsOk() throws Exception {
        ResultDTO dto = new ResultDTO();
        dto.setResultsId(1L);
        dto.setTotalPoints(25);
        when(resultService.updateResult(eq(1L), any(UpdateResultRequest.class))).thenReturn(dto);

        // Build valid update request: 8 answers with points in {0,1,2,3,5}
        List<UpdateResultRequest.AnswerSubmission> answers = new java.util.ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            UpdateResultRequest.AnswerSubmission a = new UpdateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(i <= 4 ? 5 : 3);
            answers.add(a);
        }
        UpdateResultRequest request = new UpdateResultRequest();
        request.setAnswers(answers);

        mockMvc.perform(put("/admin/results/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(25));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateResult_withPointsAboveMax_returnsBadRequest() throws Exception {
        List<UpdateResultRequest.AnswerSubmission> answers = new java.util.ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            UpdateResultRequest.AnswerSubmission a = new UpdateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(6); // invalid — max is 5
            answers.add(a);
        }
        UpdateResultRequest request = new UpdateResultRequest();
        request.setAnswers(answers);

        mockMvc.perform(put("/admin/results/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
