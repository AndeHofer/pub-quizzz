package com.ande.pubquizzz.dto;

import lombok.Data;

@Data
public class AnswerScoreDTO {
    private Integer questionNumber;
    private Integer points;
    private Boolean changed;
}
