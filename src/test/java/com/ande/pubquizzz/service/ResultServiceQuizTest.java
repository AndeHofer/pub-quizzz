package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.*;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.QuizResultEntry;
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

        List<QuizResultEntry> entries = resultService.getResultsForQuiz(1L);

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

        List<QuizResultEntry> entries = resultService.getResultsForQuiz(1L);

        assertThat(entries).hasSize(3);
        // Both tied teams get rank 1
        assertThat(entries.stream().filter(e -> e.getTotalPoints() == 28).map(QuizResultEntry::getRank))
                .containsOnly(1);
        // Next team gets rank 3 (Olympic skip)
        assertThat(entries.stream().filter(e -> e.getTotalPoints() == 20).map(QuizResultEntry::getRank))
                .containsOnly(3);
    }
}
