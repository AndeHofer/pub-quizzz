package com.ande.pubquizzz;

import com.ande.pubquizzz.controller.AdminController;
import com.ande.pubquizzz.database.entities.Question;
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
    public void testCreateQuestion() {
        Question question = new Question();
        question.setPubDate(LocalDate.of(2024, 1, 7));
        question.setQuestion1("What is the capital of France?");
        question.setHint1_1("It's in Europe");
        question.setHint1_2("It's a city of lights");
        question.setHint1_3("Eiffel Tower is there");
        question.setHint1_4("It starts with P");
        question.setAnswer1("Paris");
        question.setNote1("Easy question");

        question.setQuestion2("Who painted the Mona Lisa?");
        question.setHint2_1("Italian artist");
        question.setHint2_2("Renaissance period");
        question.setHint2_3("Also invented things");
        question.setHint2_4("First name Leonardo");
        question.setAnswer2("Leonardo da Vinci");
        question.setNote2("Famous artist");

        question.setQuestion3("What is 2+2?");
        question.setHint3_1("It's a number");
        question.setHint3_2("Less than 10");
        question.setHint3_3("Even number");
        question.setHint3_4("Between 3 and 5");
        question.setAnswer3("4");
        question.setNote3("Math question");

        question.setQuestion4("What is H2O?");
        question.setHint4_1("Chemical compound");
        question.setHint4_2("Essential for life");
        question.setHint4_3("Liquid at room temperature");
        question.setHint4_4("You drink it");
        question.setAnswer4("Water");
        question.setNote4("Science question");

        question.setQuestion5("What year did WWII end?");
        question.setHint5_1("20th century");
        question.setHint5_2("1940s");
        question.setHint5_3("After 1944");
        question.setAnswer5("1945");
        question.setNote5("History question");

        question.setQuestion6("What is the largest planet?");
        question.setHint6_1("In our solar system");
        question.setHint6_2("Gas giant");
        question.setHint6_3("Has a Great Red Spot");
        question.setAnswer6("Jupiter");
        question.setNote6("Astronomy question");

        question.setQuestion7("Who wrote Romeo and Juliet?");
        question.setHint7_1("English playwright");
        question.setHint7_2("Elizabethan era");
        question.setHint7_3("Also wrote Hamlet");
        question.setAnswer7("William Shakespeare");
        question.setNote7("Literature question");

        question.setQuestion8("What is the speed of light?");
        question.setHint8_1("Very fast");
        question.setHint8_2("In vacuum");
        question.setHint8_3("Approximately 300,000 km/s");
        question.setAnswer8("299,792,458 m/s");
        question.setNote8("Physics question");

        // Test that submitDate is set automatically when null
        assertNull(question.getSubmitDate());

        adminController.createQuestion(question);

        // Verify the question was saved
        Question savedQuestion = questionRepository.findById(question.getQuestionId()).orElse(null);
        assertNotNull(savedQuestion);
        assertEquals("What is the capital of France?", savedQuestion.getQuestion1());
        assertEquals("Paris", savedQuestion.getAnswer1());
        assertEquals(LocalDate.now(), savedQuestion.getSubmitDate());
    }

    @Test
    public void testCreateQuestionWithExistingDatabaseData() {
        // Get initial count of questions in database
        long initialCount = questionRepository.count();

        // Create a new question based on existing data pattern
        Question newQuestion = new Question();
        newQuestion.setPubDate(LocalDate.of(2025, 1, 7));
        newQuestion.setQuestion1("What is the capital of Germany?");
        newQuestion.setHint1_1("It's in Europe");
        newQuestion.setHint1_2("It's the largest city in Germany");
        newQuestion.setHint1_3("Brandenburg Gate is there");
        newQuestion.setHint1_4("It starts with B");
        newQuestion.setAnswer1("Berlin");
        newQuestion.setNote1("Geography question");

        newQuestion.setQuestion2("Who wrote '1984'?");
        newQuestion.setHint2_1("British author");
        newQuestion.setHint2_2("Also wrote Animal Farm");
        newQuestion.setHint2_3("Real name was Eric Blair");
        newQuestion.setHint2_4("Pen name George");
        newQuestion.setAnswer2("George Orwell");
        newQuestion.setNote2("Literature question");

        newQuestion.setQuestion3("What is 10 * 10?");
        newQuestion.setHint3_1("It's a number");
        newQuestion.setHint3_2("Perfect square");
        newQuestion.setHint3_3("Three digits");
        newQuestion.setHint3_4("Between 50 and 150");
        newQuestion.setAnswer3("100");
        newQuestion.setNote3("Math question");

        newQuestion.setQuestion4("What is CO2?");
        newQuestion.setHint4_1("Chemical compound");
        newQuestion.setHint4_2("Greenhouse gas");
        newQuestion.setHint4_3("Plants use it");
        newQuestion.setHint4_4("Carbon and oxygen");
        newQuestion.setAnswer4("Carbon Dioxide");
        newQuestion.setNote4("Science question");

        newQuestion.setQuestion5("What year did WWI start?");
        newQuestion.setHint5_1("20th century");
        newQuestion.setHint5_2("1910s");
        newQuestion.setHint5_3("Before 1920");
        newQuestion.setAnswer5("1914");
        newQuestion.setNote5("History question");

        newQuestion.setQuestion6("What is the smallest planet?");
        newQuestion.setHint6_1("In our solar system");
        newQuestion.setHint6_2("Rocky planet");
        newQuestion.setHint6_3("Closest to the sun");
        newQuestion.setAnswer6("Mercury");
        newQuestion.setNote6("Astronomy question");

        newQuestion.setQuestion7("Who wrote Hamlet?");
        newQuestion.setHint7_1("English playwright");
        newQuestion.setHint7_2("Elizabethan era");
        newQuestion.setHint7_3("Also wrote Romeo and Juliet");
        newQuestion.setAnswer7("William Shakespeare");
        newQuestion.setNote7("Literature question");

        newQuestion.setQuestion8("What is the boiling point of water?");
        newQuestion.setHint8_1("In Celsius");
        newQuestion.setHint8_2("At sea level");
        newQuestion.setHint8_3("Three digits");
        newQuestion.setAnswer8("100°C");
        newQuestion.setNote8("Physics question");

        // Call createQuestion through AdminController
        adminController.createQuestion(newQuestion);

        // Verify the new question was saved
        long finalCount = questionRepository.count();
        assertEquals(initialCount + 1, finalCount, "Question count should increase by 1");

        Question savedQuestion = questionRepository.findById(newQuestion.getQuestionId()).orElse(null);
        assertNotNull(savedQuestion, "Saved question should not be null");
        assertEquals("What is the capital of Germany?", savedQuestion.getQuestion1());
        assertEquals("Berlin", savedQuestion.getAnswer1());
        assertEquals("George Orwell", savedQuestion.getAnswer2());
        assertEquals("100", savedQuestion.getAnswer3());
        assertNotNull(savedQuestion.getSubmitDate(), "Submit date should be set automatically");
        assertEquals(LocalDate.now(), savedQuestion.getSubmitDate());
    }

    @Test
    public void testCreateQuestionFromSqlData() {
        // Get initial count of questions in database
        long initialCount = questionRepository.count();

        // Create a question based on the data from QUESTIONS.sql
        Question sqlQuestion = new Question();
        sqlQuestion.setPubDate(LocalDate.of(2026, 1, 7));
        sqlQuestion.setSubmitDate(LocalDate.of(2026, 1, 16));

        sqlQuestion.setQuestion1("Academy Award für den besten Schnitt. Die spielenhöchstens in 8mm-Filmen.");
        sqlQuestion.setHint1_1("Kate Blanchet");
        sqlQuestion.setHint1_2("Sigourney Weaver");
        sqlQuestion.setHint1_3("Demi Moore");
        sqlQuestion.setHint1_4("Charlize Theron");
        sqlQuestion.setAnswer1("Spielte eine Rolle mit Glatze");
        sqlQuestion.setNote1("");

        sqlQuestion.setQuestion2("Beim Wiener Slang verstehe ich immer nur Bahnhof. (Oder eben ganz was anderes.)");
        sqlQuestion.setHint2_1("Zeit");
        sqlQuestion.setHint2_2("Anus");
        sqlQuestion.setHint2_3("Teil");
        sqlQuestion.setHint2_4("Großeltern");
        sqlQuestion.setAnswer2("UR kann vor die Wörter gesetzt werden");
        sqlQuestion.setNote2("");

        sqlQuestion.setQuestion3("Wer bin ich? Ein Niemand. Ein Unausprechlicher. Ein Sklave (laut eigener Aussage)");
        sqlQuestion.setHint3_1("Kreis");
        sqlQuestion.setHint3_2("Bindestrich");
        sqlQuestion.setHint3_3("Pfeil nach unten");
        sqlQuestion.setHint3_4("eine Art Chamälionschwanz (nach links weg)");
        sqlQuestion.setAnswer3("Prinz (Symbol)");
        sqlQuestion.setNote3("");

        sqlQuestion.setQuestion4("Klingt hart. Im Radio-Edit vor allem wegen der Übergänge beliebt.");
        sqlQuestion.setHint4_1("Rockstar 28 ");
        sqlQuestion.setHint4_2("The Great Pretender 80");
        sqlQuestion.setHint4_3("Das Beste 47");
        sqlQuestion.setHint4_4("Fear of the Dark 26");
        sqlQuestion.setAnswer4("Interpret hat einen Namen passend zur Ordnungszahl der Elemente");
        sqlQuestion.setNote4("");

        sqlQuestion.setQuestion5("Arkanes Wissen aufdecken: Wer oder was ist die Große Unbekannte?");
        sqlQuestion.setHint5_1("Keoprata III");
        sqlQuestion.setHint5_2("Napoleon IV");
        sqlQuestion.setHint5_3("Pius V");
        sqlQuestion.setAnswer5("?");
        sqlQuestion.setNote5("Tarot-Karten");

        sqlQuestion.setQuestion6("Varus, Varus, hast du dich da verrechnent?");
        sqlQuestion.setHint6_1("vier = 6");
        sqlQuestion.setHint6_2("drei = 1");
        sqlQuestion.setHint6_3("zwei = 1");
        sqlQuestion.setAnswer6("eins = 1");
        sqlQuestion.setNote6("zählen der römischen \"Zahlen\" im Wort");

        sqlQuestion.setQuestion7("Ignorant und/oder Charmeur? Überall Fünfer (außer in Musik)");
        sqlQuestion.setHint7_1("Geschichte");
        sqlQuestion.setHint7_2("Biologie");
        sqlQuestion.setHint7_3("Ein naturwissenschaftliches Lehrbuch");
        sqlQuestion.setAnswer7("Französich");
        sqlQuestion.setNote7("Liedtest Wonderful Day?");

        sqlQuestion.setQuestion8("Kling(!) wohl so verkehrt weil du die Leseliste Marsupialia überlässt.");
        sqlQuestion.setHint8_1("Das Judas Evangelium (z.B.)");
        sqlQuestion.setHint8_2("Das Johannes Evagelium");
        sqlQuestion.setHint8_3("Dogma 95");
        sqlQuestion.setAnswer8("Die Chroniken von Narnia");
        sqlQuestion.setNote8("Känguru Bücher ");

        // Call createQuestion through AdminController
        adminController.createQuestion(sqlQuestion);

        // Verify the new question was saved
        long finalCount = questionRepository.count();
        assertEquals(initialCount + 1, finalCount, "Question count should increase by 1");

        Question savedQuestion = questionRepository.findById(sqlQuestion.getQuestionId()).orElse(null);
        assertNotNull(savedQuestion, "Saved question should not be null");
        assertEquals("Academy Award für den besten Schnitt. Die spielenhöchstens in 8mm-Filmen.", savedQuestion.getQuestion1());
        assertEquals("Spielte eine Rolle mit Glatze", savedQuestion.getAnswer1());
        assertEquals("UR kann vor die Wörter gesetzt werden", savedQuestion.getAnswer2());
        assertEquals("Prinz (Symbol)", savedQuestion.getAnswer3());
        assertEquals(LocalDate.of(2026, 1, 7), savedQuestion.getPubDate());
        assertEquals(LocalDate.of(2026, 1, 16), savedQuestion.getSubmitDate());
    }
}
