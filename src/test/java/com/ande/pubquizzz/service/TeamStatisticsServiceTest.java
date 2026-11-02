package com.ande.pubquizzz.service;

import com.ande.pubquizzz.dto.AverageLeaderboardEntry;
import com.ande.pubquizzz.dto.MedalLeaderboardEntry;
import com.ande.pubquizzz.dto.PointsLeaderboardEntry;
import com.ande.pubquizzz.dto.TeamLeaderboardStatistics;
import com.ande.pubquizzz.dto.TeamResultEntry;
import com.ande.pubquizzz.dto.TopResultLeaderboardEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamStatisticsServiceTest {
    @Mock
    private ResultService resultService;
    @Mock
    private LeaderboardService leaderboardService;
    @InjectMocks
    private TeamStatisticsService service;

    @Test
    void multipleYearsAreNewestFirstAndOverallIsLast() {
        when(resultService.getResultsForTeam(7L)).thenReturn(List.of(result("2025-03-01"),
                result("2026-03-01"), result("2026-04-01")));
        PointsLeaderboardEntry tied = new PointsLeaderboardEntry(2, 7L, "Team", 50, 2);
        AverageLeaderboardEntry average = new AverageLeaderboardEntry(1, 7L, "Team", 25, 2);
        MedalLeaderboardEntry medals = new MedalLeaderboardEntry(3, 7L, "Team", 0, 0, 1);
        TopResultLeaderboardEntry first = new TopResultLeaderboardEntry(1, 7L, "Team", 10L, "Quiz 1", "2026-03-01", 30);
        TopResultLeaderboardEntry second = new TopResultLeaderboardEntry(2, 7L, "Team", 11L, "Quiz 2", "2026-04-01", 20);
        TopResultLeaderboardEntry other = new TopResultLeaderboardEntry(1, 8L, "Other", 10L, "Quiz 1", "2026-03-01", 30);
        when(leaderboardService.getPointsLeaderboard(2026)).thenReturn(List.of(tied));
        when(leaderboardService.getAverageLeaderboard(2026)).thenReturn(List.of(average));
        when(leaderboardService.getMedalLeaderboard(2026)).thenReturn(List.of(medals));
        when(leaderboardService.getTopResultsLeaderboard(2026)).thenReturn(List.of(first, other, second));

        List<TeamLeaderboardStatistics> stats = service.getStatistics(7L);

        assertThat(stats).extracting(TeamLeaderboardStatistics::year).containsExactly(2026, 2025, null);
        assertThat(stats.getFirst().points()).isEqualTo(tied);
        assertThat(stats.getFirst().average()).isEqualTo(average);
        assertThat(stats.getFirst().medals()).isEqualTo(medals);
        assertThat(stats.getFirst().topResults()).containsExactly(first, second);
        assertThat(stats.get(1).points()).isNull();
        assertThat(stats.get(1).medals()).isNull();
        assertThat(stats.get(1).topResults()).isEmpty();
    }

    @Test
    void oneYearDoesNotCalculateOverall() {
        when(resultService.getResultsForTeam(7L)).thenReturn(List.of(result("2025-01-01"), result("2025-03-01")));
        assertThat(service.getStatistics(7L)).extracting(TeamLeaderboardStatistics::year).containsExactly(2025);
        verify(leaderboardService, never()).getPointsLeaderboard(null);
    }

    @Test
    void noResultsReturnsNoStatistics() {
        when(resultService.getResultsForTeam(7L)).thenReturn(List.of());
        assertThat(service.getStatistics(7L)).isEmpty();
        verify(leaderboardService, never()).getPointsLeaderboard(null);
    }

    private static TeamResultEntry result(String date) {
        TeamResultEntry entry = new TeamResultEntry();
        entry.setQuizDate(date);
        return entry;
    }
}
