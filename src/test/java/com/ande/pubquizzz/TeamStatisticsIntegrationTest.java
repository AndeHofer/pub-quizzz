package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TeamStatisticsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private QuizRepository quizzes;
    @Autowired
    private TeamRepository teams;
    @Autowired
    private ResultRepository results;
    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @BeforeEach
    void setUp() {
        clearCaches();
        results.deleteAll();
        quizzes.deleteAll();
        teams.deleteAll();
    }

    @Test
    void statisticsUseActualYearlyAndOverallRankings() throws Exception {
        Team team = team("Statistik Team");
        Team rival = team("Rivale");
        Quiz oldQuiz = quiz(2025, 1);
        Quiz newQuiz = quiz(2026, 1);
        Quiz anotherNewQuiz = quiz(2026, 2);
        result(team, oldQuiz, 1);
        result(team, newQuiz, 5);
        result(rival, newQuiz, 5);
        result(team, anotherNewQuiz, 3);

        mockMvc.perform(get("/api/teams/{id}/statistics", team.getTeamsId()).with(user("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].year").value(2026))
                .andExpect(jsonPath("$[0].points.rank").value(1))
                .andExpect(jsonPath("$[0].average.rank").value(2))
                .andExpect(jsonPath("$[0].medals.rank").value(1))
                .andExpect(jsonPath("$[0].topResults", hasSize(2)))
                .andExpect(jsonPath("$[0].topResults[0].rank").value(1))
                .andExpect(jsonPath("$[1].year").value(2025))
                .andExpect(jsonPath("$[2].year").doesNotExist())
                .andExpect(jsonPath("$[2].points.totalPoints").value(72));

        mockMvc.perform(get("/api/teams/{id}/statistics", rival.getTeamsId()).with(user("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].year").value(2026))
                .andExpect(jsonPath("$[0].topResults[0].rank").value(1));
    }

    @Test
    void unknownTeamHasNoStatisticsAndEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/teams/999999/statistics").with(user("test")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/teams/999999/statistics"))
                .andExpect(status().is3xxRedirection());
    }

    private Team team(String name) {
        Team team = new Team();
        team.setTeamName(name);
        return teams.save(team);
    }

    private Quiz quiz(int year, int month) {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.of(year, month, 1));
        quiz.setSubmitDate(LocalDate.of(year, month, 2));
        for (int i = 1; i <= 8; i++) {
            int count = i <= 4 ? 4 : 3;
            List<Hint> hints = IntStream.range(0, count).mapToObj(index -> {
                Hint hint = new Hint();
                hint.setHintText("Hinweis");
                return hint;
            }).toList();
            quiz.addQuestion(i, "Frage", "Antwort", "", hints);
        }
        return quizzes.save(quiz);
    }

    private void result(Team team, Quiz quiz, int points) {
        Result result = new Result();
        result.setTeam(team);
        result.setQuiz(quiz);
        List<ResultAnswer> answers = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            ResultAnswer answer = new ResultAnswer();
            answer.setQuestionNumber(i);
            answer.setPoints(points);
            answer.setChanged(false);
            answer.setResult(result);
            answers.add(answer);
        }
        result.setAnswers(answers);
        results.save(result);
    }
}
