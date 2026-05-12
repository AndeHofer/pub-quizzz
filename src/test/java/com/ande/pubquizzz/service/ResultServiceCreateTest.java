package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.ResultMapper;
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

    @Mock
    private ResultMapper resultMapper;

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

    private CreateResultRequest makeValidRequest(Long quizId, Long teamId) {
        CreateResultRequest req = new CreateResultRequest();
        req.setQuizId(quizId);
        req.setTeamId(teamId);
        req.setAnswers(ResultServiceTestData.createAnswerSubmissions(1, 1, 1, 1, 1, 1, 1, 1));
        return req;
    }

    @Test
    public void testCreateResultHappyPath() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));
        when(resultRepository.findByTeam_TeamsIdAndQuiz_QuizId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(resultRepository.save(any(Result.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateResultRequest req = new CreateResultRequest();
        req.setQuizId(1L);
        req.setTeamId(5L);

        int total = 0;
        int[] allowedPoints = {5, 3, 2, 1, 5, 3, 2, 1};
        for (int allowedPoint : allowedPoints) {
            total += allowedPoint;
        }
        List<CreateResultRequest.AnswerSubmission> answers = ResultServiceTestData.createAnswerSubmissions(allowedPoints);
        req.setAnswers(answers);

        ResultDTO expectedDto = new ResultDTO();
        expectedDto.setQuizId(1L);
        expectedDto.setTeamId(5L);
        expectedDto.setTotalPoints(total);
        expectedDto.setAnswers(answers.stream().map(a -> {
            AnswerScoreDTO s = new AnswerScoreDTO();
            s.setQuestionNumber(a.getQuestionNumber());
            s.setPoints(a.getPoints());
            return s;
        }).toList());
        when(resultMapper.toDTO(any(Result.class))).thenReturn(expectedDto);

        ResultDTO dto = resultService.createResult(req);
        assertNotNull(dto);
        assertEquals(1L, dto.getQuizId());
        assertEquals(5L, dto.getTeamId());
        assertEquals(total, dto.getTotalPoints());
        assertEquals(8, dto.getAnswers().size());
    }

    @Test
    public void testCreateResultQuizNotFound() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        CreateResultRequest req = makeValidRequest(99L, 5L);
        assertThrows(ResourceNotFoundException.class, () -> resultService.createResult(req));
    }

    @Test
    public void testCreateResultDuplicateTeamAndQuiz() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));
        when(resultRepository.findByTeam_TeamsIdAndQuiz_QuizId(5L, 1L))
                .thenReturn(Optional.of(new Result()));

        assertThrows(BusinessValidationException.class,
                () -> resultService.createResult(makeValidRequest(1L, 5L)));
    }

    @Test
    public void testCreateResultDuplicateQuestion() {
        when(quizRepository.findById(anyLong())).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(team));
        when(resultRepository.findByTeam_TeamsIdAndQuiz_QuizId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

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

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> resultService.createResult(req));
        assertTrue(ex.getMessage().contains("Doppelte Frage"));
    }

    @Test
    public void testCreateResultMissingQuestion() {
        when(quizRepository.findById(anyLong())).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(team));
        when(resultRepository.findByTeam_TeamsIdAndQuiz_QuizId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

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
        req.setAnswers(answers);

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> resultService.createResult(req));
        assertTrue(ex.getMessage().contains("Fehlende Frage"));
    }

    @Test
    public void testCreateResultDisallowedPointsValue() {
        when(quizRepository.findById(anyLong())).thenReturn(Optional.of(quiz));
        when(teamRepository.findById(anyLong())).thenReturn(Optional.of(team));
        when(resultRepository.findByTeam_TeamsIdAndQuiz_QuizId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        CreateResultRequest req = makeValidRequest(1L, 5L);
        req.getAnswers().get(3).setPoints(4);

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> resultService.createResult(req));
        assertTrue(ex.getMessage().contains("0, 1, 2, 3 oder 5"));
    }
}
