package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.AllTimeLeaderboardEntry;
import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.QuizResultEntry;
import com.ande.pubquizzz.dto.QuizSummaryDTO;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.dto.TeamResultEntry;
import com.ande.pubquizzz.dto.UpdateResultRequest;
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
    public List<QuizSummaryDTO> getQuizSummaries() {
        log.info("Fetching quiz summaries");
        List<Object[]> rows = quizRepository.findAllWithResultCount();
        return rows.stream().map(row -> {
            Quiz quiz = (Quiz) row[0];
            long count = ((Number) row[1]).longValue();
            QuizSummaryDTO dto = new QuizSummaryDTO();
            dto.setQuizId(quiz.getQuizId());
            dto.setQuizTitle(deriveQuizTitle(quiz.getTitle(), quiz.getPubDate()));
            dto.setPubDate(quiz.getPubDate().toString());
            dto.setTeamCount((int) count);
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResultEntry> getResultsForQuiz(Long quizId) {
        log.info("Fetching results for quiz {}", quizId);
        List<Result> results = resultRepository.findByQuizIdWithTeamAndAnswers(quizId);

        // Sort by totalPoints descending
        List<Result> sorted = results.stream()
                .sorted((a, b) -> b.calculateTotalPoints() - a.calculateTotalPoints())
                .toList();

        // Assign Olympic ranks
        List<QuizResultEntry> entries = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0 && sorted.get(i).calculateTotalPoints() < sorted.get(i - 1).calculateTotalPoints()) {
                rank = i + 1;
            }
            Result r = sorted.get(i);
            QuizResultEntry entry = new QuizResultEntry();
            entry.setRank(rank);
            entry.setTeamName(r.getTeam().getTeamName());
            entry.setTotalPoints(r.calculateTotalPoints());
            entry.setAnswers(r.getAnswers().stream()
                    .map(a -> {
                        AnswerScoreDTO dto = new AnswerScoreDTO();
                        dto.setQuestionNumber(a.getQuestionNumber());
                        dto.setPoints(a.getPoints());
                        dto.setChanged(Boolean.TRUE.equals(a.getChanged()));
                        return dto;
                    })
                    .toList());
            entries.add(entry);
        }
        return entries;
    }

    @Transactional(readOnly = true)
    public List<ResultDTO> getResults(Long quizId) {
        log.info("Fetching results{}", quizSuffix(quizId));
        return loadResults(quizId).stream().map(resultMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AllTimeLeaderboardEntry> getAllTimeLeaderboard() {
        log.info("Fetching all-time leaderboard");
        List<Object[]> rows = resultRepository.findAllTimeLeaderboardRaw();
        List<AllTimeLeaderboardEntry> leaderboard = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            AllTimeLeaderboardEntry entry = new AllTimeLeaderboardEntry();
            entry.setRank(i + 1);
            entry.setTeamName((String) row[0]);
            entry.setTotalPoints(((Number) row[1]).intValue());
            entry.setQuizCount(((Number) row[2]).intValue());
            leaderboard.add(entry);
        }
        return leaderboard;
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

    @Transactional
    public void deleteResult(Long id) {
        log.info("Deleting result id={}", id);
        if (!resultRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ergebnis nicht gefunden: " + id);
        }
        resultRepository.deleteById(id);
    }

    @Transactional
    public ResultDTO updateResult(Long id, UpdateResultRequest req) {
        log.info("Updating result id={}", id);
        Result result = resultRepository.findByIdWithAnswers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ergebnis nicht gefunden: " + id));

        for (UpdateResultRequest.AnswerSubmission submission : req.getAnswers()) {
            int newPoints = submission.getPoints();
            result.getAnswers().stream()
                    .filter(ra -> ra.getQuestionNumber() == submission.getQuestionNumber())
                    .findFirst()
                    .ifPresent(ra -> {
                        if (ra.getPoints() != newPoints) {
                            ra.setPoints(newPoints);
                            ra.setChanged(true);
                        }
                    });
        }

        Result saved = resultRepository.save(result);
        return resultMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamResultEntry> getResultsForTeam(String teamName) {
        return resultRepository.findByTeamNameOrderByPubDateDesc(teamName)
                .stream()
                .map(r -> {
                    TeamResultEntry entry = new TeamResultEntry();
                    entry.setQuizDate(r.getQuiz().getPubDate().toString());
                    entry.setQuizTitle(deriveQuizTitle(r.getQuiz().getTitle(), r.getQuiz().getPubDate()));
                    entry.setTotalPoints(r.calculateTotalPoints());
                    entry.setAnswers(r.getAnswers().stream()
                            .map(a -> {
                                AnswerScoreDTO dto = new AnswerScoreDTO();
                                dto.setQuestionNumber(a.getQuestionNumber());
                                dto.setPoints(a.getPoints());
                                dto.setChanged(Boolean.TRUE.equals(a.getChanged()));
                                return dto;
                            })
                            .toList());
                    return entry;
                })
                .toList();
    }

    private static final String[] GERMAN_MONTHS = {
        "Jänner", "Februar", "März", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember"
    };

    private String deriveQuizTitle(String title, java.time.LocalDate pubDate) {
        if (title != null && !title.isBlank()) return title;
        return pubDate.getYear() + " " + GERMAN_MONTHS[pubDate.getMonthValue() - 1];
    }

    private List<Result> loadResults(Long quizId) {
        return quizId != null
                ? resultRepository.findByQuiz_QuizId(quizId)
                : resultRepository.findAll();
    }

    private String quizSuffix(Long quizId) {
        return quizId != null ? " for quiz " + quizId : "";
    }
}
