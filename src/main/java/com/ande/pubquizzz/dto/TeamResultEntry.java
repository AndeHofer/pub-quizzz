package com.ande.pubquizzz.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamResultEntry {
    private Long quizId;
    private String quizDate;
    private String quizTitle;
    private int totalPoints;
    private List<AnswerScoreDTO> answers;
}
