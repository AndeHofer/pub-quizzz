package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.PointsLeaderboardEntry;
import com.ande.pubquizzz.dto.AverageLeaderboardEntry;
import com.ande.pubquizzz.dto.MedalLeaderboardEntry;
import com.ande.pubquizzz.mapper.ResultMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceLeaderboardTest {

    @Mock ResultRepository resultRepository;
    @Mock QuizRepository quizRepository;
    @Mock TeamRepository teamRepository;
    @Mock ResultMapper resultMapper;

    @InjectMocks ResultService resultService;

    @ParameterizedTest
    @MethodSource("pointsLeaderboardInputVariants")
    void getPointsLeaderboard_ranksEntriesByPointsForAnyInputOrder(List<Object[]> rows) {
        when(resultRepository.findLeaderboardRaw()).thenReturn(rows);

        List<PointsLeaderboardEntry> result = resultService.getPointsLeaderboard();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(0).getTeamName()).isEqualTo("Alpha Team");
        assertThat(result.get(0).getTotalPoints()).isEqualTo(150);
        assertThat(result.get(0).getQuizCount()).isEqualTo(3);

        assertThat(result.get(1).getRank()).isEqualTo(1);
        assertThat(result.get(1).getTeamName()).isEqualTo("Gamma Team");
        assertThat(result.get(1).getTotalPoints()).isEqualTo(150);
        assertThat(result.get(1).getQuizCount()).isEqualTo(2);

        assertThat(result.get(2).getRank()).isEqualTo(3);
        assertThat(result.get(2).getTeamName()).isEqualTo("Beta Team");
        assertThat(result.get(2).getTotalPoints()).isEqualTo(90);
        assertThat(result.get(2).getQuizCount()).isEqualTo(2);
    }

    private static Stream<List<Object[]>> pointsLeaderboardInputVariants() {
        Object[] alpha = {"Alpha Team", 150L, 3L};
        Object[] gamma = {"Gamma Team", 150L, 2L};
        Object[] beta = {"Beta Team", 90L, 2L};
        return Stream.of(
                List.of(beta, alpha, gamma),
                List.of(alpha, gamma, beta)
        );
    }

    @Test
    void getPointsLeaderboard_whenEmpty_returnsEmptyList() {
        when(resultRepository.findLeaderboardRaw()).thenReturn(List.of());

        List<PointsLeaderboardEntry> result = resultService.getPointsLeaderboard();

        assertThat(result).isEmpty();
    }

    @Test
    void getAverageLeaderboard_returnsEntriesRankedByAverage() {
        Object[] row1 = {"Beta Team", 100L, 2L};
        Object[] row2 = {"Alpha Team", 120L, 3L};
        Object[] row3 = {"Gamma Team", 50L, 1L};
        when(resultRepository.findLeaderboardRaw()).thenReturn(List.of(row1, row2, row3));

        List<AverageLeaderboardEntry> result = resultService.getAverageLeaderboard();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(0).getTeamName()).isEqualTo("Beta Team");
        assertThat(result.get(0).getAveragePoints()).isEqualTo(50.0);
        assertThat(result.get(0).getQuizCount()).isEqualTo(2);

        assertThat(result.get(1).getRank()).isEqualTo(1);
        assertThat(result.get(1).getTeamName()).isEqualTo("Gamma Team");
        assertThat(result.get(1).getAveragePoints()).isEqualTo(50.0);
        assertThat(result.get(1).getQuizCount()).isEqualTo(1);

        assertThat(result.get(2).getRank()).isEqualTo(3);
        assertThat(result.get(2).getTeamName()).isEqualTo("Alpha Team");
        assertThat(result.get(2).getAveragePoints()).isEqualTo(40.0);
        assertThat(result.get(2).getQuizCount()).isEqualTo(3);
    }

    @Test
    void getMedalLeaderboard_appliesCompetitionRankingAndMedalCounts() {
        when(resultRepository.findPerQuizTeamScoreBreakdownRaw()).thenReturn(List.of(
                new Object[]{1L, "Alpha Team", 40L, 3L, 1L},
                new Object[]{1L, "Beta Team", 40L, 2L, 2L},
                new Object[]{1L, "Gamma Team", 30L, 1L, 3L},
                new Object[]{2L, "Gamma Team", 50L, 4L, 1L},
                new Object[]{2L, "Alpha Team", 45L, 4L, 0L},
                new Object[]{2L, "Beta Team", 44L, 4L, 0L}
        ));

        List<MedalLeaderboardEntry> result = resultService.getMedalLeaderboard();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTeamName()).isEqualTo("Alpha Team");
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(0).getGoldCount()).isEqualTo(1);
        assertThat(result.get(0).getSilverCount()).isEqualTo(1);
        assertThat(result.get(0).getBronzeCount()).isEqualTo(0);

        Map<String, MedalLeaderboardEntry> byTeam = result.stream()
                .collect(java.util.stream.Collectors.toMap(MedalLeaderboardEntry::getTeamName, e -> e));

        assertThat(byTeam.get("Beta Team")).isNotNull();
        assertThat(byTeam.get("Beta Team").getRank()).isEqualTo(3);
        assertThat(byTeam.get("Beta Team").getGoldCount()).isEqualTo(0);
        assertThat(byTeam.get("Beta Team").getSilverCount()).isEqualTo(1);
        assertThat(byTeam.get("Beta Team").getBronzeCount()).isEqualTo(1);

        assertThat(byTeam.get("Gamma Team")).isNotNull();
        assertThat(byTeam.get("Gamma Team").getRank()).isEqualTo(2);
        assertThat(byTeam.get("Gamma Team").getGoldCount()).isEqualTo(1);
        assertThat(byTeam.get("Gamma Team").getSilverCount()).isEqualTo(0);
        assertThat(byTeam.get("Gamma Team").getBronzeCount()).isEqualTo(1);
    }

    @Test
    void getMedalLeaderboard_excludesTeamsWithoutAnyMedal() {
        when(resultRepository.findPerQuizTeamScoreBreakdownRaw()).thenReturn(List.of(
                new Object[]{1L, "Alpha Team", 60L, 4L, 1L},
                new Object[]{1L, "Beta Team", 50L, 3L, 1L},
                new Object[]{1L, "Gamma Team", 40L, 2L, 2L},
                new Object[]{1L, "Delta Team", 30L, 1L, 2L}
        ));

        List<MedalLeaderboardEntry> result = resultService.getMedalLeaderboard();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(MedalLeaderboardEntry::getTeamName)
                .containsExactly("Alpha Team", "Beta Team", "Gamma Team");
    }

    @Test
    void getMedalLeaderboard_usesSameTieRulesAsQuizRanking() {
        when(resultRepository.findPerQuizTeamScoreBreakdownRaw()).thenReturn(List.of(
                new Object[]{1L, "Alpha Team", 40L, 3L, 1L},
                new Object[]{1L, "Beta Team", 40L, 2L, 2L},
                new Object[]{1L, "Gamma Team", 35L, 3L, 0L},
                new Object[]{2L, "Beta Team", 30L, 2L, 1L},
                new Object[]{2L, "Gamma Team", 30L, 2L, 1L},
                new Object[]{2L, "Alpha Team", 29L, 3L, 0L}
        ));

        List<MedalLeaderboardEntry> result = resultService.getMedalLeaderboard();
        Map<String, MedalLeaderboardEntry> byTeam = result.stream()
                .collect(java.util.stream.Collectors.toMap(MedalLeaderboardEntry::getTeamName, e -> e));

        assertThat(byTeam.get("Alpha Team")).isNotNull();
        assertThat(byTeam.get("Alpha Team").getGoldCount()).isEqualTo(1);
        assertThat(byTeam.get("Alpha Team").getSilverCount()).isEqualTo(0);
        assertThat(byTeam.get("Alpha Team").getBronzeCount()).isEqualTo(1);

        assertThat(byTeam.get("Beta Team")).isNotNull();
        assertThat(byTeam.get("Beta Team").getGoldCount()).isEqualTo(1);
        assertThat(byTeam.get("Beta Team").getSilverCount()).isEqualTo(1);
        assertThat(byTeam.get("Beta Team").getBronzeCount()).isEqualTo(0);

        assertThat(byTeam.get("Gamma Team")).isNotNull();
        assertThat(byTeam.get("Gamma Team").getGoldCount()).isEqualTo(1);
        assertThat(byTeam.get("Gamma Team").getSilverCount()).isEqualTo(0);
        assertThat(byTeam.get("Gamma Team").getBronzeCount()).isEqualTo(1);
    }
}
