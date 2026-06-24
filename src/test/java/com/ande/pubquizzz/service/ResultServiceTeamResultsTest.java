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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
        team.setTeamsId(7L);
        team.setTeamName("TestTeam");

        quizOld = new Quiz();
        quizOld.setQuizId(1L);
        quizOld.setPubDate(LocalDate.of(2026, 1, 1));
        quizOld.setSubmitDate(LocalDate.of(2026, 1, 2));

        quizNew = new Quiz();
        quizNew.setQuizId(2L);
        quizNew.setPubDate(LocalDate.of(2026, 3, 15));
        quizNew.setSubmitDate(LocalDate.of(2026, 3, 16));
    }

    private ResultAnswer makeAnswer(int questionNumber, int points) {
        ResultAnswer a = new ResultAnswer();
        a.setQuestionNumber(questionNumber);
        a.setPoints(points);
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
        Result oldResult = makeResult(quizOld, List.of(makeAnswer(1, 3)));
        Result newResult = makeResult(quizNew, List.of(makeAnswer(1, 5)));
        // Repository returns them already sorted (newest first per query)
        when(resultRepository.findByTeamIdOrderByPubDateDesc(7L))
            .thenReturn(List.of(newResult, oldResult));

        List<TeamResultEntry> results = resultService.getResultsForTeam(7L);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getQuizDate()).isEqualTo("2026-03-15");
        assertThat(results.get(0).getQuizTitle()).isEqualTo("2026 März");
        assertThat(results.get(1).getQuizDate()).isEqualTo("2026-01-01");
        assertThat(results.get(1).getQuizTitle()).isEqualTo("2026 Jänner");
    }

    @Test
    void getResultsForTeam_ignoresStoredTitleAndUsesPubDateMonthYear() {
        quizNew.setTitle("Frühjahr 2026");
        Result result = makeResult(quizNew, List.of(makeAnswer(1, 5)));
        when(resultRepository.findByTeamIdOrderByPubDateDesc(7L))
            .thenReturn(List.of(result));

        List<TeamResultEntry> results = resultService.getResultsForTeam(7L);

        assertThat(results.get(0).getQuizTitle()).isEqualTo("2026 März");
    }

    @Test
    void getResultsForTeam_unknownTeam_returnsEmpty() {
        when(resultRepository.findByTeamIdOrderByPubDateDesc(999L))
            .thenReturn(List.of());

        List<TeamResultEntry> results = resultService.getResultsForTeam(999L);

        assertThat(results).isEmpty();
    }

    @Test
    void getResultsForTeam_mapsAnswersCorrectly() {
        ResultAnswer a1 = makeAnswer(1, 4);
        ResultAnswer a2 = makeAnswer(2, 2);
        Result result = makeResult(quizNew, List.of(a1, a2));
        when(resultRepository.findByTeamIdOrderByPubDateDesc(7L))
            .thenReturn(List.of(result));

        List<TeamResultEntry> results = resultService.getResultsForTeam(7L);

        assertThat(results).hasSize(1);
        TeamResultEntry entry = results.get(0);
        assertThat(entry.getTotalPoints()).isEqualTo(6);
        assertThat(entry.getAnswers()).hasSize(2);
        assertThat(entry.getQuizRank()).isEqualTo(1);
        assertThat(entry.getParticipantCount()).isEqualTo(1);
        assertThat(entry.getAnswers().get(0).getQuestionNumber()).isEqualTo(1);
        assertThat(entry.getAnswers().get(0).getPoints()).isEqualTo(4);
    }

    @Test
    void getResultsForTeam_withScoreRowsIncludingTeamId_calculatesRankWithoutClassCast() {
        Team otherTeam = new Team();
        otherTeam.setTeamsId(9L);
        otherTeam.setTeamName("Other Team");

        Result tracked = makeResult(quizNew, List.of(makeAnswer(1, 5), makeAnswer(2, 3)));

        when(resultRepository.findByTeamIdOrderByPubDateDesc(7L)).thenReturn(List.of(tracked));
        when(resultRepository.findScoresByQuizIds(List.of(2L))).thenReturn(new ArrayList<>(List.of(
                new Object[]{2L, 7L, "TestTeam", 8L, 1L, 1L},
                new Object[]{2L, 9L, "Other Team", 9L, 1L, 2L}
        )));

        assertThatCode(() -> resultService.getResultsForTeam(7L))
                .doesNotThrowAnyException();

        List<TeamResultEntry> results = resultService.getResultsForTeam(7L);
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getQuizRank()).isEqualTo(2);
        assertThat(results.getFirst().getParticipantCount()).isEqualTo(2);
    }
}
