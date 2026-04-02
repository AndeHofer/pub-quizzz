package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that Hint entities (hintOrder, imageUrlAtStart, imageUrlAsHint, hintText) are persisted and
 * loaded correctly — the core of today's entity model change.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class HintPersistenceTest {

    @Autowired
    private QuizRepository quizRepository;

    /** Build a minimal 8-question quiz using the given hints for question 1. */
    private Quiz quizWithHintsOnFirstQuestion(List<Hint> q1hints) {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.of(2099, 1, 1));
        quiz.setSubmitDate(LocalDate.now());

        quiz.addQuestion(1, "Q1", "A1", "", q1hints);
        quiz.addQuestion(2, "Q2", "A2", "", textHints("h1", "h2", "h3", "h4"));
        quiz.addQuestion(3, "Q3", "A3", "", textHints("h1", "h2", "h3", "h4"));
        quiz.addQuestion(4, "Q4", "A4", "", textHints("h1", "h2", "h3", "h4"));
        quiz.addQuestion(5, "Q5", "A5", "", textHints("h1", "h2", "h3"));
        quiz.addQuestion(6, "Q6", "A6", "", textHints("h1", "h2", "h3"));
        quiz.addQuestion(7, "Q7", "A7", "", textHints("h1", "h2", "h3"));
        quiz.addQuestion(8, "Q8", "A8", "", textHints("h1", "h2", "h3"));
        return quiz;
    }

    private static List<Hint> textHints(String... texts) {
        return java.util.Arrays.stream(texts).map(t -> {
            Hint h = new Hint();
            h.setHintText(t);
            return h;
        }).toList();
    }

    @Test
    void hintsAreSavedWithCorrectOrder() {
        List<Hint> hints = textHints("first", "second", "third", "fourth");
        Quiz quiz = quizWithHintsOnFirstQuestion(hints);
        quizRepository.save(quiz);

        Quiz loaded = quizRepository.findById(quiz.getQuizId()).orElseThrow();
        List<Hint> savedHints = loaded.getQuestions().get(0).getHints();

        assertEquals(4, savedHints.size());
        assertEquals(1, savedHints.get(0).getHintOrder());
        assertEquals(2, savedHints.get(1).getHintOrder());
        assertEquals(3, savedHints.get(2).getHintOrder());
        assertEquals(4, savedHints.get(3).getHintOrder());
    }

    @Test
    void hintTextIsPersistedCorrectly() {
        List<Hint> hints = textHints("alpha", "beta", "gamma", "delta");
        Quiz quiz = quizWithHintsOnFirstQuestion(hints);
        quizRepository.save(quiz);

        Quiz loaded = quizRepository.findById(quiz.getQuizId()).orElseThrow();
        List<Hint> savedHints = loaded.getQuestions().get(0).getHints();

        assertEquals("alpha", savedHints.get(0).getHintText());
        assertEquals("delta", savedHints.get(3).getHintText());
    }

    @Test
    void imageUrlAtStartIsPersistedWhenSet() {
        Hint h1 = new Hint();
        h1.setHintText("a clue");
        h1.setImageUrlAtStart("/uploads/start-image.jpg");

        Hint h2 = new Hint(); h2.setHintText("h2");
        Hint h3 = new Hint(); h3.setHintText("h3");
        Hint h4 = new Hint(); h4.setHintText("h4");

        Quiz quiz = quizWithHintsOnFirstQuestion(List.of(h1, h2, h3, h4));
        quizRepository.save(quiz);

        Quiz loaded = quizRepository.findById(quiz.getQuizId()).orElseThrow();
        Hint savedFirstHint = loaded.getQuestions().get(0).getHints().get(0);

        assertEquals("/uploads/start-image.jpg", savedFirstHint.getImageUrlAtStart());
        assertNull(savedFirstHint.getImageUrlAsHint());
        assertEquals("a clue", savedFirstHint.getHintText());
    }

    @Test
    void imageUrlAsHintIsPersistedWhenSet() {
        Hint h1 = new Hint();
        h1.setHintText("a clue");
        h1.setImageUrlAsHint("/uploads/hint-image.png");

        Hint h2 = new Hint();
        h2.setHintText("h2");
        Hint h3 = new Hint();
        h3.setHintText("h3");
        Hint h4 = new Hint();
        h4.setHintText("h4");

        Quiz quiz = quizWithHintsOnFirstQuestion(List.of(h1, h2, h3, h4));
        quizRepository.save(quiz);

        Quiz loaded = quizRepository.findById(quiz.getQuizId()).orElseThrow();
        Hint savedFirstHint = loaded.getQuestions().get(0).getHints().get(0);

        assertNull(savedFirstHint.getImageUrlAtStart());
        assertEquals("/uploads/hint-image.png", savedFirstHint.getImageUrlAsHint());
    }

    @Test
    void bothImageUrlsAreNullWhenNotSet() {
        List<Hint> hints = textHints("only text", "h2", "h3", "h4");
        Quiz quiz = quizWithHintsOnFirstQuestion(hints);
        quizRepository.save(quiz);

        Quiz loaded = quizRepository.findById(quiz.getQuizId()).orElseThrow();
        Hint savedFirstHint = loaded.getQuestions().get(0).getHints().get(0);
        assertNull(savedFirstHint.getImageUrlAtStart());
        assertNull(savedFirstHint.getImageUrlAsHint());
    }

    @Test
    void bothImageUrlsCanBeSetOnSameHint() {
        Hint h1 = new Hint();
        h1.setHintText("double image");
        h1.setImageUrlAtStart("/uploads/start.jpg");
        h1.setImageUrlAsHint("/uploads/hint.jpg");

        Hint h2 = new Hint();
        h2.setHintText("h2");
        Hint h3 = new Hint();
        h3.setHintText("h3");
        Hint h4 = new Hint();
        h4.setHintText("h4");

        Quiz quiz = quizWithHintsOnFirstQuestion(List.of(h1, h2, h3, h4));
        quizRepository.save(quiz);

        Quiz loaded = quizRepository.findById(quiz.getQuizId()).orElseThrow();
        Hint saved = loaded.getQuestions().get(0).getHints().get(0);

        assertEquals("/uploads/start.jpg", saved.getImageUrlAtStart());
        assertEquals("/uploads/hint.jpg", saved.getImageUrlAsHint());
    }

    @Test
    void hintWithNullTextAndImageAsHintIsPersisted() {
        Hint h1 = new Hint();
        h1.setHintText(null);  // null is now allowed
        h1.setImageUrlAsHint("/uploads/hint-only.jpg");

        Hint h2 = new Hint(); h2.setHintText("h2");
        Hint h3 = new Hint(); h3.setHintText("h3");
        Hint h4 = new Hint(); h4.setHintText("h4");

        Quiz quiz = quizWithHintsOnFirstQuestion(List.of(h1, h2, h3, h4));
        quizRepository.save(quiz);

        Quiz loaded = quizRepository.findById(quiz.getQuizId()).orElseThrow();
        Hint saved = loaded.getQuestions().get(0).getHints().get(0);

        assertNull(saved.getHintText());
        assertEquals("/uploads/hint-only.jpg", saved.getImageUrlAsHint());
    }
}
