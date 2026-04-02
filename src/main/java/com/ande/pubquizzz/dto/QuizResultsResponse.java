package com.ande.pubquizzz.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizResultsResponse {
    private String quizTitle;
    private List<QuizResultEntry> entries;
}
