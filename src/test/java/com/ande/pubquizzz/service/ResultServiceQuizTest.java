package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.*;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.QuizResultEntry;
import com.ande.pubquizzz.dto.QuizResultsResponse;
import com.ande.pubquizzz.dto.QuizSummaryDTO;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceQuizTest {

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

    private Quiz quiz1;
    private Quiz quiz2;
    private Team teamA;
    private Team teamB;

    @BeforeEach
    void setUp() {
        quiz1 = new Quiz();
        quiz1.setPubDate(LocalDate.of(2026, 3, 15));
        quiz1.setSubmitDate(LocalDate.of(2026, 3, 16));

        quiz2 = new Quiz();
        quiz2.setPubDate(LocalDate.of(2026, 1, 10));
        quiz2.setSubmitDate(LocalDate.of(2026, 1, 11));
        quiz2.setTitle("Jahresstart 2026");

        teamA = new Team();
        teamA.setTeamName("Alpha");

        teamB = new Team();
        teamB.setTeamName("Beta");
    }

    private ResultAnswer makeAnswer(int questionNumber, int points) {
        ResultAnswer a = new ResultAnswer();
        a.setQuestionNumber(questionNumber);
        a.setPoints(points);
        a.setChanged(false);
        return a;
    }

    private Result makeResult(Quiz quiz, Team team, List<ResultAnswer> answers) {
        Result r = new Result();
        r.setQuiz(quiz);
        r.setTeam(team);
        answers.forEach(a -> a.setResult(r));
        r.setAnswers(answers);
        return r;
    }

    // --- getQuizSummaries tests ---

    @Test
    void getQuizSummaries_returnsSortedNewestFirst() {
        List<Object[]> rows = List.of(
                new Object[]{quiz1, 3L},
                new Object[]{quiz2, 1L}
        );
        when(quizRepository.findAllWithResultCount()).thenReturn(rows);
        when(resultRepository.findScoresByQuizIds(anyList())).thenReturn(List.of());

        List<QuizSummaryDTO> summaries = resultService.getQuizSummaries();

        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).getQuizTitle()).isEqualTo("2026 März");
        assertThat(summaries.get(0).getPubDate()).isEqualTo("2026-03-15");
        assertThat(summaries.get(0).getTeamCount()).isEqualTo(3);
        assertThat(summaries.get(1).getQuizTitle()).isEqualTo("Jahresstart 2026");
        assertThat(summaries.get(1).getTeamCount()).isEqualTo(1);
    }

    @Test
    void getQuizSummaries_withExplicitTitle_usesTitle() {
        List<Object[]> rows2 = new java.util.ArrayList<>();
        rows2.add(new Object[]{quiz2, 0L});
        when(quizRepository.findAllWithResultCount()).thenReturn(rows2);

        List<QuizSummaryDTO> summaries = resultService.getQuizSummaries();

        assertThat(summaries.get(0).getQuizTitle()).isEqualTo("Jahresstart 2026");
    }

    // --- getResultsForQuiz tests ---

    @Test
    void getResultsForQuiz_ranksByTotalPointsDescWithOlympicRanking() {
        Result rA = makeResult(quiz1, teamA, List.of(makeAnswer(1, 10), makeAnswer(2, 5)));  // 15 pts
        Result rB = makeResult(quiz1, teamB, List.of(makeAnswer(1, 10), makeAnswer(2, 10))); // 20 pts

        when(resultRepository.findByQuizIdWithTeamAndAnswers(1L)).thenReturn(List.of(rA, rB));

        QuizResultsResponse response = resultService.getResultsForQuiz(1L);
        List<QuizResultEntry> entries = response.getEntries();

        assertThat(response.getQuizTitle()).isEqualTo("2026 März");
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getTeamName()).isEqualTo("Beta");
        assertThat(entries.get(0).getTotalPoints()).isEqualTo(20);
        assertThat(entries.get(0).getRank()).isEqualTo(1);
        assertThat(entries.get(1).getTeamName()).isEqualTo("Alpha");
        assertThat(entries.get(1).getTotalPoints()).isEqualTo(15);
        assertThat(entries.get(1).getRank()).isEqualTo(2);
    }

    @Test
    void getResultsForQuiz_olympicRankingForTies() {
        Team teamC = new Team();
        teamC.setTeamName("Gamma");

        Result rA = makeResult(quiz1, teamA, List.of(makeAnswer(1, 28))); // 28 pts
        Result rB = makeResult(quiz1, teamB, List.of(makeAnswer(1, 28))); // 28 pts — tie with A
        Result rC = makeResult(quiz1, teamC, List.of(makeAnswer(1, 20))); // 20 pts

        when(resultRepository.findByQuizIdWithTeamAndAnswers(1L)).thenReturn(List.of(rA, rB, rC));

        List<QuizResultEntry> entries = resultService.getResultsForQuiz(1L).getEntries();

        assertThat(entries).hasSize(3);
        // Both tied teams get rank 1
        assertThat(entries.stream().filter(e -> e.getTotalPoints() == 28).map(QuizResultEntry::getRank))
                .containsOnly(1);
        // Next team gets rank 3 (Olympic skip)
        assertThat(entries.stream().filter(e -> e.getTotalPoints() == 20).map(QuizResultEntry::getRank))
                .containsOnly(3);
    }

    // --- getQuizSummaries winner tests ---

    @Test
    void getQuizSummaries_withResults_populatesWinnerTeamName() {
        quiz1.setQuizId(10L);
        List<Object[]> rows = List.<Object[]>of(new Object[]{quiz1, 2L});
        when(quizRepository.findAllWithResultCount()).thenReturn(rows);
        // [quizId, teamName, totalPoints, fivesCount, threesCount]
        List<Object[]> scoreRows = List.<Object[]>of(
                new Object[]{10L, "Alpha", 30L, 5L, 1L},
                new Object[]{10L, "Beta", 25L, 4L, 1L}
        );
        when(resultRepository.findScoresByQuizIds(List.of(10L))).thenReturn(scoreRows);

        List<QuizSummaryDTO> summaries = resultService.getQuizSummaries();

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).getWinnerTeamName()).isEqualTo("Alpha");
    }

    @Test
    void getQuizSummaries_withNoResults_winnerTeamNameIsNull() {
        quiz1.setQuizId(11L);
        List<Object[]> rows = List.<Object[]>of(new Object[]{quiz1, 0L});
        when(quizRepository.findAllWithResultCount()).thenReturn(rows);

        List<QuizSummaryDTO> summaries = resultService.getQuizSummaries();

        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).getWinnerTeamName()).isNull();
    }

    @Test
    void getQuizSummaries_tieOnTotal_winnerHasMoreFives() {
        quiz1.setQuizId(12L);
        List<Object[]> rows = List.<Object[]>of(new Object[]{quiz1, 2L});
        when(quizRepository.findAllWithResultCount()).thenReturn(rows);
        // Both teams 25 pts; Beta has more 5-point answers
        List<Object[]> scoreRows = List.<Object[]>of(
                new Object[]{12L, "Alpha", 25L, 3L, 5L},
                new Object[]{12L, "Beta", 25L, 5L, 0L}
        );
        when(resultRepository.findScoresByQuizIds(List.of(12L))).thenReturn(scoreRows);

        List<QuizSummaryDTO> summaries = resultService.getQuizSummaries();

        assertThat(summaries.get(0).getWinnerTeamName()).isEqualTo("Beta");
    }

    @Test
    void getQuizSummaries_tieOnTotalAndFives_winnerHasMoreThrees() {
        quiz1.setQuizId(13L);
        List<Object[]> rows = List.<Object[]>of(new Object[]{quiz1, 2L});
        when(quizRepository.findAllWithResultCount()).thenReturn(rows);
        // Both teams 25 pts, both 3 fives; Alpha has more 3-point answers
        List<Object[]> scoreRows = List.<Object[]>of(
                new Object[]{13L, "Alpha", 25L, 3L, 4L},
                new Object[]{13L, "Beta", 25L, 3L, 2L}
        );
        when(resultRepository.findScoresByQuizIds(List.of(13L))).thenReturn(scoreRows);

        List<QuizSummaryDTO> summaries = resultService.getQuizSummaries();

        assertThat(summaries.get(0).getWinnerTeamName()).isEqualTo("Alpha");
    }

    // --- getResultsForQuiz tiebreaker tests ---

    @Test
    void getResultsForQuiz_tieOnTotal_rankedByFivesDesc() {
        // Both 20 pts; Beta has 4 fives (4×5=20), Alpha has 2 fives + 2 fives-equiv via 3s
        Result rA = makeResult(quiz1, teamA, List.of(
                makeAnswer(1, 5), makeAnswer(2, 5), makeAnswer(3, 3), makeAnswer(4, 3), makeAnswer(5, 2), makeAnswer(6, 2)));  // 20 pts, 2 fives
        Result rB = makeResult(quiz1, teamB, List.of(
                makeAnswer(1, 5), makeAnswer(2, 5), makeAnswer(3, 5), makeAnswer(4, 5)));                                     // 20 pts, 4 fives

        when(resultRepository.findByQuizIdWithTeamAndAnswers(1L)).thenReturn(List.of(rA, rB));

        List<QuizResultEntry> entries = resultService.getResultsForQuiz(1L).getEntries();

        assertThat(entries.get(0).getTeamName()).isEqualTo("Beta");
        assertThat(entries.get(0).getRank()).isEqualTo(1);
        assertThat(entries.get(1).getTeamName()).isEqualTo("Alpha");
        assertThat(entries.get(1).getRank()).isEqualTo(2);
    }

    @Test
    void getResultsForQuiz_tieOnTotalAndFives_rankedByThreesDesc() {
        // Alpha: 20 pts, 2 fives, 3 threes
        Result rAlpha = makeResult(quiz1, teamA, List.of(
                makeAnswer(1, 5), makeAnswer(2, 5), makeAnswer(3, 3), makeAnswer(4, 3), makeAnswer(5, 3), makeAnswer(6, 1)));
        // Beta: 20 pts, 2 fives, 2 threes — fewer threes loses
        Result rBeta = makeResult(quiz1, teamB, List.of(
                makeAnswer(1, 5), makeAnswer(2, 5), makeAnswer(3, 3), makeAnswer(4, 3), makeAnswer(5, 2), makeAnswer(6, 2)));

        when(resultRepository.findByQuizIdWithTeamAndAnswers(1L)).thenReturn(List.of(rBeta, rAlpha));

        List<QuizResultEntry> entries = resultService.getResultsForQuiz(1L).getEntries();

        assertThat(entries.get(0).getTeamName()).isEqualTo("Alpha");
        assertThat(entries.get(0).getRank()).isEqualTo(1);
        assertThat(entries.get(1).getTeamName()).isEqualTo("Beta");
        assertThat(entries.get(1).getRank()).isEqualTo(2);
    }
}
