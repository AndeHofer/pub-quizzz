package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.*;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.TeamResultEntry;
import com.ande.pubquizzz.mapper.ResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceTeamResultsTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ResultMapper resultMapper;

    @InjectMocks
    private ResultService resultService;

    private Team team;
    private Quiz quizOld;
    private Quiz quizNew;

    @BeforeEach
    void setUp() {
        team = new Team();
        team.setTeamName("TestTeam");

        quizOld = new Quiz();
        quizOld.setPubDate(LocalDate.of(2026, 1, 1));
        quizOld.setSubmitDate(LocalDate.of(2026, 1, 2));

        quizNew = new Quiz();
        quizNew.setPubDate(LocalDate.of(2026, 3, 15));
        quizNew.setSubmitDate(LocalDate.of(2026, 3, 16));
    }

    private ResultAnswer makeAnswer(int questionNumber, int points, Boolean changed) {
        ResultAnswer a = new ResultAnswer();
        a.setQuestionNumber(questionNumber);
        a.setPoints(points);
        a.setChanged(changed);
        return a;
    }

    private Result makeResult(Quiz quiz, List<ResultAnswer> answers) {
        Result r = new Result();
        r.setTeam(team);
        r.setQuiz(quiz);
        answers.forEach(a -> a.setResult(r));
        r.setAnswers(answers);
        return r;
    }

    @Test
    void getResultsForTeam_returnsSortedNewestFirst() {
        Result oldResult = makeResult(quizOld, List.of(makeAnswer(1, 3, null)));
        Result newResult = makeResult(quizNew, List.of(makeAnswer(1, 5, null)));
        // Repository returns them already sorted (newest first per query)
        when(resultRepository.findByTeamNameOrderByPubDateDesc("TestTeam"))
            .thenReturn(List.of(newResult, oldResult));

        List<TeamResultEntry> results = resultService.getResultsForTeam("TestTeam");

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getQuizDate()).isEqualTo("2026-03-15");
        assertThat(results.get(1).getQuizDate()).isEqualTo("2026-01-01");
    }

    @Test
    void getResultsForTeam_unknownTeam_returnsEmpty() {
        when(resultRepository.findByTeamNameOrderByPubDateDesc("Unknown"))
            .thenReturn(List.of());

        List<TeamResultEntry> results = resultService.getResultsForTeam("Unknown");

        assertThat(results).isEmpty();
    }

    @Test
    void getResultsForTeam_mapsAnswersCorrectly() {
        ResultAnswer a1 = makeAnswer(1, 4, true);
        ResultAnswer a2 = makeAnswer(2, 2, null);  // null changed → false
        Result result = makeResult(quizNew, List.of(a1, a2));
        when(resultRepository.findByTeamNameOrderByPubDateDesc("TestTeam"))
            .thenReturn(List.of(result));

        List<TeamResultEntry> results = resultService.getResultsForTeam("TestTeam");

        assertThat(results).hasSize(1);
        TeamResultEntry entry = results.get(0);
        assertThat(entry.getTotalPoints()).isEqualTo(6);
        assertThat(entry.getAnswers()).hasSize(2);
        assertThat(entry.getAnswers().get(0).getQuestionNumber()).isEqualTo(1);
        assertThat(entry.getAnswers().get(0).getPoints()).isEqualTo(4);
        assertThat(entry.getAnswers().get(0).getChanged()).isTrue();
        assertThat(entry.getAnswers().get(1).getChanged()).isFalse();
    }
}
