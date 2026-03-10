package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.LeaderboardEntry;
import com.ande.pubquizzz.dto.ResultDTO;
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

    @Transactional(readOnly = true)
    public List<ResultDTO> getResults(Long quizId) {
        log.info("Fetching results{}", quizSuffix(quizId));
        return loadResults(quizId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(Long quizId) {
        log.info("Fetching leaderboard{}", quizSuffix(quizId));
        List<Result> results = quizId != null
                ? resultRepository.findByQuizIdOrderByTotalPointsDesc(quizId)
                : resultRepository.findAllOrderByTotalPointsDesc();

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        for (int rank = 1; rank <= results.size(); rank++) {
            leaderboard.add(toLeaderboardEntry(results.get(rank - 1), rank));
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

    private List<Result> loadResults(Long quizId) {
        return quizId != null
                ? resultRepository.findByQuiz_QuizId(quizId)
                : resultRepository.findAll();
    }

    private ResultDTO toDTO(Result result) {
        ResultDTO dto = new ResultDTO();
        dto.setResultsId(result.getResultsId());
        dto.setTeamId(result.getTeam().getTeamsId());
        dto.setTeamName(result.getTeam().getTeamName());
        dto.setQuizId(result.getQuiz().getQuizId());
        dto.setQuizDate(result.getQuiz().getPubDate());
        dto.setAnswers(result.getAnswers().stream().map(this::toAnswerScoreDTO).toList());
        dto.setTotalPoints(totalPoints(result));
        return dto;
    }

    private AnswerScoreDTO toAnswerScoreDTO(ResultAnswer a) {
        AnswerScoreDTO dto = new AnswerScoreDTO();
        dto.setQuestionNumber(a.getQuestionNumber());
        dto.setPoints(a.getPoints());
        dto.setChanged(a.getChanged());
        return dto;
    }

    private LeaderboardEntry toLeaderboardEntry(Result result, int rank) {
        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setRank(rank);
        entry.setTeamName(result.getTeam().getTeamName());
        entry.setTeamId(result.getTeam().getTeamsId());
        entry.setQuizId(result.getQuiz().getQuizId());
        entry.setQuizDate(result.getQuiz().getPubDate().toString());
        entry.setTotalPoints(totalPoints(result));
        return entry;
    }

    private int totalPoints(Result result) {
        return result.getAnswers().stream().mapToInt(ResultAnswer::getPoints).sum();
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
