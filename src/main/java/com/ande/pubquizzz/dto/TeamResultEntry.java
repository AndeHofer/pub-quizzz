package com.ande.pubquizzz.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamResultEntry {
    private String quizDate;
    private int totalPoints;
    private List<AnswerScoreDTO> answers;
}
