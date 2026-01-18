package com.ande.pubquizzz;

import com.ande.pubquizzz.controller.AdminController;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.repositories.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DatabaseTest {

    @Autowired
    private AdminController adminController;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    public void testCreateFictionalQuestion() {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.of(2024, 1, 7));
        quiz.setQuestion1("What is the capital of France?");
        quiz.setHint1_1("It's in Europe");
        quiz.setHint1_2("It's a city of lights");
        quiz.setHint1_3("Eiffel Tower is there");
        quiz.setHint1_4("It starts with P");
        quiz.setAnswer1("Paris");
        quiz.setNote1("Easy question");

        quiz.setQuestion2("Who painted the Mona Lisa?");
        quiz.setHint2_1("Italian artist");
        quiz.setHint2_2("Renaissance period");
        quiz.setHint2_3("Also invented things");
        quiz.setHint2_4("First name Leonardo");
        quiz.setAnswer2("Leonardo da Vinci");
        quiz.setNote2("Famous artist");

        quiz.setQuestion3("What is 2+2?");
        quiz.setHint3_1("It's a number");
        quiz.setHint3_2("Less than 10");
        quiz.setHint3_3("Even number");
        quiz.setHint3_4("Between 3 and 5");
        quiz.setAnswer3("4");
        quiz.setNote3("Math question");

        quiz.setQuestion4("What is H2O?");
        quiz.setHint4_1("Chemical compound");
        quiz.setHint4_2("Essential for life");
        quiz.setHint4_3("Liquid at room temperature");
        quiz.setHint4_4("You drink it");
        quiz.setAnswer4("Water");
        quiz.setNote4("Science question");

        quiz.setQuestion5("What year did WWII end?");
        quiz.setHint5_1("20th century");
        quiz.setHint5_2("1940s");
        quiz.setHint5_3("After 1944");
        quiz.setAnswer5("1945");
        quiz.setNote5("History question");

        quiz.setQuestion6("What is the largest planet?");
        quiz.setHint6_1("In our solar system");
        quiz.setHint6_2("Gas giant");
        quiz.setHint6_3("Has a Great Red Spot");
        quiz.setAnswer6("Jupiter");
        quiz.setNote6("Astronomy question");

        quiz.setQuestion7("Who wrote Romeo and Juliet?");
        quiz.setHint7_1("English playwright");
        quiz.setHint7_2("Elizabethan era");
        quiz.setHint7_3("Also wrote Hamlet");
        quiz.setAnswer7("William Shakespeare");
        quiz.setNote7("Literature question");

        quiz.setQuestion8("What is the speed of light?");
        quiz.setHint8_1("Very fast");
        quiz.setHint8_2("In vacuum");
        quiz.setHint8_3("Approximately 300,000 km/s");
        quiz.setAnswer8("299,792,458 m/s");
        quiz.setNote8("Physics question");

        // Test that submitDate is set automatically when null
        assertNull(quiz.getSubmitDate());

        adminController.createQuestion(quiz);

        // Verify the question was saved
        Quiz savedQuiz = questionRepository.findById(quiz.getQuizId()).orElse(null);
        assertNotNull(savedQuiz);
        assertEquals("What is the capital of France?", savedQuiz.getQuestion1());
        assertEquals("Paris", savedQuiz.getAnswer1());
        assertEquals(LocalDate.now(), savedQuiz.getSubmitDate());
    }

    @Test
    public void testCreateFictionalQuestion2() {
        // Get initial count of questions in database
        long initialCount = questionRepository.count();

        // Create a new question based on existing data pattern
        Quiz newQuiz = new Quiz();
        newQuiz.setPubDate(LocalDate.of(2025, 1, 7));
        newQuiz.setQuestion1("What is the capital of Germany?");
        newQuiz.setHint1_1("It's in Europe");
        newQuiz.setHint1_2("It's the largest city in Germany");
        newQuiz.setHint1_3("Brandenburg Gate is there");
        newQuiz.setHint1_4("It starts with B");
        newQuiz.setAnswer1("Berlin");
        newQuiz.setNote1("Geography question");

        newQuiz.setQuestion2("Who wrote '1984'?");
        newQuiz.setHint2_1("British author");
        newQuiz.setHint2_2("Also wrote Animal Farm");
        newQuiz.setHint2_3("Real name was Eric Blair");
        newQuiz.setHint2_4("Pen name George");
        newQuiz.setAnswer2("George Orwell");
        newQuiz.setNote2("Literature question");

        newQuiz.setQuestion3("What is 10 * 10?");
        newQuiz.setHint3_1("It's a number");
        newQuiz.setHint3_2("Perfect square");
        newQuiz.setHint3_3("Three digits");
        newQuiz.setHint3_4("Between 50 and 150");
        newQuiz.setAnswer3("100");
        newQuiz.setNote3("Math question");

        newQuiz.setQuestion4("What is CO2?");
        newQuiz.setHint4_1("Chemical compound");
        newQuiz.setHint4_2("Greenhouse gas");
        newQuiz.setHint4_3("Plants use it");
        newQuiz.setHint4_4("Carbon and oxygen");
        newQuiz.setAnswer4("Carbon Dioxide");
        newQuiz.setNote4("Science question");

        newQuiz.setQuestion5("What year did WWI start?");
        newQuiz.setHint5_1("20th century");
        newQuiz.setHint5_2("1910s");
        newQuiz.setHint5_3("Before 1920");
        newQuiz.setAnswer5("1914");
        newQuiz.setNote5("History question");

        newQuiz.setQuestion6("What is the smallest planet?");
        newQuiz.setHint6_1("In our solar system");
        newQuiz.setHint6_2("Rocky planet");
        newQuiz.setHint6_3("Closest to the sun");
        newQuiz.setAnswer6("Mercury");
        newQuiz.setNote6("Astronomy question");

        newQuiz.setQuestion7("Who wrote Hamlet?");
        newQuiz.setHint7_1("English playwright");
        newQuiz.setHint7_2("Elizabethan era");
        newQuiz.setHint7_3("Also wrote Romeo and Juliet");
        newQuiz.setAnswer7("William Shakespeare");
        newQuiz.setNote7("Literature question");

        newQuiz.setQuestion8("What is the boiling point of water?");
        newQuiz.setHint8_1("In Celsius");
        newQuiz.setHint8_2("At sea level");
        newQuiz.setHint8_3("Three digits");
        newQuiz.setAnswer8("100°C");
        newQuiz.setNote8("Physics question");

        // Call createQuestion through AdminController
        adminController.createQuestion(newQuiz);

        // Verify the new question was saved
        long finalCount = questionRepository.count();
        assertEquals(initialCount + 1, finalCount, "Question count should increase by 1");

        Quiz savedQuiz = questionRepository.findById(newQuiz.getQuizId()).orElse(null);
        assertNotNull(savedQuiz, "Saved question should not be null");
        assertEquals("What is the capital of Germany?", savedQuiz.getQuestion1());
        assertEquals("Berlin", savedQuiz.getAnswer1());
        assertEquals("George Orwell", savedQuiz.getAnswer2());
        assertEquals("100", savedQuiz.getAnswer3());
        assertNotNull(savedQuiz.getSubmitDate(), "Submit date should be set automatically");
        assertEquals(LocalDate.now(), savedQuiz.getSubmitDate());
    }

    @Test
    public void testCreateRealQuestion() {
        // Get initial count of questions in database
        long initialCount = questionRepository.count();

        // Create a question based on the data from QUESTIONS.sql
        Quiz sqlQuiz = new Quiz();
        sqlQuiz.setPubDate(LocalDate.of(2026, 1, 7));

        sqlQuiz.setQuestion1("Academy Award für den besten Schnitt. Die spielenhöchstens in 8mm-Filmen.");
        sqlQuiz.setHint1_1("Kate Blanchet");
        sqlQuiz.setHint1_2("Sigourney Weaver");
        sqlQuiz.setHint1_3("Demi Moore");
        sqlQuiz.setHint1_4("Charlize Theron");
        sqlQuiz.setAnswer1("Spielte eine Rolle mit Glatze");
        sqlQuiz.setNote1("");

        sqlQuiz.setQuestion2("Beim Wiener Slang verstehe ich immer nur Bahnhof. (Oder eben ganz was anderes.)");
        sqlQuiz.setHint2_1("Zeit");
        sqlQuiz.setHint2_2("Anus");
        sqlQuiz.setHint2_3("Teil");
        sqlQuiz.setHint2_4("Großeltern");
        sqlQuiz.setAnswer2("UR kann vor die Wörter gesetzt werden");
        sqlQuiz.setNote2("");

        sqlQuiz.setQuestion3("Wer bin ich? Ein Niemand. Ein Unausprechlicher. Ein Sklave (laut eigener Aussage)");
        sqlQuiz.setHint3_1("Kreis");
        sqlQuiz.setHint3_2("Bindestrich");
        sqlQuiz.setHint3_3("Pfeil nach unten");
        sqlQuiz.setHint3_4("eine Art Chamälionschwanz (nach links weg)");
        sqlQuiz.setAnswer3("Prinz (Symbol)");
        sqlQuiz.setNote3("");

        sqlQuiz.setQuestion4("Klingt hart. Im Radio-Edit vor allem wegen der Übergänge beliebt.");
        sqlQuiz.setHint4_1("Rockstar 28 ");
        sqlQuiz.setHint4_2("The Great Pretender 80");
        sqlQuiz.setHint4_3("Das Beste 47");
        sqlQuiz.setHint4_4("Fear of the Dark 26");
        sqlQuiz.setAnswer4("Interpret hat einen Namen passend zur Ordnungszahl der Elemente");
        sqlQuiz.setNote4("");

        sqlQuiz.setQuestion5("Arkanes Wissen aufdecken: Wer oder was ist die Große Unbekannte?");
        sqlQuiz.setHint5_1("Keoprata III");
        sqlQuiz.setHint5_2("Napoleon IV");
        sqlQuiz.setHint5_3("Pius V");
        sqlQuiz.setAnswer5("?");
        sqlQuiz.setNote5("Tarot-Karten");

        sqlQuiz.setQuestion6("Varus, Varus, hast du dich da verrechnent?");
        sqlQuiz.setHint6_1("vier = 6");
        sqlQuiz.setHint6_2("drei = 1");
        sqlQuiz.setHint6_3("zwei = 1");
        sqlQuiz.setAnswer6("eins = 1");
        sqlQuiz.setNote6("zählen der römischen \"Zahlen\" im Wort");

        sqlQuiz.setQuestion7("Ignorant und/oder Charmeur? Überall Fünfer (außer in Musik)");
        sqlQuiz.setHint7_1("Geschichte");
        sqlQuiz.setHint7_2("Biologie");
        sqlQuiz.setHint7_3("Ein naturwissenschaftliches Lehrbuch");
        sqlQuiz.setAnswer7("Französich");
        sqlQuiz.setNote7("Liedtest Wonderful Day?");

        sqlQuiz.setQuestion8("Kling(!) wohl so verkehrt weil du die Leseliste Marsupialia überlässt.");
        sqlQuiz.setHint8_1("Das Judas Evangelium (z.B.)");
        sqlQuiz.setHint8_2("Das Johannes Evagelium");
        sqlQuiz.setHint8_3("Dogma 95");
        sqlQuiz.setAnswer8("Die Chroniken von Narnia");
        sqlQuiz.setNote8("Känguru Bücher ");

        // Call createQuestion through AdminController
        adminController.createQuestion(sqlQuiz);

        // Verify the new question was saved
        long finalCount = questionRepository.count();
        assertEquals(initialCount + 1, finalCount, "Question count should increase by 1");

        Quiz savedQuiz = questionRepository.findById(sqlQuiz.getQuizId()).orElse(null);
        assertNotNull(savedQuiz, "Saved question should not be null");
        assertEquals("Academy Award für den besten Schnitt. Die spielenhöchstens in 8mm-Filmen.", savedQuiz.getQuestion1());
        assertEquals("Spielte eine Rolle mit Glatze", savedQuiz.getAnswer1());
        assertEquals("UR kann vor die Wörter gesetzt werden", savedQuiz.getAnswer2());
        assertEquals("Prinz (Symbol)", savedQuiz.getAnswer3());
        assertEquals(LocalDate.of(2026, 1, 7), savedQuiz.getPubDate());
    }
}
