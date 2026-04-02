package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Manual data seeder — populates the local file-based H2 database with a realistic
 * base dataset for manual testing via the H2 console or the running application.
 * <p>
 * Run with: mvn test -Dgroups=manual
 * <p>
 * WARNING: clears ALL quiz/team/result data before seeding (users are preserved).
 */
@Tag("manual")
@SpringBootTest
@ActiveProfiles("local")
class BigIntegrationTest {

    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    // -------------------------------------------------------------------------
    // Cleanup — runs before the test to ensure a clean slate
    // -------------------------------------------------------------------------

    @BeforeEach
    void clearNonUserData() {
        resultRepository.deleteAll();   // cascades to result_answer
        quizRepository.deleteAll();     // cascades to question + question_hints
        teamRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // Main test
    // -------------------------------------------------------------------------

    @Test
    void seedBaseData() throws Exception {
        // --- 1. Create quizzes ---
        Quiz quiz1 = buildApril2026Quiz();
        Quiz quiz2 = buildPopkulturQuiz();
        Quiz quiz3 = buildWissenschaftQuiz();
        Quiz quiz4 = buildGeschichteQuiz();
        Quiz quiz5 = buildOesterreichQuiz();

        quizRepository.saveAll(List.of(quiz1, quiz2, quiz3, quiz4, quiz5));
        List<Quiz> quizzes = List.of(quiz1, quiz2, quiz3, quiz4, quiz5);

        // --- 2. Create teams ---
        List<Team> teams = buildTeams();
        teamRepository.saveAll(teams);

        // --- 3. Create results ---
        // Team i (0-indexed) participates in quizzes 0..min(i+1, 5)-1
        // so team[0] → 1 quiz, team[1] → 2 quizzes, ..., team[4..9] → 5 quizzes
        // Points ramp from high (team 0) to low (team 9) for realism
        int[][] pointTemplates = {
                {0, 3, 3, 5, 5, 2, 5, 5},  // team 0 — Dicktales
                {0, 3, 3, 1, 5, 0, 5, 5},  // team 1 — Stute
                {0, 3, 3, 5, 1, 0, 5, 5},  // team 2 — UDO
                {0, 3, 3, 5, 5, 1, 5, 5},  // team 3 — Vienna Lemmings
                {0, 3, 0, 2, 0, 0, 0, 2},  // team 4 — The M-Team
                {0, 0, 0, 0, 2, 0, 0, 2},  // team 5 — Die Dodos
                {0, 0, 0, 0, 1, 1, 3, 0},  // team 6 — 5BG Hotdogs
                {2, 5, 0, 5, 3, 0, 5, 3},  // team 7 — TEAM
                {0, 3, 0, 3, 0, 0, 3, 2},  // team 8 — Die Julians
                {0, 3, 0, 5, 5, 1, 5, 2},  // team 9 — Ginger Army
                {0, 2, 0, 5, 0, 1, 1, 0},  // team 9 — Me is Quiz
                {0, 3, 0, 5, 0, 0, 2, 2},  // team 9 — SpaGra
        };

        List<Result> allResults = new ArrayList<>();
        for (int t = 0; t < teams.size(); t++) {
            int quizCount = Math.min(t + 1, quizzes.size());
            for (int q = 0; q < quizCount; q++) {
                allResults.add(buildResult(teams.get(t), quizzes.get(q), pointTemplates[t]));
            }
        }
        resultRepository.saveAll(allResults);

        // --- 4. Assert counts ---
        assertEquals(5, quizRepository.count(), "Expected 5 quizzes");
        assertEquals(12, teamRepository.count(), "Expected 12 teams");
        assertEquals(50, resultRepository.count(), "Expected 50 results");

        // --- 5. Spot-check quiz structure ---
        Quiz loaded1 = quizRepository.findById(quiz1.getQuizId()).orElseThrow();
        assertEquals(8, loaded1.getQuestions().size(), "Quiz 1 should have 8 questions");
        assertEquals(4, loaded1.getQuestions().get(0).getHints().size(), "Q1 should have 4 hints");
        assertEquals(4, loaded1.getQuestions().get(3).getHints().size(), "Q4 should have 4 hints");
        assertEquals(3, loaded1.getQuestions().get(4).getHints().size(), "Q5 should have 3 hints");
        assertEquals(3, loaded1.getQuestions().get(7).getHints().size(), "Q8 should have 3 hints");

        // --- 6. Spot-check result content ---
        Result r = resultRepository
                .findByIdWithAnswers(
                        resultRepository.findByTeam_TeamsIdAndQuiz_QuizId(
                                        teams.get(0).getTeamsId(), quiz1.getQuizId())
                                .orElseThrow().getResultsId())
                .orElseThrow();
        assertEquals(8, r.getAnswers().size(), "Result should have 8 answers");
        assertTrue(r.calculateTotalPoints() > 0, "Top team should have positive points");

        // --- 7. Spot-check low-scorer ---
        Result last = resultRepository
                .findByIdWithAnswers(
                        resultRepository.findByTeam_TeamsIdAndQuiz_QuizId(
                                        teams.get(9).getTeamsId(), quiz1.getQuizId())
                                .orElseThrow().getResultsId())
                .orElseThrow();
        assertEquals(8, last.getAnswers().size());
        assertTrue(last.calculateTotalPoints() < r.calculateTotalPoints(),
                "Last team should score less than first team");

        System.out.println("=== BigIntegrationTest: database seeded successfully ===");
        System.out.println("  Quizzes  : " + quizRepository.count());
        System.out.println("  Teams    : " + teamRepository.count());
        System.out.println("  Results  : " + resultRepository.count());
    }

    // -------------------------------------------------------------------------
    // Quiz builders
    // -------------------------------------------------------------------------

    private Quiz buildApril2026Quiz() throws Exception {
        Quiz q = new Quiz();
        q.setPubDate(LocalDate.of(2025, 4, 1));
        q.setSubmitDate(LocalDate.of(2025, 4, 2));
        q.addQuestion(1, "Zwar nicht sprichwörtlicih, aber das ist in Ordnung", "Wörter in alphabetischer Ordnung", "",
                hints("Beikost", "Dekor", "Film", "Eis"));
        q.addQuestion(2, "Die neue ART der Suche. Deden - nein - Gegen das immer gleiche Bild?", "Google Doodle", "",
                hints("International: Tag der Bastille 2000", "Interaktiv: Pacman 2010", "Video: Charlie Chaplin 2011", "Überhaupt: Burning Man Festival 1998"));
        q.addQuestion(3, "Wer bin ich? Ein Lied, mal kalt, mal heiß. Abschnitte des Sesselspiels?", "Ned Stark", "",
                hints("4", "12", "16", "20"));
        q.addQuestion(4, "Alle gemeinsam sind über jeder Tür zU finden", "Netzplan über den U-Bahn Türen", "",
                hintsWithImages(
                    "/pics/2026April_4_1.jpg",
                    "/pics/2026April_4_2.jpg",
                    "/pics/2026April_4_3.jpg",
                    "/pics/2026April_4_4.jpg"
                ));
        q.addQuestion(5, "Eine Erfindung der EU. Wenn die Hauptstadt nicht die Hauptstadt ist.", "Belgien 1993", "",
                hints("Italien 1986", "BR Deutschland 1988", "UK 1990"));
        q.addQuestion(6, "Auch das Personal in meiner Gruppe. Aberwo MAg der Leiter sein?", "36-1200", "",
                hints("20-1120", "22-1200", "31-1060"));
        q.addQuestion(7, "Am Fuße des Doms stirbt der reiche Mann. Wieder und wieder und wieder...", "Peter 91x", "",
                hints("Walther 55x", "Will 56x", "Alexander 69x"));
        q.addQuestion(8, "In England schreit ein blinder Computer schriftlich um Hilfe?", "11 10 10 10 ", "HELP in Computer Braille",
                hints("10 11 00 10", "10 01 00 10 ", "11 10 10 10"));
        return q;
    }

    private Quiz buildPopkulturQuiz() {
        Quiz q = new Quiz();
        q.setPubDate(LocalDate.of(2025, 3, 20));
        q.setSubmitDate(LocalDate.of(2025, 3, 19));
        q.addQuestion(1, "Wie heißt der Regisseur von Pulp Fiction?", "Quentin Tarantino", "",
                hints("US-Amerikaner", "Bekannt für stilisierte Gewalt", "Auch Kill Bill", "Filmzitate-König"));
        q.addQuestion(2, "In welchem Jahr erschien das erste iPhone?", "2007", "",
                hints("21. Jahrhundert", "Steve Jobs stellte es vor", "Vor 2010", "Revolutionierte Smartphones"));
        q.addQuestion(3, "Wer sang 'Thriller'?", "Michael Jackson", "",
                hints("King of Pop", "Moonwalk-Erfinder", "Gestorben 2009", "Schwarzes Leder im Clip"));
        q.addQuestion(4, "Wie heißt Harry Potters Eule?", "Hedwig", "",
                hints("Weiße Eule", "Geschenk von Hagrid", "Schneeeule", "Weiblich"));
        q.addQuestion(5, "In welcher Stadt spielt die Serie 'Breaking Bad'?", "Albuquerque", "",
                hints("USA", "Bundesstaat New Mexico", "Wüstenklima"));
        q.addQuestion(6, "Welche Band sang 'Bohemian Rhapsody'?", "Queen", "",
                hints("Britisch", "Freddie Mercury", "Hard Rock trifft Oper"));
        q.addQuestion(7, "Wie viele Oscars gewann Titanic (1997)?", "11", "",
                hints("Gleich viele wie Ben-Hur", "Rekord damals", "Zweistellig"));
        q.addQuestion(8, "Wer schrieb den Roman 'Der Herr der Ringe'?", "J.R.R. Tolkien", "",
                hints("Britischer Autor", "Oxford-Professor", "Erfand Mittelerde"));
        return q;
    }

    private Quiz buildWissenschaftQuiz() {
        Quiz q = new Quiz();
        q.setPubDate(LocalDate.of(2025, 5, 10));
        q.setSubmitDate(LocalDate.of(2025, 5, 9));
        q.addQuestion(1, "Was ist die chemische Formel für Kochsalz?", "NaCl", "",
                hints("Natrium und Chlor", "Zwei Elemente", "Ionenverbindung", "Im Meer gelöst"));
        q.addQuestion(2, "Wie viele Knochen hat ein erwachsener Mensch?", "206", "",
                hints("Dreistellige Zahl", "Mehr als 200", "Weniger als 210", "Kinder haben mehr"));
        q.addQuestion(3, "Was ist der schnellste Vogel der Welt im Sturzflug?", "Wanderfalke", "",
                hints("Über 300 km/h", "Sturzflug-Spezialist", "Raubvogel", "Kein Pflanzenfresser"));
        q.addQuestion(4, "Welches Element hat das chemische Symbol Au?", "Gold", "",
                hints("Edelmetall", "Für Schmuck bekannt", "Latein: Aurum", "Gelb glänzend"));
        q.addQuestion(5, "In welchem Organ wird Insulin produziert?", "Bauchspeicheldrüse (Pankreas)", "",
                hints("Hinter dem Magen", "Verdauungsorgan", "Nicht die Leber"));
        q.addQuestion(6, "Was ist die Atommasse von Kohlenstoff?", "12", "",
                hints("Grundelement des Lebens", "Organische Chemie", "Einstellig mal zwei"));
        q.addQuestion(7, "Wie nennt man das wissenschaftliche Studium von Pilzen?", "Mykologie", "",
                hints("Griechischer Ursprung", "Mykos = Pilz", "Endet auf -logie"));
        q.addQuestion(8, "Was ist die chemische Bezeichnung für Wasser?", "H\u2082O", "",
                hints("Zwei Elemente", "Flüssig bei 20°C", "Basis des Lebens"));
        return q;
    }

    private Quiz buildGeschichteQuiz() {
        Quiz q = new Quiz();
        q.setPubDate(LocalDate.of(2025, 7, 4));
        q.setSubmitDate(LocalDate.of(2025, 7, 3));
        q.addQuestion(1, "In welchem Jahr fiel die Berliner Mauer?", "1989", "",
                hints("20. Jahrhundert", "Ende des Kalten Krieges", "80er oder 90er?", "November"));
        q.addQuestion(2, "Wer war der erste Mensch im Weltall?", "Juri Gagarin", "",
                hints("Sowjetunion", "Jahr 1961", "Kosmonaut", "Raumkapsel Wostok 1"));
        q.addQuestion(3, "Welches Land gewann die erste Fußball-WM 1930?", "Uruguay", "",
                hints("Südamerika", "Gastgeberland", "Kleines Land", "Hellblaues Trikot"));
        q.addQuestion(4, "Wie hieß die erste deutsche Demokratie (1919–1933)?", "Weimarer Republik", "",
                hints("Zwischen Kaiserreich und NS-Zeit", "In Thüringen gegründet", "Benannt nach einer Stadt", "Scheiterte 1933"));
        q.addQuestion(5, "In welchem Jahr begann der Erste Weltkrieg?", "1914", "",
                hints("Attentat in Sarajevo", "Vor 1920", "Früh im 20. Jahrhundert"));
        q.addQuestion(6, "Wie hieß Napoleons letzte Niederlage?", "Waterloo", "",
                hints("Belgien", "ABBA schrieb einen Song darüber", "Jahr 1815"));
        q.addQuestion(7, "Welches Schiff versank 1912 bei seiner Jungfernfahrt?", "Titanic", "",
                hints("Eisberg", "April", "Nordatlantik"));
        q.addQuestion(8, "Wer war der erste Präsident der USA?", "George Washington", "",
                hints("Gründervater", "18. Jahrhundert", "Aus Virginia"));
        return q;
    }

    private Quiz buildOesterreichQuiz() {
        Quiz q = new Quiz();
        q.setPubDate(LocalDate.of(2025, 9, 18));
        q.setSubmitDate(LocalDate.of(2025, 9, 17));
        q.addQuestion(1, "Wie heißt der höchste Berg Österreichs?", "Großglockner", "",
                hints("In den Alpen", "Kärnten/Tirol-Grenze", "Über 3700m", "Hat einen Gletscher"));
        q.addQuestion(2, "In welcher Stadt wurde Mozart geboren?", "Salzburg", "",
                hints("Österreich", "Nicht Wien", "Bekannt für Festspiele", "W.A.M."));
        q.addQuestion(3, "Wie heißt das österreichische Parlament?", "Nationalrat", "",
                hints("Demokratie", "In Wien", "Ringstraße", "Gewählte Abgeordnete"));
        q.addQuestion(4, "Welches Gericht ist ein Wiener Klassiker mit Rindfleisch?", "Tafelspitz", "",
                hints("Rind", "Klare Suppe", "Mit Meerrettich", "Kaisergericht"));
        q.addQuestion(5, "Wie viele Bundesländer hat Österreich?", "9", "",
                hints("Einstellige Zahl", "Mehr als 8", "Wien ist eines davon"));
        q.addQuestion(6, "Wie heißt die berühmte Wiener Schokoladentorte?", "Sachertorte", "",
                hints("Schokolade", "Benannt nach einem Hotel", "Erfunden 1832"));
        q.addQuestion(7, "Welcher österreichische Komponist schrieb die Zauberflöte?", "Wolfgang Amadeus Mozart", "",
                hints("18. Jahrhundert", "In Wien uraufgeführt", "Sein letztes vollendetes Werk"));
        q.addQuestion(8, "Wie nennt man auf Wienerisch die Kartoffel?", "Erdäpfel", "",
                hints("Österreichischer Dialekt", "Knollengemüse", "Wort beginnt mit 'Erd'"));
        return q;
    }

    // -------------------------------------------------------------------------
    // Team builder
    // -------------------------------------------------------------------------

    private List<Team> buildTeams() {
        return List.of(
                team("Dicktales"),
                team("Stute"),
                team("UDO"),
                team("Vienna Lemmings"),
                team("The M-Team"),
                team("Die Dodos"),
                team("5BG Hotdogs"),
                team("TEAM"),
                team("Die Julian"),
                team("Ginger Army"),
                team("Me is Quiz"),
                team("Spagra")
        );
    }

    private Team team(String name) {
        Team t = new Team();
        t.setTeamName(name);
        return t;
    }

    // -------------------------------------------------------------------------
    // Result builder
    // -------------------------------------------------------------------------

    private Result buildResult(Team team, Quiz quiz, int[] points) {
        Result result = new Result();
        result.setTeam(team);
        result.setQuiz(quiz);
        List<ResultAnswer> answers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ResultAnswer ra = new ResultAnswer();
            ra.setQuestionNumber(i + 1);
            ra.setPoints(points[i]);
            ra.setChanged(false);
            ra.setResult(result);
            answers.add(ra);
        }
        result.setAnswers(answers);
        return result;
    }

    // -------------------------------------------------------------------------
    // Hint helper
    // -------------------------------------------------------------------------

    private static List<Hint> hints(String... texts) {
        List<Hint> list = new ArrayList<>();
        for (String text : texts) {
            Hint h = new Hint();
            h.setHintText(text);
            list.add(h);
        }
        return list;
    }

    private List<Hint> hintsWithImages(String... resourcePaths) throws Exception {
        List<Hint> list = new ArrayList<>();
        for (String path : resourcePaths) {
            Hint h = new Hint();
            h.setHintText("");
            h.setImageUrlAsHint(storeClasspathImage(path));
            list.add(h);
        }
        return list;
    }

    // resourcePath must start with '/' for absolute classpath resolution (e.g. "/pics/foo.jpg")
    private String storeClasspathImage(String resourcePath) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IllegalStateException("Test resource not found: " + resourcePath);
            byte[] bytes = is.readAllBytes();
            String filename = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            MockMultipartFile file = new MockMultipartFile(filename, filename, "image/jpeg", bytes);
            return imageStorageService.store(file);
        }
    }
}
