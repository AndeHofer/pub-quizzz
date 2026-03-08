package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.*;
import com.ande.pubquizzz.service.QuizService;
import com.ande.pubquizzz.service.ResultService;
import com.ande.pubquizzz.service.TeamService;
import com.ande.pubquizzz.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final QuizService quizService;
    private final TeamService teamService;
    private final ResultService resultService;

    // ==================== User Management ====================

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDto) {
        try {
            userService.register(userDto);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userService.deleteUser(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("User deleted successfully");
    }

    // ==================== Quiz Management ====================

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizDTO>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @PostMapping("create-quiz")
    public ResponseEntity<String> createQuiz(@RequestBody CreateQuizRequest request) {
        try {
            quizService.createQuiz(request);
            return ResponseEntity.ok("Quiz created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating quiz", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating quiz: " + e.getMessage());
        }
    }

    @GetMapping("/quiz/{id}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/quiz/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        if (!quizService.deleteQuiz(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Quiz deleted successfully");
    }

    @PutMapping("/quiz/{id}")
    public ResponseEntity<String> updateQuiz(@PathVariable Long id, @RequestBody QuizDTO updatedQuiz) {
        return quizService.updateQuiz(id, updatedQuiz.getPubDate(), updatedQuiz.getSubmitDate())
                .map(q -> ResponseEntity.ok("Quiz updated successfully"))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Team Management ====================

    @GetMapping("/teams")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Long id) {
        return teamService.getTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/team")
    public ResponseEntity<String> createTeam(@RequestParam String teamName) {
        try {
            teamService.createTeam(teamName);
            return ResponseEntity.ok("Team created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long id) {
        if (!teamService.deleteTeam(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Team deleted successfully");
    }

    // ==================== Results Management ====================

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
