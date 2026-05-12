package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.dto.UpdateResultRequest;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.ResultMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultServiceDeleteUpdateTest {

    @Mock
    private ResultRepository resultRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private ResultMapper resultMapper;

    @InjectMocks
    private ResultService resultService;

    // ---- deleteResult ----

    @Test
    void deleteResult_success() {
        when(resultRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> resultService.deleteResult(1L));
        verify(resultRepository).deleteById(1L);
    }

    @Test
    void deleteResult_notFound_throwsResourceNotFoundException() {
        when(resultRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> resultService.deleteResult(99L));
        verify(resultRepository, never()).deleteById(any());
    }

    // ---- updateResult ----

    @Test
    void updateResult_changesPoints_setsChangedFlag() {
        // Build a result with 8 answers, all at 3 points
        Result result = new Result();
        List<ResultAnswer> answers = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            ResultAnswer ra = new ResultAnswer();
            ra.setQuestionNumber(i);
            ra.setPoints(3);
            ra.setChanged(false);
            answers.add(ra);
        }
        result.setAnswers(answers);

        when(resultRepository.findByIdWithAnswers(1L)).thenReturn(Optional.of(result));
        when(resultRepository.save(any(Result.class))).thenReturn(result);
        when(resultMapper.toDTO(any(Result.class))).thenReturn(new ResultDTO());

        // Submit: question 1 → 5 (changed), questions 2-8 → 3 (unchanged)
        UpdateResultRequest req = new UpdateResultRequest();
        List<UpdateResultRequest.AnswerSubmission> submissions = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            UpdateResultRequest.AnswerSubmission a = new UpdateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(i == 1 ? 5 : 3);
            submissions.add(a);
        }
        req.setAnswers(submissions);

        resultService.updateResult(1L, req);

        // Question 1: points changed to 5, changed=true
        assertEquals(5, result.getAnswers().get(0).getPoints());
        assertTrue(result.getAnswers().get(0).getChanged());
        // Question 2: unchanged, changed still false
        assertEquals(3, result.getAnswers().get(1).getPoints());
        assertFalse(result.getAnswers().get(1).getChanged());
    }

    @Test
    void updateResult_notFound_throwsResourceNotFoundException() {
        when(resultRepository.findByIdWithAnswers(99L)).thenReturn(Optional.empty());

        UpdateResultRequest req = new UpdateResultRequest();
        req.setAnswers(new ArrayList<>());

        assertThrows(ResourceNotFoundException.class, () -> resultService.updateResult(99L, req));
    }

    @Test
    void updateResult_duplicateQuestion_throwsBusinessValidationException() {
        Result result = new Result();
        result.setAnswers(buildResultAnswersWithPoints(3));

        when(resultRepository.findByIdWithAnswers(1L)).thenReturn(Optional.of(result));

        UpdateResultRequest req = new UpdateResultRequest();
        List<UpdateResultRequest.AnswerSubmission> submissions = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            UpdateResultRequest.AnswerSubmission a = new UpdateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(3);
            submissions.add(a);
        }
        UpdateResultRequest.AnswerSubmission duplicate = new UpdateResultRequest.AnswerSubmission();
        duplicate.setQuestionNumber(7);
        duplicate.setPoints(3);
        submissions.add(duplicate);
        req.setAnswers(submissions);

        assertThrows(BusinessValidationException.class, () -> resultService.updateResult(1L, req));
        verify(resultRepository, never()).save(any(Result.class));
    }

    @Test
    void updateResult_missingQuestion_throwsBusinessValidationException() {
        Result result = new Result();
        result.setAnswers(buildResultAnswersWithPoints(3));

        when(resultRepository.findByIdWithAnswers(1L)).thenReturn(Optional.of(result));

        UpdateResultRequest req = new UpdateResultRequest();
        List<UpdateResultRequest.AnswerSubmission> submissions = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            UpdateResultRequest.AnswerSubmission a = new UpdateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(3);
            submissions.add(a);
        }
        req.setAnswers(submissions);

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> resultService.updateResult(1L, req));
        assertTrue(exception.getMessage().contains("Fehlende Frage"));
        verify(resultRepository, never()).save(any(Result.class));
    }

    @Test
    void updateResult_disallowedPointsValue_throwsBusinessValidationException() {
        Result result = new Result();
        result.setAnswers(buildResultAnswersWithPoints(3));

        when(resultRepository.findByIdWithAnswers(1L)).thenReturn(Optional.of(result));

        UpdateResultRequest req = new UpdateResultRequest();
        List<UpdateResultRequest.AnswerSubmission> submissions = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            UpdateResultRequest.AnswerSubmission a = new UpdateResultRequest.AnswerSubmission();
            a.setQuestionNumber(i);
            a.setPoints(i == 4 ? 4 : 3);
            submissions.add(a);
        }
        req.setAnswers(submissions);

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> resultService.updateResult(1L, req));
        assertTrue(exception.getMessage().contains("0, 1, 2, 3 oder 5"));
        verify(resultRepository, never()).save(any(Result.class));
    }

    private List<ResultAnswer> buildResultAnswersWithPoints(int points) {
        List<ResultAnswer> answers = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            ResultAnswer ra = new ResultAnswer();
            ra.setQuestionNumber(i);
            ra.setPoints(points);
            ra.setChanged(false);
            answers.add(ra);
        }
        return answers;
    }
}
