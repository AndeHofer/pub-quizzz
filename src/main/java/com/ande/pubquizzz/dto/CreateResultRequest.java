package com.ande.pubquizzz.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateResultRequest {
    private Long quizId;
    private Long teamId;
    private List<AnswerSubmission> answers;

    @Data
    @NoArgsConstructor
    public static class AnswerSubmission {
        private int questionNumber;
        private int points;
    }
}
