package com.ande.pubquizzz.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizResultEntry {
    private int rank;
    private String teamName;
    private int totalPoints;
    private List<AnswerScoreDTO> answers;
}
