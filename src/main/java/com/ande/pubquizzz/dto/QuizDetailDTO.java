package com.ande.pubquizzz.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class QuizDetailDTO {
    private Long quizId;
    private LocalDate pubDate;
    private LocalDate submitDate;
    private String creator;
    private List<QuestionDetailDTO> questions;

    @Data
    public static class QuestionDetailDTO {
        private int number;
        private String questionText;
        private String answer;
        private String answerImageUrl;
        private String note;
        private List<HintDetailDTO> hints;
    }

    @Data
    public static class HintDetailDTO {
        private String hintText;
        private String imageUrlAtStart;
        private String imageUrlAsHint;
    }
}
