package com.ande.pubquizzz.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class QuizDTO {
    private Long quizId;
    private LocalDate pubDate;
    private LocalDate submitDate;
    private String creator;
    private boolean finished;
}
