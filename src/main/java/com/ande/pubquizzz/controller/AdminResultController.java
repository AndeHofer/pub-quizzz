package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.LeaderboardEntry;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminResultController {

    private final ResultService resultService;

    @GetMapping("/results")
    public ResponseEntity<List<ResultDTO>> getAllResults(@RequestParam(required = false) Long quizId) {
        return ResponseEntity.ok(resultService.getResults(quizId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@RequestParam(required = false) Long quizId) {
        return ResponseEntity.ok(resultService.getLeaderboard(quizId));
    }

    @GetMapping("/results/export")
    public ResponseEntity<String> exportResults(@RequestParam(required = false) Long quizId) {
        String csv = resultService.exportResultsCsv(quizId);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"quiz_results.csv\"")
                .body(csv);
    }
}
