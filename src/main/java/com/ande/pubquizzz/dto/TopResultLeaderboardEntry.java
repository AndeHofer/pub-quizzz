package com.ande.pubquizzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopResultLeaderboardEntry {
    private int rank;
    private Long teamId;
    private String teamName;
    private Long quizId;
    private String quizTitle;
    private String quizDate;
    private int totalPoints;
    private int quizRank;
}
