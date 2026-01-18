package com.ande.pubquizzz.dto;

import lombok.Data;

@Data
public class LeaderboardEntry {
    private int rank;
    private String teamName;
    private Long teamId;
    private Integer totalPoints;
    private Long quizId;
    private String quizDate;
}