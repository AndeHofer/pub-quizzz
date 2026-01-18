package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.*;
import com.ande.pubquizzz.database.repositories.*;
import com.ande.pubquizzz.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== User Management ====================

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String password) {
        log.info("Registering new user: {}", username);

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(appUser);
        log.info("User '{}' registered successfully", username);

        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Fetching all users");
        List<UserDTO> users = userRepository.findAll().stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setUserId(user.getAppUserId());
                    dto.setUsername(user.getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // ==================== Quiz Management ====================

    @PostMapping("create-quiz")
    public ResponseEntity<String> createQuiz(@RequestBody CreateQuizRequest request) {
        log.info("Creating new quiz entry");
        try {
            Quiz quiz = new Quiz();
            quiz.setPubDate(request.getPubDate() != null ? request.getPubDate() : LocalDate.now());
            quiz.setSubmitDate(LocalDate.now());

            // Add each question using the helper method
            for (CreateQuizRequest.QuestionData questionData : request.getQuestions()) {
                quiz.addQuestion(
                        questionData.getNumber(),
                        questionData.getQuestion(),
                        questionData.getAnswer(),
                        questionData.getNote(),
                        questionData.getHints()
                );
            }

            quizRepository.save(quiz);
            log.info("Quiz entry saved successfully with ID: {}", quiz.getQuizId());
            return ResponseEntity.ok("Quiz created successfully");
        } catch (Exception e) {
            log.error("Error creating quiz", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating quiz: " + e.getMessage());
        }
    }

    @GetMapping("/quiz/{id}")
    public ResponseEntity<?> getQuizById(@PathVariable Long id) {
        log.info("Fetching quiz with ID: {}", id);
        return quizRepository.findById(id)
                .map(quiz -> {
                    QuizDTO dto = new QuizDTO();
                    dto.setQuizId(quiz.getQuizId());
                    dto.setPubDate(quiz.getPubDate());
                    dto.setSubmitDate(quiz.getSubmitDate());
                    dto.setQuestionCount(quiz.getQuestions().size());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/quiz/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        log.info("Deleting quiz with ID: {}", id);
        if (!quizRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            quizRepository.deleteById(id);
            log.info("Quiz {} deleted successfully", id);
            return ResponseEntity.ok("Quiz deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting quiz {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting quiz: " + e.getMessage());
        }
    }

    @PutMapping("/quiz/{id}")
    public ResponseEntity<String> updateQuiz(@PathVariable Long id, @RequestBody Quiz updatedQuiz) {
        log.info("Updating quiz with ID: {}", id);
        return quizRepository.findById(id)
                .map(quiz -> {
                    quiz.setPubDate(updatedQuiz.getPubDate());
                    quiz.setSubmitDate(updatedQuiz.getSubmitDate());
                    quizRepository.save(quiz);
                    log.info("Quiz {} updated successfully", id);
                    return ResponseEntity.ok("Quiz updated successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Team Management ====================

    @GetMapping("/teams")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        log.info("Fetching all teams");
        List<TeamDTO> teams = teamRepository.findAll().stream()
                .map(team -> {
                    TeamDTO dto = new TeamDTO();
                    dto.setTeamsId(team.getTeamsId());
                    dto.setTeamName(team.getTeamName());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable Long id) {
        log.info("Fetching team with ID: {}", id);
        return teamRepository.findById(id)
                .map(team -> {
                    TeamDTO dto = new TeamDTO();
                    dto.setTeamsId(team.getTeamsId());
                    dto.setTeamName(team.getTeamName());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/team")
    public ResponseEntity<String> createTeam(@RequestParam String teamName) {
        log.info("Creating new team: {}", teamName);

        if (teamRepository.existsByTeamName(teamName)) {
            return ResponseEntity.badRequest().body("Team name already exists");
        }

        Team team = new Team();
        team.setTeamName(teamName);
        teamRepository.save(team);
        log.info("Team '{}' created successfully", teamName);

        return ResponseEntity.ok("Team created successfully");
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long id) {
        log.info("Deleting team with ID: {}", id);
        if (!teamRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            teamRepository.deleteById(id);
            log.info("Team {} deleted successfully", id);
            return ResponseEntity.ok("Team deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting team {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting team: " + e.getMessage());
        }
    }

    // ==================== Results Management ====================

    @GetMapping("/results")
    public ResponseEntity<List<ResultDTO>> getAllResults(@RequestParam(required = false) Long quizId) {
        log.info("Fetching results" + (quizId != null ? " for quiz " + quizId : ""));

        List<Result> results = quizId != null
                ? resultRepository.findByQuiz_QuizId(quizId)
                : resultRepository.findAll();

        List<ResultDTO> resultDTOs = results.stream()
                .map(this::convertToResultDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@RequestParam(required = false) Long quizId) {
        log.info("Fetching leaderboard" + (quizId != null ? " for quiz " + quizId : ""));

        List<Result> results = quizId != null
                ? resultRepository.findByQuizIdOrderByTotalPointsDesc(quizId)
                : resultRepository.findAllOrderByTotalPointsDesc();

        List<LeaderboardEntry> leaderboard = new java.util.ArrayList<>();
        int rank = 1;

        for (Result result : results) {
            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setRank(rank++);
            entry.setTeamName(result.getTeam().getTeamName());
            entry.setTeamId(result.getTeam().getTeamsId());
            entry.setQuizId(result.getQuiz().getQuizId());
            entry.setQuizDate(result.getQuiz().getPubDate().toString());
            entry.setTotalPoints(calculateTotalPoints(result));
            leaderboard.add(entry);
        }

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/results/export")
    public ResponseEntity<String> exportResults(@RequestParam(required = false) Long quizId) {
        log.info("Exporting results" + (quizId != null ? " for quiz " + quizId : ""));

        List<Result> results = quizId != null
                ? resultRepository.findByQuiz_QuizId(quizId)
                : resultRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("Team,Quiz Date,Q1,Q2,Q3,Q4,Q5,Q6,Q7,Q8,Total\n");

        for (Result result : results) {
            csv.append(result.getTeam().getTeamName()).append(",");
            csv.append(result.getQuiz().getPubDate()).append(",");
            csv.append(result.getAnswer1Points()).append(",");
            csv.append(result.getAnswer2Points()).append(",");
            csv.append(result.getAnswer3Points()).append(",");
            csv.append(result.getAnswer4Points()).append(",");
            csv.append(result.getAnswer5Points()).append(",");
            csv.append(result.getAnswer6Points()).append(",");
            csv.append(result.getAnswer7Points()).append(",");
            csv.append(result.getAnswer8Points()).append(",");
            csv.append(calculateTotalPoints(result)).append("\n");
        }

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"quiz_results.csv\"")
                .body(csv.toString());
    }

    // ==================== Helper Methods ====================

    private ResultDTO convertToResultDTO(Result result) {
        ResultDTO dto = new ResultDTO();
        dto.setResultsId(result.getResultsId());
        dto.setTeamId(result.getTeam().getTeamsId());
        dto.setTeamName(result.getTeam().getTeamName());
        dto.setQuizId(result.getQuiz().getQuizId());
        dto.setQuizDate(result.getQuiz().getPubDate());
        dto.setAnswer1Points(result.getAnswer1Points());
        dto.setChangedAnswer1(result.getChangedAnswer1());
        dto.setAnswer2Points(result.getAnswer2Points());
        dto.setChangedAnswer2(result.getChangedAnswer2());
        dto.setAnswer3Points(result.getAnswer3Points());
        dto.setChangedAnswer3(result.getChangedAnswer3());
        dto.setAnswer4Points(result.getAnswer4Points());
        dto.setChangedAnswer4(result.getChangedAnswer4());
        dto.setAnswer5Points(result.getAnswer5Points());
        dto.setChangedAnswer5(result.getChangedAnswer5());
        dto.setAnswer6Points(result.getAnswer6Points());
        dto.setChangedAnswer6(result.getChangedAnswer6());
        dto.setAnswer7Points(result.getAnswer7Points());
        dto.setChangedAnswer7(result.getChangedAnswer7());
        dto.setAnswer8Points(result.getAnswer8Points());
        dto.setChangedAnswer8(result.getChangedAnswer8());
        dto.setTotalPoints(calculateTotalPoints(result));
        return dto;
    }

    private Integer calculateTotalPoints(Result result) {
        return (result.getAnswer1Points() != null ? result.getAnswer1Points() : 0) +
                (result.getAnswer2Points() != null ? result.getAnswer2Points() : 0) +
                (result.getAnswer3Points() != null ? result.getAnswer3Points() : 0) +
                (result.getAnswer4Points() != null ? result.getAnswer4Points() : 0) +
                (result.getAnswer5Points() != null ? result.getAnswer5Points() : 0) +
                (result.getAnswer6Points() != null ? result.getAnswer6Points() : 0) +
                (result.getAnswer7Points() != null ? result.getAnswer7Points() : 0) +
                (result.getAnswer8Points() != null ? result.getAnswer8Points() : 0);
    }
}