package com.ande.pubquizzz.dto;

import lombok.Data;

@Data
public class QuizSummaryDTO {
    private long quizId;
    private String quizTitle;
    private String pubDate;
    private boolean finished;
    private int teamCount;
    private String winnerTeamName;
}
