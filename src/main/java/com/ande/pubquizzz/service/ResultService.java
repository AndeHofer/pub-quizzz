package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.LeaderboardEntry;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final QuizRepository quizRepository;
    private final TeamRepository teamRepository;
    private final ResultMapper resultMapper;

    @Transactional(readOnly = true)
    public List<ResultDTO> getResults(Long quizId) {
        log.info("Fetching results{}", quizSuffix(quizId));
        return loadResults(quizId).stream().map(resultMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(Long quizId) {
        log.info("Fetching leaderboard{}", quizSuffix(quizId));
        List<Result> results = quizId != null
                ? resultRepository.findByQuizIdOrderByTotalPointsDesc(quizId)
                : resultRepository.findAllOrderByTotalPointsDesc();

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        for (int rank = 1; rank <= results.size(); rank++) {
            leaderboard.add(resultMapper.toLeaderboardEntry(results.get(rank - 1), rank));
        }
        return leaderboard;
    }

    @Transactional(readOnly = true)
    public String exportResultsCsv(Long quizId) {
        log.info("Exporting results{}", quizSuffix(quizId));
        List<Result> results = loadResults(quizId);

        StringBuilder csv = new StringBuilder();
        csv.append("Team,Quiz Date,Q1,Q2,Q3,Q4,Q5,Q6,Q7,Q8,Total\n");
        for (Result result : results) {
            List<ResultAnswer> answers = result.getAnswers();
            csv.append(escapeCsv(result.getTeam().getTeamName())).append(",");
            csv.append(result.getQuiz().getPubDate()).append(",");
            for (int i = 0; i < 8; i++) {
                int points = i < answers.size() ? answers.get(i).getPoints() : 0;
                csv.append(points).append(",");
            }
            csv.append(totalPoints(result)).append("\n");
        }
        return csv.toString();
    }

    @Transactional
    public ResultDTO createResult(CreateResultRequest req) {
        log.info("Creating result for quizId={} teamId={}", req.getQuizId(), req.getTeamId());

        var quizOpt = quizRepository.findById(req.getQuizId());
        if (quizOpt.isEmpty()) throw new ResourceNotFoundException("Quiz nicht gefunden: " + req.getQuizId());
        var teamOpt = teamRepository.findById(req.getTeamId());
        if (teamOpt.isEmpty()) throw new ResourceNotFoundException("Team nicht gefunden: " + req.getTeamId());

        // Check for duplicate result
        if (resultRepository.findByTeam_TeamsIdAndQuiz_QuizId(req.getTeamId(), req.getQuizId()).isPresent()) {
            throw new BusinessValidationException("Ergebnis für dieses Team und Quiz existiert bereits");
        }

        var answers = req.getAnswers();
        boolean[] seen = new boolean[9];
        for (CreateResultRequest.AnswerSubmission a : answers) {
            int qn = a.getQuestionNumber();
            if (seen[qn]) throw new BusinessValidationException("Doppelte Frage: " + qn);
            seen[qn] = true;
        }

        Result result = new Result();
        result.setQuiz(quizOpt.get());
        result.setTeam(teamOpt.get());

        List<ResultAnswer> resultAnswers = new ArrayList<>();
        for (CreateResultRequest.AnswerSubmission a : answers) {
            ResultAnswer ra = new ResultAnswer();
            ra.setQuestionNumber(a.getQuestionNumber());
            ra.setPoints(a.getPoints());
            ra.setChanged(false);
            ra.setResult(result);
            resultAnswers.add(ra);
        }
        result.setAnswers(resultAnswers);

        Result saved = resultRepository.save(result);
        return resultMapper.toDTO(saved);
    }

    private List<Result> loadResults(Long quizId) {
        return quizId != null
                ? resultRepository.findByQuiz_QuizId(quizId)
                : resultRepository.findAll();
    }

    private int totalPoints(Result result) {
        return result.calculateTotalPoints();
    }

    private String quizSuffix(Long quizId) {
        return quizId != null ? " for quiz " + quizId : "";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
