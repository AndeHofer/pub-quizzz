package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaderboardYearIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        clearCaches();
        resultRepository.deleteAll();
        quizRepository.deleteAll();
        teamRepository.deleteAll();

        Team alpha = saveTeam("Alpha Team");
        Team beta = saveTeam("Beta Team");
        Team gamma = saveTeam("Gamma Team");

        Quiz may2025 = saveQuiz(LocalDate.of(2025, 5, 1));
        Quiz june2025 = saveQuiz(LocalDate.of(2025, 6, 1));
        Quiz may2026 = saveQuiz(LocalDate.of(2026, 5, 1));

        resultRepository.save(buildResult(alpha, may2025, new int[]{5, 5, 5, 5, 5, 5, 5, 5}));
        resultRepository.save(buildResult(beta, may2025, new int[]{5, 5, 5, 5, 5, 5, 3, 3}));
        resultRepository.save(buildResult(alpha, june2025, new int[]{5, 5, 5, 5, 5, 0, 0, 0}));
        resultRepository.save(buildResult(beta, june2025, new int[]{3, 3, 3, 3, 3, 3, 3, 3}));
        resultRepository.save(buildResult(gamma, may2026, new int[]{5, 5, 5, 5, 5, 3, 3, 3}));
    }

    @Test
    void leaderboardEndpoints_recalculateForSelectedYearAndExposeAvailableYears() throws Exception {
        mockMvc.perform(get("/api/leaderboard/years").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(2026))
                .andExpect(jsonPath("$[1]").value(2025));

        mockMvc.perform(get("/api/leaderboard/points").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"));

        mockMvc.perform(get("/api/leaderboard/points").param("year", "2025").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].quizCount").value(2));

        mockMvc.perform(get("/api/leaderboard/average").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Gamma Team"));

        mockMvc.perform(get("/api/leaderboard/average").param("year", "2025").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].quizCount").value(2));

        mockMvc.perform(get("/api/leaderboard/medals").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"));

        mockMvc.perform(get("/api/leaderboard/medals").param("year", "2025").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"));

        mockMvc.perform(get("/api/leaderboard/top-results").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].quizDate").value("2025-05-01"));

        mockMvc.perform(get("/api/leaderboard/top-results").param("year", "2026").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Gamma Team"))
                .andExpect(jsonPath("$[0].quizDate").value("2026-05-01"));
    }

    private Team saveTeam(String name) {
        Team team = new Team();
        team.setTeamName(name);
        return teamRepository.save(team);
    }

    private Quiz saveQuiz(LocalDate pubDate) {
        Quiz quiz = new Quiz();
        quiz.setPubDate(pubDate);
        quiz.setSubmitDate(pubDate.plusDays(1));
        quiz.addQuestion(1, "Frage 1", "Antwort 1", "", hints("a", "b", "c", "d"));
        quiz.addQuestion(2, "Frage 2", "Antwort 2", "", hints("a", "b", "c", "d"));
        quiz.addQuestion(3, "Frage 3", "Antwort 3", "", hints("a", "b", "c", "d"));
        quiz.addQuestion(4, "Frage 4", "Antwort 4", "", hints("a", "b", "c", "d"));
        quiz.addQuestion(5, "Frage 5", "Antwort 5", "", hints("a", "b", "c"));
        quiz.addQuestion(6, "Frage 6", "Antwort 6", "", hints("a", "b", "c"));
        quiz.addQuestion(7, "Frage 7", "Antwort 7", "", hints("a", "b", "c"));
        quiz.addQuestion(8, "Frage 8", "Antwort 8", "", hints("a", "b", "c"));
        return quizRepository.save(quiz);
    }

    private static Result buildResult(Team team, Quiz quiz, int[] points) {
        Result result = new Result();
        result.setTeam(team);
        result.setQuiz(quiz);

        List<ResultAnswer> answers = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            ResultAnswer answer = new ResultAnswer();
            answer.setQuestionNumber(i + 1);
            answer.setPoints(points[i]);
            answer.setChanged(false);
            answer.setResult(result);
            answers.add(answer);
        }
        result.setAnswers(answers);
        return result;
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor user() {
        return SecurityMockMvcRequestPostProcessors.user("test").roles("USER");
    }

    private static List<Hint> hints(String... texts) {
        return Arrays.stream(texts).map(text -> {
            Hint hint = new Hint();
            hint.setHintText(text);
            return hint;
        }).toList();
    }

    private void clearCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }
}
