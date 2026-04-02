package com.ande.pubquizzz.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class QuizDTO {
    private Long quizId;
    private String title;
    private LocalDate pubDate;
    private LocalDate submitDate;
    private boolean finished;
}
