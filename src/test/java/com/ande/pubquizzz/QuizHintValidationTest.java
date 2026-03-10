package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Quiz.addQuestion() hint-count validation rules.
 * These run without Spring context — pure logic tests.
 */
public class QuizHintValidationTest {

    private static List<Hint> hints(int count) {
        Hint[] arr = new Hint[count];
        for (int i = 0; i < count; i++) {
            Hint h = new Hint();
            h.setHintText("hint " + (i + 1));
            arr[i] = h;
        }
        return Arrays.asList(arr);
    }

    private Quiz newQuiz() {
        Quiz q = new Quiz();
        q.setPubDate(LocalDate.now());
        return q;
    }

    // ── Questions 1-4 require exactly 4 hints ──────────────────────────────

    @Test
    void questions1to4AcceptExactlyFourHints() {
        Quiz quiz = newQuiz();
        assertDoesNotThrow(() -> quiz.addQuestion(1, "Q", "A", "", hints(4)));
        assertDoesNotThrow(() -> quiz.addQuestion(2, "Q", "A", "", hints(4)));
        assertDoesNotThrow(() -> quiz.addQuestion(3, "Q", "A", "", hints(4)));
        assertDoesNotThrow(() -> quiz.addQuestion(4, "Q", "A", "", hints(4)));
    }

    @Test
    void question1RejectsTooFewHints() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> newQuiz().addQuestion(1, "Q", "A", "", hints(3)));
        assertTrue(ex.getMessage().contains("4 hints"));
    }

    @Test
    void question1RejectsTooManyHints() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> newQuiz().addQuestion(1, "Q", "A", "", hints(5)));
        assertTrue(ex.getMessage().contains("4 hints"));
    }

    // ── Questions 5-8 require exactly 3 hints ──────────────────────────────

    @Test
    void questions5to8AcceptExactlyThreeHints() {
        Quiz quiz = newQuiz();
        quiz.addQuestion(1, "Q", "A", "", hints(4));
        quiz.addQuestion(2, "Q", "A", "", hints(4));
        quiz.addQuestion(3, "Q", "A", "", hints(4));
        quiz.addQuestion(4, "Q", "A", "", hints(4));

        assertDoesNotThrow(() -> quiz.addQuestion(5, "Q", "A", "", hints(3)));
        assertDoesNotThrow(() -> quiz.addQuestion(6, "Q", "A", "", hints(3)));
        assertDoesNotThrow(() -> quiz.addQuestion(7, "Q", "A", "", hints(3)));
        assertDoesNotThrow(() -> quiz.addQuestion(8, "Q", "A", "", hints(3)));
    }

    @Test
    void question5RejectsFourHints() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> newQuiz().addQuestion(5, "Q", "A", "", hints(4)));
        assertTrue(ex.getMessage().contains("3 hints"));
    }

    @Test
    void question5RejectsTwoHints() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> newQuiz().addQuestion(5, "Q", "A", "", hints(2)));
        assertTrue(ex.getMessage().contains("3 hints"));
    }

    // ── Out-of-range question numbers ──────────────────────────────────────

    @Test
    void questionNumberZeroIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> newQuiz().addQuestion(0, "Q", "A", "", hints(4)));
    }

    @Test
    void questionNumberNineIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> newQuiz().addQuestion(9, "Q", "A", "", hints(3)));
    }
}
