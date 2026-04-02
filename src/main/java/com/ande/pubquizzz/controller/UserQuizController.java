package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.QuizResultsResponse;
import com.ande.pubquizzz.dto.QuizSummaryDTO;
import com.ande.pubquizzz.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class UserQuizController {

    private final ResultService resultService;

    @GetMapping
    public List<QuizSummaryDTO> getQuizSummaries() {
        log.info("GET /api/quizzes");
        return resultService.getQuizSummaries();
    }

    @GetMapping("/{quizId}/results")
    public QuizResultsResponse getQuizResults(@PathVariable Long quizId) {
        log.info("GET /api/quizzes/{}/results", quizId);
        return resultService.getResultsForQuiz(quizId);
    }
}
