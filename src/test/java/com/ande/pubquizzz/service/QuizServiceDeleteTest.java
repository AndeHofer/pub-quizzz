package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.mapper.QuizMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Mock
    private DocumentStorageService documentStorageService;

    @InjectMocks
    private QuizService quizService;

    @Test
    void deleteQuiz_deletesResultsBeforeQuiz() {
        Quiz quiz = quizWithoutImages();
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        boolean result = quizService.deleteQuiz(1L);

        assertTrue(result);
        InOrder order = inOrder(resultRepository, quizRepository);
        order.verify(resultRepository).deleteByQuizQuizId(1L);
        order.verify(quizRepository).deleteById(1L);
    }

    @Test
    void deleteQuiz_notFound_returnsFalse() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = quizService.deleteQuiz(99L);

        assertFalse(result);
        verifyNoInteractions(resultRepository);
        verify(quizRepository, never()).deleteById(any());
    }

    @Test
    void deleteQuiz_deletesAllImageFiles() {
        Quiz quiz = quizWithImages("/uploads/start.jpg", "/uploads/hint.png");
        when(quizRepository.findById(2L)).thenReturn(Optional.of(quiz));

        quizService.deleteQuiz(2L);

        verify(imageStorageService).delete("/uploads/start.jpg");
        verify(imageStorageService).delete("/uploads/hint.png");
    }

    @Test
    void deleteQuiz_noImages_doesNotCallImageDelete() {
        Quiz quiz = quizWithoutImages();
        when(quizRepository.findById(3L)).thenReturn(Optional.of(quiz));

        quizService.deleteQuiz(3L);

        verify(imageStorageService, never()).delete(any());
    }

    @Test
    void deleteQuiz_deletesDocuments() {
        Quiz quiz = quizWithoutImages();
        when(quizRepository.findById(5L)).thenReturn(Optional.of(quiz));

        quizService.deleteQuiz(5L);

        verify(documentStorageService).deleteAllDocumentsForQuiz(5L);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Quiz quizWithoutImages() {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.now());
        quiz.setSubmitDate(LocalDate.now());
        Hint h = new Hint();
        h.setHintText("text");
        quiz.addQuestion(1, "Q1", "A1", "", List.of(h, hint(), hint(), hint()));
        for (int i = 2; i <= 4; i++) quiz.addQuestion(i, "Q" + i, "A" + i, "", textHints(4));
        for (int i = 5; i <= 8; i++) quiz.addQuestion(i, "Q" + i, "A" + i, "", textHints(3));
        return quiz;
    }

    private Quiz quizWithImages(String atStart, String asHint) {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.now());
        quiz.setSubmitDate(LocalDate.now());
        Hint h = new Hint();
        h.setHintText("text");
        h.setImageUrlAtStart(atStart);
        h.setImageUrlAsHint(asHint);
        quiz.addQuestion(1, "Q1", "A1", "", List.of(h, hint(), hint(), hint()));
        for (int i = 2; i <= 4; i++) quiz.addQuestion(i, "Q" + i, "A" + i, "", textHints(4));
        for (int i = 5; i <= 8; i++) quiz.addQuestion(i, "Q" + i, "A" + i, "", textHints(3));
        return quiz;
    }

    private static Hint hint() {
        Hint h = new Hint();
        h.setHintText("h");
        return h;
    }

    private static List<Hint> textHints(int count) {
        List<Hint> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(hint());
        return list;
    }
}
