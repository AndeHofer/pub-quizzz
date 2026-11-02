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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ParticipatingTeamsIntegrationTest {
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

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        results.deleteAll();
        quizzes.deleteAll();
        teams.deleteAll();
    }

    @Test
    void listsEachParticipatingTeamOnceIncludingZeroPointResults() throws Exception {
        Team alpha = team("Alpha");
        Team beta = team("Beta");
        team("Keine Teilnahme");
        Quiz first = quiz(1);
        Quiz second = quiz(2);
        result(alpha, first, 5);
        result(alpha, second, 5);
        result(beta, first, 0);

        mockMvc.perform(get("/api/teams").with(user("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].teamName", containsInAnyOrder("Alpha", "Beta")))
                .andExpect(jsonPath("$[*].teamsId", containsInAnyOrder((int) alpha.getTeamsId(), (int) beta.getTeamsId())));
    }

    @Test
    void emptyDirectoryAndUnauthenticatedRequest() throws Exception {
        team("Ohne Teilnahme");
        mockMvc.perform(get("/api/teams").with(user("test")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/teams")).andExpect(status().is3xxRedirection());
    }

    private Team team(String name) {
        Team team = new Team();
        team.setTeamName(name);
        return teams.save(team);
    }

    private Quiz quiz(int month) {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.of(2026, month, 1));
        quiz.setSubmitDate(LocalDate.of(2026, month, 2));
        for (int i = 1; i <= 8; i++) {
            List<Hint> hints = IntStream.range(0, i <= 4 ? 4 : 3).mapToObj(index -> {
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
