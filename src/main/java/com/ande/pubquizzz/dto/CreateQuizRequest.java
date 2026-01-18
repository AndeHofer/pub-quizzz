
package com.ande.pubquizzz.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateQuizRequest {
    private LocalDate pubDate;
    private List<QuestionData> questions;

    @Data
    public static class QuestionData {
        private int number;
        private String question;
        private String answer;
        private String note;
        private List<String> hints;
    }
}