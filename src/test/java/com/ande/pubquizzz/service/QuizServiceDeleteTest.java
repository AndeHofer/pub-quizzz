package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.mapper.QuizMapper;
import com.ande.pubquizzz.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceDeleteTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private QuizMapper quizMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private QuizService quizService;

    @Test
    void deleteQuiz_deletesResultsBeforeQuiz() {
        when(quizRepository.existsById(1L)).thenReturn(true);

        boolean result = quizService.deleteQuiz(1L);

        assertTrue(result);
        InOrder order = inOrder(resultRepository, quizRepository);
        order.verify(resultRepository).deleteByQuizQuizId(1L);
        order.verify(quizRepository).deleteById(1L);
    }

    @Test
    void deleteQuiz_notFound_returnsFalse() {
        when(quizRepository.existsById(99L)).thenReturn(false);

        boolean result = quizService.deleteQuiz(99L);

        assertFalse(result);
        verifyNoInteractions(resultRepository);
        verify(quizRepository, never()).deleteById(any());
    }
}
