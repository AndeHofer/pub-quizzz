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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TopResultsLeaderboardCacheInvalidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ResultRepository resultRepository;

    @BeforeEach
    void setUp() {
        resultRepository.deleteAll();
        quizRepository.deleteAll();
        teamRepository.deleteAll();

        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.of(2026, 5, 1));
        quiz.setSubmitDate(LocalDate.of(2026, 5, 2));
        quiz.addQuestion(1, "Frage 1", "Antwort 1", "", hints("a", "b", "c", "d"));
        quiz.addQuestion(2, "Frage 2", "Antwort 2", "", hints("a", "b", "c", "d"));
        quiz.addQuestion(3, "Frage 3", "Antwort 3", "", hints("a", "b", "c", "d"));
        quiz.addQuestion(4, "Frage 4", "Antwort 4", "", hints("a", "b", "c", "d"));
        quiz.addQuestion(5, "Frage 5", "Antwort 5", "", hints("a", "b", "c"));
        quiz.addQuestion(6, "Frage 6", "Antwort 6", "", hints("a", "b", "c"));
        quiz.addQuestion(7, "Frage 7", "Antwort 7", "", hints("a", "b", "c"));
        quiz.addQuestion(8, "Frage 8", "Antwort 8", "", hints("a", "b", "c"));
        quiz = quizRepository.save(quiz);

        Team existingLeader = new Team();
        existingLeader.setTeamName("Bestehendes Team");
        existingLeader = teamRepository.save(existingLeader);

        Team newLeader = new Team();
        newLeader.setTeamName("Neues Top-Team");
        newLeader = teamRepository.save(newLeader);

        resultRepository.save(buildResult(existingLeader, quiz, new int[]{5, 5, 3, 3, 3, 3, 3, 3}));
    }

    @Test
    void topResultsLeaderboard_refreshesAfterCreatingANewHigherScoringResult() throws Exception {
        mockMvc.perform(get("/api/leaderboard/top-results").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Bestehendes Team"))
                .andExpect(jsonPath("$[0].totalPoints").value(28));

        Long quizId = quizRepository.findAll().getFirst().getQuizId();
        Long teamId = teamRepository.findAll().stream()
                .filter(team -> "Neues Top-Team".equals(team.getTeamName()))
                .findFirst()
                .orElseThrow()
                .getTeamsId();

        mockMvc.perform(post("/admin/results")
                        .with(user().roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createResultRequestJson(quizId, teamId, 5, 5, 5, 5, 5, 5, 5, 5)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/leaderboard/top-results").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Neues Top-Team"))
                .andExpect(jsonPath("$[0].totalPoints").value(40));
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor user() {
        return SecurityMockMvcRequestPostProcessors.user("test").roles("USER");
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

    private static String createResultRequestJson(Long quizId, Long teamId, int... pointsByQuestion) {
        StringBuilder answersJson = new StringBuilder();
        for (int i = 0; i < pointsByQuestion.length; i++) {
            if (i > 0) {
                answersJson.append(',');
            }
            answersJson.append("{\"questionNumber\":")
                    .append(i + 1)
                    .append(",\"points\":")
                    .append(pointsByQuestion[i])
                    .append('}');
        }

        return "{\"quizId\":" + quizId
                + ",\"teamId\":" + teamId
                + ",\"answers\":[" + answersJson + "]}";
    }

    private static List<Hint> hints(String... texts) {
        return Arrays.stream(texts).map(text -> {
            Hint hint = new Hint();
            hint.setHintText(text);
            return hint;
        }).toList();
    }

}
