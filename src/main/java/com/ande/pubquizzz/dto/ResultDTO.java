package com.ande.pubquizzz.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ResultDTO {
    private Long resultsId;
    private Long teamId;
    private String teamName;
    private Long quizId;
    private LocalDate quizDate;
    private List<AnswerScoreDTO> answers;
    private Integer totalPoints;
}
