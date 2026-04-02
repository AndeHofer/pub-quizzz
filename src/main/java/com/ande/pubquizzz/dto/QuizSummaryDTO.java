package com.ande.pubquizzz.dto;

import lombok.Data;

@Data
public class QuizSummaryDTO {
    private long quizId;
    private String quizTitle;
    private String pubDate;
    private int teamCount;
}
