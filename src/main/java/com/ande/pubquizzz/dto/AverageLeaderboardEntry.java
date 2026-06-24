package com.ande.pubquizzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AverageLeaderboardEntry {
    private int rank;
    private Long teamId;
    private String teamName;
    private double averagePoints;
    private int quizCount;
}
