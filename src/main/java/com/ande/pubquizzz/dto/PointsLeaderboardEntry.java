package com.ande.pubquizzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsLeaderboardEntry {
    private int rank;
    private String teamName;
    private int totalPoints;
    private int quizCount;
}
