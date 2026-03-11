package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.ResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResultServiceCreateTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private com.ande.pubquizzz.database.repositories.QuizRepository quizRepository;

    @Mock
    private com.ande.pubquizzz.database.repositories.TeamRepository teamRepository;

    @InjectMocks
    private ResultService resultService;

    private Quiz quiz;
    private Team team;

    @BeforeEach
    public void setup() {
        quiz = new Quiz();
        quiz.setQuizId(1L);
        quiz.setPubDate(LocalDate.now());
        quiz.setSubmitDate(LocalDate.now());

        team = new Team();
        team.setTeamsId(5L);
        team.setTeamName("Die Besten");
    }

    @Test
    public void testCreateResultHappyPath() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));
        when(resultRepository.save(any(Result.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateResultRequest req = new CreateResultRequest();
        req.setQuizId(1L);
        req.setTeamId(5L);

        List<CreateResultRequest.AnswerSubmission> answers = new ArrayList<>();
        int total = 0;
        for (int i = 1; i <= 8; i++) {
            CreateResultRequest.AnswerSubmission a = new CreateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(i);
            total += i;
            answers.add(a);
        }
        req.setAnswers(answers);

        ResultDTO dto = resultService.createResult(req);
        assertNotNull(dto);
        assertEquals(1L, dto.getQuizId());
        assertEquals(5L, dto.getTeamId());
        assertEquals(total, dto.getTotalPoints());
        assertEquals(8, dto.getAnswers().size());
    }

    @Test
    public void testCreateResultMissingQuiz() {
        CreateResultRequest req = new CreateResultRequest();
        req.setQuizId(null);
        req.setTeamId(5L);
        req.setAnswers(new ArrayList<>());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> resultService.createResult(req));
        assertTrue(ex.getMessage().contains("Quiz und Team müssen ausgewählt werden"));
    }

    @Test
    public void testCreateResultWrongAnswerCount() {
        when(quizRepository.findById(anyLong())).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(team));

        CreateResultRequest req = new CreateResultRequest();
        req.setQuizId(1L);
        req.setTeamId(5L);
        req.setAnswers(new ArrayList<>()); // empty

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> resultService.createResult(req));
        assertTrue(ex.getMessage().contains("Es müssen 8 Antworten übergeben werden"));
    }

    @Test
    public void testCreateResultNegativePoints() {
        when(quizRepository.findById(anyLong())).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(team));

        CreateResultRequest req = new CreateResultRequest();
        req.setQuizId(1L);
        req.setTeamId(5L);
        List<CreateResultRequest.AnswerSubmission> answers = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            CreateResultRequest.AnswerSubmission a = new CreateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(i == 3 ? -1 : 1);
            answers.add(a);
        }
        req.setAnswers(answers);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> resultService.createResult(req));
        assertTrue(ex.getMessage().contains("Punkte müssen >= 0 sein"));
    }

    @Test
    public void testCreateResultDuplicateQuestion() {
        when(quizRepository.findById(anyLong())).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(team));

        CreateResultRequest req = new CreateResultRequest();
        req.setQuizId(1L);
        req.setTeamId(5L);
        List<CreateResultRequest.AnswerSubmission> answers = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            CreateResultRequest.AnswerSubmission a = new CreateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(1);
            answers.add(a);
        }
        // duplicate question 7
        CreateResultRequest.AnswerSubmission dup = new CreateResultRequest.AnswerSubmission();
        dup.setQuestionNumber(7);
        dup.setPoints(1);
        answers.add(dup);
        req.setAnswers(answers);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> resultService.createResult(req));
        assertTrue(ex.getMessage().contains("Doppelte Frage"));
    }
}
