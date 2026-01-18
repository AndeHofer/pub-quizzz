package com.ande.pubquizzz.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ResultDTO {
    private Long resultsId;
    private Long teamId;
    private String teamName;
    private Long quizId;
    private LocalDate quizDate;
    private Integer answer1Points;
    private Boolean changedAnswer1;
    private Integer answer2Points;
    private Boolean changedAnswer2;
    private Integer answer3Points;
    private Boolean changedAnswer3;
    private Integer answer4Points;
    private Boolean changedAnswer4;
    private Integer answer5Points;
    private Boolean changedAnswer5;
    private Integer answer6Points;
    private Boolean changedAnswer6;
    private Integer answer7Points;
    private Boolean changedAnswer7;
    private Integer answer8Points;
    private Boolean changedAnswer8;
    private Integer totalPoints;
}