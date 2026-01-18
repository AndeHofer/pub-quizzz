package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DatabaseTest {

    @Autowired
    private QuizRepository quizRepository;

    @Test
    public void testCreateFictionalQuestion() {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.of(2024, 1, 7));

        quiz.addQuestion(1, "What is the capital of France?", "Paris", "Easy question",
                Arrays.asList("It's in Europe", "It's a city of lights", "Eiffel Tower is there", "It starts with P"));

        quiz.addQuestion(2, "Who painted the Mona Lisa?", "Leonardo da Vinci", "Famous artist",
                Arrays.asList("Italian artist", "Renaissance period", "Also invented things", "First name Leonardo"));

        quiz.addQuestion(3, "What is 2+2?", "4", "Math question",
                Arrays.asList("It's a number", "Less than 10", "Even number", "Between 3 and 5"));

        quiz.addQuestion(4, "What is H2O?", "Water", "Science question",
                Arrays.asList("Chemical compound", "Essential for life", "Liquid at room temperature", "You drink it"));

        quiz.addQuestion(5, "What year did WWII end?", "1945", "History question",
                Arrays.asList("20th century", "1940s", "After 1944"));

        quiz.addQuestion(6, "What is the largest planet?", "Jupiter", "Astronomy question",
                Arrays.asList("In our solar system", "Gas giant", "Has a Great Red Spot"));

        quiz.addQuestion(7, "Who wrote Romeo and Juliet?", "William Shakespeare", "Literature question",
                Arrays.asList("English playwright", "Elizabethan era", "Also wrote Hamlet"));

        quiz.addQuestion(8, "What is the speed of light?", "299,792,458 m/s", "Physics question",
                Arrays.asList("Very fast", "In vacuum", "Approximately 300,000 km/s"));

        // Set submitDate and save directly via repository
        quiz.setSubmitDate(LocalDate.now());
        quizRepository.save(quiz);

        // Verify the quiz was saved
        Quiz savedQuiz = quizRepository.findById(quiz.getQuizId()).orElse(null);
        assertNotNull(savedQuiz);
        assertEquals(8, savedQuiz.getQuestions().size());
        assertEquals("What is the capital of France?", savedQuiz.getQuestions().getFirst().getQuestion());
        assertEquals("Paris", savedQuiz.getQuestions().getFirst().getAnswer());
        assertEquals(LocalDate.now(), savedQuiz.getSubmitDate());
    }

    @Test
    public void testCreateFictionalQuestion2() {
        // Get initial count of quizzes in database
        long initialCount = quizRepository.count();

        // Create a new quiz based on existing data pattern
        Quiz newQuiz = new Quiz();
        newQuiz.setPubDate(LocalDate.of(2025, 1, 7));

        newQuiz.addQuestion(1, "What is the capital of Germany?", "Berlin", "Geography question",
                Arrays.asList("It's in Europe", "It's the largest city in Germany", "Brandenburg Gate is there", "It starts with B"));

        newQuiz.addQuestion(2, "Who wrote '1984'?", "George Orwell", "Literature question",
                Arrays.asList("British author", "Also wrote Animal Farm", "Real name was Eric Blair", "Pen name George"));

        newQuiz.addQuestion(3, "What is 10 * 10?", "100", "Math question",
                Arrays.asList("It's a number", "Perfect square", "Three digits", "Between 50 and 150"));

        newQuiz.addQuestion(4, "What is CO2?", "Carbon Dioxide", "Science question",
                Arrays.asList("Chemical compound", "Greenhouse gas", "Plants use it", "Carbon and oxygen"));

        newQuiz.addQuestion(5, "What year did WWI start?", "1914", "History question",
                Arrays.asList("20th century", "1910s", "Before 1920"));

        newQuiz.addQuestion(6, "What is the smallest planet?", "Mercury", "Astronomy question",
                Arrays.asList("In our solar system", "Rocky planet", "Closest to the sun"));

        newQuiz.addQuestion(7, "Who wrote Hamlet?", "William Shakespeare", "Literature question",
                Arrays.asList("English playwright", "Elizabethan era", "Also wrote Romeo and Juliet"));

        newQuiz.addQuestion(8, "What is the boiling point of water?", "100°C", "Physics question",
                Arrays.asList("In Celsius", "At sea level", "Three digits"));

        // Set submitDate and save directly via repository
        newQuiz.setSubmitDate(LocalDate.now());
        quizRepository.save(newQuiz);

        // Verify the new quiz was saved
        long finalCount = quizRepository.count();
        assertEquals(initialCount + 1, finalCount, "Quiz count should increase by 1");

        Quiz savedQuiz = quizRepository.findById(newQuiz.getQuizId()).orElse(null);
        assertNotNull(savedQuiz, "Saved quiz should not be null");
        assertEquals(8, savedQuiz.getQuestions().size());
        assertEquals("What is the capital of Germany?", savedQuiz.getQuestions().get(0).getQuestion());
        assertEquals("Berlin", savedQuiz.getQuestions().get(0).getAnswer());
        assertEquals("George Orwell", savedQuiz.getQuestions().get(1).getAnswer());
        assertEquals("100", savedQuiz.getQuestions().get(2).getAnswer());
        assertNotNull(savedQuiz.getSubmitDate(), "Submit date should be set automatically");
        assertEquals(LocalDate.now(), savedQuiz.getSubmitDate());
    }

    @Test
    public void testCreateRealQuestion() {
        // Get initial count of quizzes in database
        long initialCount = quizRepository.count();

        // Create a quiz based on the data from QUESTIONS.sql
        Quiz sqlQuiz = new Quiz();
        sqlQuiz.setPubDate(LocalDate.of(2026, 1, 7));

        sqlQuiz.addQuestion(1, "Academy Award für den besten Schnitt. Die spielenhöchstens in 8mm-Filmen.",
                "Spielte eine Rolle mit Glatze", "",
                Arrays.asList("Kate Blanchet", "Sigourney Weaver", "Demi Moore", "Charlize Theron"));

        sqlQuiz.addQuestion(2, "Beim Wiener Slang verstehe ich immer nur Bahnhof. (Oder eben ganz was anderes.)",
                "UR kann vor die Wörter gesetzt werden", "",
                Arrays.asList("Zeit", "Anus", "Teil", "Großeltern"));

        sqlQuiz.addQuestion(3, "Wer bin ich? Ein Niemand. Ein Unausprechlicher. Ein Sklave (laut eigener Aussage)",
                "Prinz (Symbol)", "",
                Arrays.asList("Kreis", "Bindestrich", "Pfeil nach unten", "eine Art Chamälionschwanz (nach links weg)"));

        sqlQuiz.addQuestion(4, "Klingt hart. Im Radio-Edit vor allem wegen der Übergänge beliebt.",
                "Interpret hat einen Namen passend zur Ordnungszahl der Elemente", "",
                Arrays.asList("Rockstar 28 ", "The Great Pretender 80", "Das Beste 47", "Fear of the Dark 26"));

        sqlQuiz.addQuestion(5, "Arkanes Wissen aufdecken: Wer oder was ist die Große Unbekannte?",
                "?", "Tarot-Karten",
                Arrays.asList("Keoprata III", "Napoleon IV", "Pius V"));

        sqlQuiz.addQuestion(6, "Varus, Varus, hast du dich da verrechnent?",
                "eins = 1", "zählen der römischen \"Zahlen\" im Wort",
                Arrays.asList("vier = 6", "drei = 1", "zwei = 1"));

        sqlQuiz.addQuestion(7, "Ignorant und/oder Charmeur? Überall Fünfer (außer in Musik)",
                "Französich", "Liedtest Wonderful Day?",
                Arrays.asList("Geschichte", "Biologie", "Ein naturwissenschaftliches Lehrbuch"));

        sqlQuiz.addQuestion(8, "Kling(!) wohl so verkehrt weil du die Leseliste Marsupialia überlässt.",
                "Die Chroniken von Narnia", "Känguru Bücher ",
                Arrays.asList("Das Judas Evangelium (z.B.)", "Das Johannes Evagelium", "Dogma 95"));

        // Set submitDate and save directly via repository
        sqlQuiz.setSubmitDate(LocalDate.now());
        quizRepository.save(sqlQuiz);

        // Verify the new quiz was saved
        long finalCount = quizRepository.count();
        assertEquals(initialCount + 1, finalCount, "Quiz count should increase by 1");

        Quiz savedQuiz = quizRepository.findById(sqlQuiz.getQuizId()).orElse(null);
        assertNotNull(savedQuiz, "Saved quiz should not be null");
        assertEquals(8, savedQuiz.getQuestions().size());
        assertEquals("Academy Award für den besten Schnitt. Die spielenhöchstens in 8mm-Filmen.", savedQuiz.getQuestions().get(0).getQuestion());
        assertEquals("Spielte eine Rolle mit Glatze", savedQuiz.getQuestions().get(0).getAnswer());
        assertEquals("UR kann vor die Wörter gesetzt werden", savedQuiz.getQuestions().get(1).getAnswer());
        assertEquals("Prinz (Symbol)", savedQuiz.getQuestions().get(2).getAnswer());
        assertEquals(LocalDate.of(2026, 1, 7), savedQuiz.getPubDate());
    }
}
