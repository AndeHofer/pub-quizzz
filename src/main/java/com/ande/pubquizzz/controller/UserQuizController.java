package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.QuizResultsResponse;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import com.ande.pubquizzz.dto.QuizSummaryDTO;
import com.ande.pubquizzz.service.QuizService;
import com.ande.pubquizzz.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class UserQuizController {

    private final ResultService resultService;
    private final QuizService quizService;

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

    @GetMapping("/{quizId}/detail")
    public ResponseEntity<QuizDetailDTO> getQuizDetail(@PathVariable Long quizId) {
        log.info("GET /api/quizzes/{}/detail", quizId);
        return quizService.getQuizDetailById(quizId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
