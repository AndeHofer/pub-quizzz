package com.ande.pubquizzz.service;

import com.ande.pubquizzz.dto.TeamLeaderboardStatistics;
import com.ande.pubquizzz.dto.TeamResultEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TeamStatisticsService {
    private final ResultService resultService;
    private final LeaderboardService leaderboardService;

    @Transactional(readOnly = true)
    public List<TeamLeaderboardStatistics> getStatistics(Long teamId) {
        List<Integer> years = resultService.getResultsForTeam(teamId).stream()
                .map(TeamResultEntry::getQuizDate)
                .map(LocalDate::parse)
                .map(LocalDate::getYear)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        List<TeamLeaderboardStatistics> statistics = new ArrayList<>();
        for (Integer year : years) {
            statistics.add(getStatisticsForPeriod(teamId, year));
        }
        if (years.size() > 1) {
            statistics.add(getStatisticsForPeriod(teamId, null));
        }
        return statistics;
    }

    private TeamLeaderboardStatistics getStatisticsForPeriod(Long teamId, Integer year) {
        return new TeamLeaderboardStatistics(
                year,
                leaderboardService.getPointsLeaderboard(year).stream()
                        .filter(entry -> Objects.equals(entry.getTeamId(), teamId)).findFirst().orElse(null),
                leaderboardService.getAverageLeaderboard(year).stream()
                        .filter(entry -> Objects.equals(entry.getTeamId(), teamId)).findFirst().orElse(null),
                leaderboardService.getMedalLeaderboard(year).stream()
                        .filter(entry -> Objects.equals(entry.getTeamId(), teamId)).findFirst().orElse(null),
                leaderboardService.getTopResultsLeaderboard(year).stream()
                        .filter(entry -> Objects.equals(entry.getTeamId(), teamId)).toList()
        );
    }
}
