package com.ande.pubquizzz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateResultRequest {
    @NotNull(message = "Quiz muss ausgewählt werden")
    private Long quizId;

    @NotNull(message = "Team muss ausgewählt werden")
    private Long teamId;

    @NotNull
    @Size(min = 8, max = 8, message = "Es müssen genau 8 Antworten übergeben werden")
    @Valid
    private List<AnswerSubmission> answers;

    @Data
    @NoArgsConstructor
    public static class AnswerSubmission {
        @Min(value = 1, message = "Fragenummer muss zwischen 1 und 8 liegen")
        @Max(value = 8, message = "Fragenummer muss zwischen 1 und 8 liegen")
        private int questionNumber;

        @Min(value = 0, message = "Punkte müssen >= 0 sein")
        @Max(value = 5, message = "Punkte dürfen maximal 5 sein")
        private int points;
    }
}
