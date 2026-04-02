package com.ande.pubquizzz.mapper;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuizFinishedCheckerTest {

    @Test
    void isFinished_fullyFilledQuiz_returnsTrue() {
        Quiz quiz = buildFullQuiz();
        assertTrue(QuizFinishedChecker.isFinished(quiz));
    }

    @Test
    void isFinished_blankQuestionText_returnsFalse() {
        Quiz quiz = buildFullQuiz();
        quiz.getQuestions().get(0).setQuestionText("");
        assertFalse(QuizFinishedChecker.isFinished(quiz));
    }

    @Test
    void isFinished_blankAnswer_returnsFalse() {
        Quiz quiz = buildFullQuiz();
        quiz.getQuestions().get(0).setAnswer("");
        assertFalse(QuizFinishedChecker.isFinished(quiz));
    }

    @Test
    void isFinished_blankHintTextAndNoImage_returnsFalse() {
        Quiz quiz = buildFullQuiz();
        quiz.getQuestions().get(0).getHints().get(0).setHintText("");
        // imageUrlAsHint is null by default
        assertFalse(QuizFinishedChecker.isFinished(quiz));
    }

    @Test
    void isFinished_nullHintTextButImageAsHintSet_returnsTrue() {
        Quiz quiz = buildFullQuiz();
        Hint hint = quiz.getQuestions().get(0).getHints().get(0);
        hint.setHintText(null);
        hint.setImageUrlAsHint("/uploads/hint.jpg");
        assertTrue(QuizFinishedChecker.isFinished(quiz));
    }

    @Test
    void isFinished_imageAtStartOnlyNoHintText_returnsFalse() {
        // imageUrlAtStart alone does NOT count — only imageUrlAsHint or hintText
        Quiz quiz = buildFullQuiz();
        Hint hint = quiz.getQuestions().get(0).getHints().get(0);
        hint.setHintText(null);
        hint.setImageUrlAtStart("/uploads/start.jpg");
        // imageUrlAsHint is still null
        assertFalse(QuizFinishedChecker.isFinished(quiz));
    }

    @Test
    void isFinished_fewerThan8Questions_returnsFalse() {
        Quiz quiz = buildFullQuiz();
        quiz.getQuestions().remove(0);
        assertFalse(QuizFinishedChecker.isFinished(quiz));
    }

    @Test
    void isFinished_emptyQuestionsList_returnsFalse() {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.now());
        assertFalse(QuizFinishedChecker.isFinished(quiz));
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private Quiz buildFullQuiz() {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.now());
        for (int i = 1; i <= 8; i++) {
            int hintCount = i <= 4 ? 4 : 3;
            List<Hint> hints = new ArrayList<>();
            for (int j = 1; j <= hintCount; j++) {
                Hint h = new Hint();
                h.setHintText("hint " + i + "." + j);
                hints.add(h);
            }
            quiz.addQuestion(i, "Question " + i, "Answer " + i, "", hints);
        }
        return quiz;
    }
}
