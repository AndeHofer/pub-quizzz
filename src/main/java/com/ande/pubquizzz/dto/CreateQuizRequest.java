
package com.ande.pubquizzz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateQuizRequest {
    private LocalDate pubDate;

    @NotNull(message = "Fragen dürfen nicht null sein")
    @Size(min = 8, max = 8, message = "Ein Quiz muss genau 8 Fragen enthalten")
    @Valid
    private List<QuestionData> questions;

    @Data
    public static class QuestionData {
        @Min(value = 1, message = "Fragenummer muss zwischen 1 und 8 liegen")
        @Max(value = 8, message = "Fragenummer muss zwischen 1 und 8 liegen")
        private int number;

        private String questionText;

        private String answer;

        private String answerImageUrl;

        private String note;

        @Valid
        private List<HintData> hints;
    }

    @Data
    public static class HintData {
        private String hintText;
        private String imageUrlAtStart;
        private String imageUrlAsHint;
    }
}
