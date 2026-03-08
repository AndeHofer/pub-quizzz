package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.repositories.ResultRepository;
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
        log.info("Fetching results{}", quizId != null ? " for quiz " + quizId : "");
        List<Result> results = quizId != null
                ? resultRepository.findByQuiz_QuizId(quizId)
                : resultRepository.findAll();
        return results.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(Long quizId) {
        log.info("Fetching leaderboard{}", quizId != null ? " for quiz " + quizId : "");
        List<Result> results = quizId != null
                ? resultRepository.findByQuizIdOrderByTotalPointsDesc(quizId)
                : resultRepository.findAllOrderByTotalPointsDesc();

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
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
        return leaderboard;
    }

    @Transactional(readOnly = true)
    public String exportResultsCsv(Long quizId) {
        log.info("Exporting results{}", quizId != null ? " for quiz " + quizId : "");
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
        return csv.toString();
    }

    private ResultDTO toDTO(Result result) {
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

    private int calculateTotalPoints(Result result) {
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
