package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import com.ande.pubquizzz.dto.QuizResultEntry;
import com.ande.pubquizzz.dto.QuizResultsResponse;
import com.ande.pubquizzz.dto.QuizSummaryDTO;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.QuizService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserQuizController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class UserQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResultService resultService;

    @MockitoBean
    private QuizService quizService;

    @Test
    @WithMockUser
    void getQuizSummaries_authenticated_returnsList() throws Exception {
        QuizSummaryDTO dto = new QuizSummaryDTO();
        dto.setQuizId(1L);
        dto.setQuizTitle("2026 März");
        dto.setPubDate("2026-03-15");
        dto.setFinished(true);
        dto.setTeamCount(3);
        dto.setWinnerTeamId(1L);
        dto.setWinnerTeamName("Alpha");

        when(resultService.getQuizSummaries()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(1))
                .andExpect(jsonPath("$[0].quizTitle").value("2026 März"))
                .andExpect(jsonPath("$[0].pubDate").value("2026-03-15"))
                .andExpect(jsonPath("$[0].finished").value(true))
                .andExpect(jsonPath("$[0].teamCount").value(3))
                .andExpect(jsonPath("$[0].winnerTeamId").value(1))
                .andExpect(jsonPath("$[0].winnerTeamName").value("Alpha"));
    }

    @Test
    void getQuizSummaries_unauthenticated_returnsJson401() throws Exception {
        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getQuizResults_authenticated_returnsRankedList() throws Exception {
        AnswerScoreDTO a = new AnswerScoreDTO();
        a.setQuestionNumber(1);
        a.setPoints(10);

        QuizResultEntry entry = new QuizResultEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
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
                .andExpect(jsonPath("$.entries[0].teamId").value(1))
                .andExpect(jsonPath("$.entries[0].teamName").value("Alpha"))
                .andExpect(jsonPath("$.entries[0].totalPoints").value(10))
                .andExpect(jsonPath("$.entries[0].answers[0].questionNumber").value(1));
    }

    @Test
    void getQuizResults_unauthenticated_returnsJson401() throws Exception {
        mockMvc.perform(get("/api/quizzes/42/results"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getQuizDetail_authenticated_returnsQuizWithQuestionsHintsAndAnswers() throws Exception {
        QuizDetailDTO.HintDetailDTO hint = new QuizDetailDTO.HintDetailDTO();
        hint.setHintText("Hinweis 1");
        hint.setImageUrlAtStart("/uploads/start.png");
        hint.setImageUrlAsHint("/uploads/hint.png");

        QuizDetailDTO.QuestionDetailDTO question = new QuizDetailDTO.QuestionDetailDTO();
        question.setNumber(1);
        question.setQuestionText("Frage 1");
        question.setAnswer("Antwort 1");
        question.setAnswerImageUrl("/uploads/answer.png");
        question.setHints(List.of(hint));

        QuizDetailDTO dto = new QuizDetailDTO();
        dto.setQuizId(42L);
        dto.setPubDate(java.time.LocalDate.of(2026, 3, 15));
        dto.setCreator("Quizmaster Klaus");
        dto.setQuestions(List.of(question));

        when(quizService.getQuizDetailById(42L)).thenReturn(java.util.Optional.of(dto));

        mockMvc.perform(get("/api/quizzes/42/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(42))
                .andExpect(jsonPath("$.pubDate").value("2026-03-15"))
                .andExpect(jsonPath("$.creator").value("Quizmaster Klaus"))
                .andExpect(jsonPath("$.questions[0].number").value(1))
                .andExpect(jsonPath("$.questions[0].questionText").value("Frage 1"))
                .andExpect(jsonPath("$.questions[0].answer").value("Antwort 1"))
                .andExpect(jsonPath("$.questions[0].answerImageUrl").value("/uploads/answer.png"))
                .andExpect(jsonPath("$.questions[0].hints[0].hintText").value("Hinweis 1"))
                .andExpect(jsonPath("$.questions[0].hints[0].imageUrlAtStart").value("/uploads/start.png"))
                .andExpect(jsonPath("$.questions[0].hints[0].imageUrlAsHint").value("/uploads/hint.png"));
    }

    @Test
    void getQuizDetail_unauthenticated_returnsJson401() throws Exception {
        mockMvc.perform(get("/api/quizzes/42/detail"))
                .andExpect(status().isUnauthorized());
    }
}
