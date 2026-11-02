package com.ande.pubquizzz.dto;

import java.util.List;

public record TeamLeaderboardStatistics(
        Integer year,
        PointsLeaderboardEntry points,
        AverageLeaderboardEntry average,
        MedalLeaderboardEntry medals,
        List<TopResultLeaderboardEntry> topResults
) {
}
