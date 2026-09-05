package com.ande.pubquizzz.service;

import com.ande.pubquizzz.cache.InvalidateAllAppCaches;
import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.QuizResultEntry;
import com.ande.pubquizzz.dto.QuizResultsResponse;
import com.ande.pubquizzz.dto.QuizSummaryDTO;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.dto.TeamResultEntry;
import com.ande.pubquizzz.dto.UpdateResultRequest;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.ResultMapper;
import com.ande.pubquizzz.mapper.QuizFinishedChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static com.ande.pubquizzz.config.CacheConfig.QUIZ_SUMMARIES;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_FOR_QUIZ;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_FOR_TEAM;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final QuizRepository quizRepository;
    private final TeamRepository teamRepository;
    private final ResultMapper resultMapper;

    /**
     * Tiebreaker order: total points DESC, count of 5-point answers DESC, count of 3-point answers DESC.
     */
    private static final Comparator<Result> RESULT_COMPARATOR =
            Comparator.<Result, Integer>comparing(Result::calculateTotalPoints).reversed()
                    .thenComparing(Comparator.<Result, Long>comparing(r -> r.countAnswersWithPoints(5)).reversed())
                    .thenComparing(Comparator.<Result, Long>comparing(r -> r.countAnswersWithPoints(3)).reversed());

    @Transactional(readOnly = true)
    @Cacheable(QUIZ_SUMMARIES)
    public List<QuizSummaryDTO> getQuizSummaries() {
        log.debug("Fetching quiz summaries");
        List<Object[]> rows = quizRepository.findAllWithResultCount();

        // Collect quiz IDs that have at least one result, then fetch scores in one query
        List<Long> quizIdsWithResults = rows.stream()
                .filter(row -> ((Number) row[1]).longValue() > 0)
                .map(row -> ((Quiz) row[0]).getQuizId())
                .toList();

        Map<Long, Object[]> winnerMap = new java.util.HashMap<>();
        if (!quizIdsWithResults.isEmpty()) {
            List<Object[]> scoreRows = resultRepository.findScoresByQuizIds(quizIdsWithResults);
            // scoreRows: [quizId, teamId, teamName, totalPoints, fivesCount, threesCount]
            // Group by quizId, pick the row with best tiebreaker order
            Map<Long, Object[]> bestRow = new java.util.HashMap<>();
            for (Object[] sr : scoreRows) {
                Long qId = ((Number) sr[0]).longValue();
                bestRow.merge(qId, sr, (existing, candidate) ->
                        RankingUtils.compareScoreRowsDesc(candidate, existing, 3, 4, 5) < 0 ? candidate : existing);
            }
            winnerMap.putAll(bestRow);
        }

        return rows.stream().map(row -> {
            Quiz quiz = (Quiz) row[0];
            long count = ((Number) row[1]).longValue();
            QuizSummaryDTO dto = new QuizSummaryDTO();
            dto.setQuizId(quiz.getQuizId());
            dto.setQuizTitle(QuizTitleFormatter.deriveQuizTitle(quiz.getPubDate()));
            dto.setPubDate(quiz.getPubDate().toString());
            dto.setFinished(QuizFinishedChecker.isFinished(quiz));
            dto.setTeamCount((int) count);
            Object[] winnerRow = winnerMap.get(quiz.getQuizId());
            if (winnerRow != null) {
                dto.setWinnerTeamId(((Number) winnerRow[1]).longValue());
                dto.setWinnerTeamName((String) winnerRow[2]);
            }
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RESULTS_FOR_QUIZ, key = "#quizId")
    public QuizResultsResponse getResultsForQuiz(Long quizId) {
        log.debug("Fetching results for quiz {}", quizId);
        List<Result> results = resultRepository.findByQuizIdWithTeamAndAnswers(quizId);

        // Derive quiz title — from results if any, otherwise look up the quiz directly
        String quizTitle;
        if (!results.isEmpty()) {
            Quiz q = results.get(0).getQuiz();
            quizTitle = QuizTitleFormatter.deriveQuizTitle(q.getPubDate());
        } else {
            quizTitle = quizRepository.findById(quizId)
                    .map(q -> QuizTitleFormatter.deriveQuizTitle(q.getPubDate()))
                    .orElse("");
        }

        // Sort by tiebreaker comparator: total points DESC, fives DESC, threes DESC
        List<Result> sorted = results.stream()
                .sorted(RESULT_COMPARATOR)
                .toList();

        // Assign Olympic ranks — teams are equal in rank only when all three criteria match
        List<QuizResultEntry> entries = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0 && RESULT_COMPARATOR.compare(sorted.get(i), sorted.get(i - 1)) != 0) {
                rank = i + 1;
            }
            Result r = sorted.get(i);
            QuizResultEntry entry = new QuizResultEntry();
            entry.setRank(rank);
            entry.setTeamId(r.getTeam().getTeamsId());
            entry.setTeamName(r.getTeam().getTeamName());
            entry.setTotalPoints(r.calculateTotalPoints());
            entry.setAnswers(mapAnswerScores(r));
            entries.add(entry);
        }

        QuizResultsResponse response = new QuizResultsResponse();
        response.setQuizTitle(quizTitle);
        response.setEntries(entries);
        return response;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RESULTS_LIST, key = "#quizId == null ? 'all' : #quizId")
    public List<ResultDTO> getResults(Long quizId) {
        log.debug("Fetching results{}", quizId != null ? " for quiz " + quizId : "");
        List<Result> results = quizId != null
                ? resultRepository.findByQuiz_QuizId(quizId)
                : resultRepository.findAll();
        return results.stream().map(resultMapper::toDTO).toList();
    }

    @Transactional
    @InvalidateAllAppCaches
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

        validateCreateAnswers(req.getAnswers());

        Result result = new Result();
        result.setQuiz(quizOpt.get());
        result.setTeam(teamOpt.get());

        List<ResultAnswer> resultAnswers = new ArrayList<>();
        for (CreateResultRequest.AnswerSubmission a : req.getAnswers()) {
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
    @InvalidateAllAppCaches
    public void deleteResult(Long id) {
        log.info("Deleting result id={}", id);
        if (!resultRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ergebnis nicht gefunden: " + id);
        }
        resultRepository.deleteById(id);
    }

    @Transactional
    @InvalidateAllAppCaches
    public ResultDTO updateResult(Long id, UpdateResultRequest req) {
        log.info("Updating result id={}", id);
        Result result = resultRepository.findByIdWithAnswers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ergebnis nicht gefunden: " + id));

        validateUpdateAnswers(req.getAnswers());

        Map<Integer, ResultAnswer> answersByQuestionNumber = result.getAnswers().stream()
                .collect(Collectors.toMap(ResultAnswer::getQuestionNumber, Function.identity()));

        for (UpdateResultRequest.AnswerSubmission submission : req.getAnswers()) {
            int newPoints = submission.getPoints();
            ResultAnswer answer = answersByQuestionNumber.get(submission.getQuestionNumber());
            if (answer != null && answer.getPoints() != newPoints) {
                answer.setPoints(newPoints);
                answer.setChanged(true);
            }
        }

        Result saved = resultRepository.save(result);
        return resultMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RESULTS_FOR_TEAM, key = "#teamId")
    public List<TeamResultEntry> getResultsForTeam(Long teamId) {
        List<Result> results = resultRepository.findByTeamIdOrderByPubDateDesc(teamId);
        if (results.isEmpty()) {
            return List.of();
        }

        List<Long> quizIds = results.stream()
                .map(r -> r.getQuiz().getQuizId())
                .filter(java.util.Objects::nonNull)
                .toList();

        List<Object[]> rawScoreRows = quizIds.isEmpty()
                ? List.of()
                : java.util.Optional.ofNullable(resultRepository.findScoresByQuizIds(quizIds)).orElse(List.of());

        Map<Long, List<Object[]>> quizScores = rawScoreRows.stream()
                .collect(Collectors.groupingBy(row -> ((Number) row[0]).longValue()));

        return results.stream().map(r -> {
            TeamResultEntry entry = new TeamResultEntry();
            entry.setTeamId(r.getTeam().getTeamsId());
            entry.setTeamName(r.getTeam().getTeamName());
            entry.setQuizId(r.getQuiz().getQuizId());
            entry.setQuizDate(r.getQuiz().getPubDate().toString());
            entry.setQuizTitle(QuizTitleFormatter.deriveQuizTitle(r.getQuiz().getPubDate()));
            entry.setTotalPoints(r.calculateTotalPoints());
            entry.setAnswers(mapAnswerScores(r));

            List<Object[]> scores = new ArrayList<>(quizScores.getOrDefault(r.getQuiz().getQuizId(), List.of()));
            if (scores.isEmpty()) {
                entry.setQuizRank(1);
                entry.setParticipantCount(1);
                return entry;
            }

            scores.sort((left, right) -> RankingUtils.compareScoreRowsDesc(left, right, 3, 4, 5));

            entry.setParticipantCount(scores.size());
            int rank = 1;
            for (int i = 0; i < scores.size(); i++) {
                if (i > 0) {
                    Object[] previous = scores.get(i - 1);
                    Object[] current = scores.get(i);
                    boolean sameRank = RankingUtils.hasSameScore(previous, current, 3, 4, 5);
                    if (!sameRank) {
                        rank = i + 1;
                    }
                }
                Long currentTeamId = ((Number) scores.get(i)[1]).longValue();
                if (currentTeamId.equals(r.getTeam().getTeamsId())) {
                    entry.setQuizRank(rank);
                    break;
                }
            }

            if (entry.getQuizRank() == 0) {
                entry.setQuizRank(1);
            }
            return entry;
        }).toList();
    }

    private List<AnswerScoreDTO> mapAnswerScores(Result result) {
        return result.getAnswers().stream()
                .map(a -> {
                    AnswerScoreDTO dto = new AnswerScoreDTO();
                    dto.setQuestionNumber(a.getQuestionNumber());
                    dto.setPoints(a.getPoints());
                    return dto;
                })
                .toList();
    }

    private void validateCreateAnswers(List<CreateResultRequest.AnswerSubmission> answers) {
        validateAnswers(
                answers,
                CreateResultRequest.AnswerSubmission::getQuestionNumber,
                CreateResultRequest.AnswerSubmission::getPoints
        );
    }

    private void validateUpdateAnswers(List<UpdateResultRequest.AnswerSubmission> answers) {
        validateAnswers(
                answers,
                UpdateResultRequest.AnswerSubmission::getQuestionNumber,
                UpdateResultRequest.AnswerSubmission::getPoints
        );
    }

    private <T> void validateAnswers(
            List<T> answers,
            ToIntFunction<T> questionNumberExtractor,
            ToIntFunction<T> pointsExtractor
    ) {
        boolean[] seen = new boolean[QUESTION_COUNT + 1];
        for (T answer : answers) {
            int questionNumber = questionNumberExtractor.applyAsInt(answer);
            ensureValidQuestionNumber(questionNumber);
            ensureAllowedPoints(pointsExtractor.applyAsInt(answer));
            if (seen[questionNumber]) {
                throw new BusinessValidationException("Doppelte Frage: " + questionNumber);
            }
            seen[questionNumber] = true;
        }
        ensureAllQuestionsPresent(seen);
    }

    private void ensureValidQuestionNumber(int questionNumber) {
        if (questionNumber < 1 || questionNumber > QUESTION_COUNT) {
            throw new BusinessValidationException("Fragenummer muss zwischen 1 und 8 liegen");
        }
    }

    private void ensureAllowedPoints(int points) {
        if (points != 0 && points != 1 && points != 2 && points != 3 && points != 5) {
            throw new BusinessValidationException("Punkte müssen einer der Werte 0, 1, 2, 3 oder 5 sein");
        }
    }

    private void ensureAllQuestionsPresent(boolean[] seen) {
        for (int i = 1; i <= QUESTION_COUNT; i++) {
            if (!seen[i]) {
                throw new BusinessValidationException("Fehlende Frage: " + i);
            }
        }
    }

    private static final int QUESTION_COUNT = 8;
}
