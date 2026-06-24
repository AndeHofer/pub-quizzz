package com.ande.pubquizzz.service;

import com.ande.pubquizzz.cache.InvalidateAllAppCaches;
import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.PointsLeaderboardEntry;
import com.ande.pubquizzz.dto.AverageLeaderboardEntry;
import com.ande.pubquizzz.dto.AnswerScoreDTO;
import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.MedalLeaderboardEntry;
import com.ande.pubquizzz.dto.QuizResultEntry;
import com.ande.pubquizzz.dto.QuizResultsResponse;
import com.ande.pubquizzz.dto.QuizSummaryDTO;
import com.ande.pubquizzz.dto.ResultDTO;
import com.ande.pubquizzz.dto.TeamResultEntry;
import com.ande.pubquizzz.dto.TopResultLeaderboardEntry;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static com.ande.pubquizzz.config.CacheConfig.AVERAGE_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.MEDAL_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.POINTS_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.QUIZ_SUMMARIES;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_FOR_QUIZ;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_FOR_TEAM;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_LIST;
import static com.ande.pubquizzz.config.CacheConfig.TOP_RESULTS_LEADERBOARD;

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

        java.util.Map<Long, Object[]> winnerMap = new java.util.HashMap<>();
        if (!quizIdsWithResults.isEmpty()) {
            List<Object[]> scoreRows = resultRepository.findScoresByQuizIds(quizIdsWithResults);
            // scoreRows: [quizId, teamId, teamName, totalPoints, fivesCount, threesCount]
            // Group by quizId, pick the row with best tiebreaker order
            java.util.Map<Long, Object[]> bestRow = new java.util.HashMap<>();
            for (Object[] sr : scoreRows) {
                Long qId = ((Number) sr[0]).longValue();
                bestRow.merge(qId, sr, (existing, candidate) -> {
                    return compareScoresDesc(
                            scoreValue(candidate, 3),
                            scoreValue(candidate, 4),
                            scoreValue(candidate, 5),
                            scoreValue(existing, 3),
                            scoreValue(existing, 4),
                            scoreValue(existing, 5)
                    ) < 0 ? candidate : existing;
                });
            }
            winnerMap.putAll(bestRow);
        }

        return rows.stream().map(row -> {
            Quiz quiz = (Quiz) row[0];
            long count = ((Number) row[1]).longValue();
            QuizSummaryDTO dto = new QuizSummaryDTO();
            dto.setQuizId(quiz.getQuizId());
            dto.setQuizTitle(deriveQuizTitle(quiz.getPubDate()));
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
            quizTitle = deriveQuizTitle(q.getPubDate());
        } else {
            quizTitle = quizRepository.findById(quizId)
                    .map(q -> deriveQuizTitle(q.getPubDate()))
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

    @Transactional(readOnly = true)
    @Cacheable(POINTS_LEADERBOARD)
    public List<PointsLeaderboardEntry> getPointsLeaderboard() {
        log.debug("Fetching points leaderboard");
        List<Object[]> rows = new ArrayList<>(resultRepository.findLeaderboardRaw());
        // Sort rows defensively by totalPoints (DESC) and teamName (ASC)
        rows.sort(Comparator.comparingInt((Object[] row) -> ((Number) row[2]).intValue()).reversed()
                .thenComparing(row -> (String) row[1]));

        List<PointsLeaderboardEntry> leaderboard = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) {
                int previousPoints = ((Number) rows.get(i - 1)[2]).intValue();
                int currentPoints = ((Number) rows.get(i)[2]).intValue();
                if (currentPoints != previousPoints) {
                    rank = i + 1;
                }
            }
            Object[] row = rows.get(i);
            PointsLeaderboardEntry entry = new PointsLeaderboardEntry();
            entry.setRank(rank);
            entry.setTeamId(((Number) row[0]).longValue());
            entry.setTeamName((String) row[1]);
            entry.setTotalPoints(((Number) row[2]).intValue());
            entry.setQuizCount(((Number) row[3]).intValue());
            leaderboard.add(entry);
        }
        return leaderboard;
    }

    @Transactional(readOnly = true)
    @Cacheable(AVERAGE_LEADERBOARD)
    public List<AverageLeaderboardEntry> getAverageLeaderboard() {
        log.debug("Fetching average leaderboard");
        List<Object[]> rows = resultRepository.findLeaderboardRaw();

        List<AverageTeamStats> stats = rows.stream()
                .map(row -> {
                    Long teamId = ((Number) row[0]).longValue();
                    String teamName = (String) row[1];
                    int quizCount = ((Number) row[3]).intValue();
                    double averagePoints = quizCount == 0 ? 0.0 : ((Number) row[2]).doubleValue() / quizCount;
                    return new AverageTeamStats(teamId, teamName, quizCount, averagePoints);
                })
                .sorted(Comparator.comparingDouble(AverageTeamStats::averagePoints).reversed())
                .toList();

        List<AverageLeaderboardEntry> leaderboard = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < stats.size(); i++) {
            if (i > 0 && Double.compare(stats.get(i).averagePoints(), stats.get(i - 1).averagePoints()) != 0) {
                rank = i + 1;
            }
            AverageTeamStats stat = stats.get(i);
            AverageLeaderboardEntry entry = new AverageLeaderboardEntry();
            entry.setRank(rank);
            entry.setTeamId(stat.teamId());
            entry.setTeamName(stat.teamName());
            entry.setAveragePoints(stat.averagePoints());
            entry.setQuizCount(stat.quizCount());
            leaderboard.add(entry);
        }
        return leaderboard;
    }

    @Transactional(readOnly = true)
    @Cacheable(MEDAL_LEADERBOARD)
    public List<MedalLeaderboardEntry> getMedalLeaderboard() {
        log.debug("Fetching medal leaderboard");
        List<Object[]> rows = resultRepository.findPerQuizTeamScoreBreakdownRaw();

        Map<Long, List<QuizTeamScore>> totalsByQuiz = new HashMap<>();
        for (Object[] row : rows) {
            Long quizId = ((Number) row[0]).longValue();
            Long teamId = ((Number) row[1]).longValue();
            String teamName = (String) row[2];
            int totalPoints = ((Number) row[3]).intValue();
            int fiveCount = ((Number) row[4]).intValue();
            int threeCount = ((Number) row[5]).intValue();
            totalsByQuiz.computeIfAbsent(quizId, ignored -> new ArrayList<>())
                    .add(new QuizTeamScore(teamId, teamName, totalPoints, fiveCount, threeCount));
        }

        Map<Long, MedalAccumulator> medalsByTeam = new HashMap<>();
        Map<Long, String> teamNamesById = new HashMap<>();
        totalsByQuiz.values().forEach(quizTotals -> {
            List<QuizTeamScore> sorted = quizTotals.stream()
                    .sorted(Comparator.comparingInt(QuizTeamScore::totalPoints).reversed()
                            .thenComparing(Comparator.comparingInt(QuizTeamScore::fiveCount).reversed())
                            .thenComparing(Comparator.comparingInt(QuizTeamScore::threeCount).reversed()))
                    .toList();

            int rank = 1;
            for (int i = 0; i < sorted.size(); i++) {
                QuizTeamScore current = sorted.get(i);
                if (i > 0 && !sameQuizRankMetrics(current, sorted.get(i - 1))) {
                    rank = i + 1;
                }

                MedalAccumulator accumulator = medalsByTeam.computeIfAbsent(
                        current.teamId(),
                        ignored -> new MedalAccumulator()
                );
                teamNamesById.putIfAbsent(current.teamId(), current.teamName());

                if (rank == 1) {
                    accumulator.goldCount++;
                } else if (rank == 2) {
                    accumulator.silverCount++;
                } else if (rank == 3) {
                    accumulator.bronzeCount++;
                }
            }
        });

        List<Map.Entry<Long, MedalAccumulator>> sortedTeams = medalsByTeam.entrySet().stream()
                .filter(e -> e.getValue().goldCount > 0 || e.getValue().silverCount > 0 || e.getValue().bronzeCount > 0)
                .sorted(Comparator.<Map.Entry<Long, MedalAccumulator>>comparingInt(e -> e.getValue().goldCount).reversed()
                        .thenComparing(Comparator.comparingInt((Map.Entry<Long, MedalAccumulator> e) -> e.getValue().silverCount).reversed())
                        .thenComparing(Comparator.comparingInt((Map.Entry<Long, MedalAccumulator> e) -> e.getValue().bronzeCount).reversed()))
                .toList();

        List<MedalLeaderboardEntry> leaderboard = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sortedTeams.size(); i++) {
            if (i > 0) {
                MedalAccumulator previous = sortedTeams.get(i - 1).getValue();
                MedalAccumulator current = sortedTeams.get(i).getValue();
                if (current.goldCount != previous.goldCount
                        || current.silverCount != previous.silverCount
                        || current.bronzeCount != previous.bronzeCount) {
                    rank = i + 1;
                }
            }
            Map.Entry<Long, MedalAccumulator> row = sortedTeams.get(i);
            MedalAccumulator acc = row.getValue();
            MedalLeaderboardEntry entry = new MedalLeaderboardEntry();
            entry.setRank(rank);
            entry.setTeamId(row.getKey());
            entry.setTeamName(teamNamesById.get(row.getKey()));
            entry.setGoldCount(acc.goldCount);
            entry.setSilverCount(acc.silverCount);
            entry.setBronzeCount(acc.bronzeCount);
            leaderboard.add(entry);
        }
        return leaderboard;
    }

    @Transactional(readOnly = true)
    @Cacheable(TOP_RESULTS_LEADERBOARD)
    public List<TopResultLeaderboardEntry> getTopResultsLeaderboard() {
        log.debug("Fetching top results leaderboard");
        List<Object[]> rows = new ArrayList<>(resultRepository.findTopResultsScoreBreakdownRaw());
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Object[]>> rowsByQuizId = rows.stream()
                .collect(Collectors.groupingBy(row -> ((Number) row[0]).longValue()));

        Map<Long, Map<Long, Integer>> quizRanksByQuizAndTeam = new HashMap<>();
        for (Map.Entry<Long, List<Object[]>> entry : rowsByQuizId.entrySet()) {
            List<Object[]> quizRows = new ArrayList<>(entry.getValue());
            quizRows.sort(ResultService::compareTopResultRowsByQuizTieBreakDesc);

            Map<Long, Integer> rankByTeamId = new HashMap<>();
            int quizRank = 1;
            for (int i = 0; i < quizRows.size(); i++) {
                if (i > 0 && !hasSameTopResultQuizScore(quizRows.get(i - 1), quizRows.get(i))) {
                    quizRank = i + 1;
                }
                Long teamId = ((Number) quizRows.get(i)[2]).longValue();
                rankByTeamId.putIfAbsent(teamId, quizRank);
            }
            quizRanksByQuizAndTeam.put(entry.getKey(), rankByTeamId);
        }

        rows.sort(ResultService::compareTopResultRowsForGlobalList);

        List<Object[]> topRows = rows.stream().limit(10).toList();

        List<TopResultLeaderboardEntry> leaderboard = new ArrayList<>();
        int rank = 1;
        Integer previousTotalPoints = null;
        for (int i = 0; i < topRows.size(); i++) {
            Object[] row = topRows.get(i);
            int totalPoints = ((Number) row[4]).intValue();
            if (i == 0) {
                previousTotalPoints = totalPoints;
            } else if (previousTotalPoints != null && totalPoints != previousTotalPoints) {
                rank = i + 1;
                previousTotalPoints = totalPoints;
            }

            Long quizId = ((Number) row[0]).longValue();
            Long teamId = ((Number) row[2]).longValue();
            LocalDate quizDate = (LocalDate) row[1];

            TopResultLeaderboardEntry dto = new TopResultLeaderboardEntry();
            dto.setRank(rank);
            dto.setTeamId(teamId);
            dto.setTeamName((String) row[3]);
            dto.setQuizId(quizId);
            dto.setQuizDate(quizDate.toString());
            dto.setQuizTitle(deriveQuizTitle(quizDate));
            dto.setTotalPoints(totalPoints);
            dto.setQuizRank(quizRanksByQuizAndTeam
                    .getOrDefault(quizId, Map.of())
                    .getOrDefault(teamId, 1));
            leaderboard.add(dto);
        }

        return leaderboard;
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
            entry.setQuizTitle(deriveQuizTitle(r.getQuiz().getPubDate()));
            entry.setTotalPoints(r.calculateTotalPoints());
            entry.setAnswers(mapAnswerScores(r));

            List<Object[]> scores = new ArrayList<>(quizScores.getOrDefault(r.getQuiz().getQuizId(), List.of()));
            if (scores.isEmpty()) {
                entry.setQuizRank(1);
                entry.setParticipantCount(1);
                return entry;
            }

            scores.sort(ResultService::compareScoreRowsDesc);

            entry.setParticipantCount(scores.size());
            int rank = 1;
            for (int i = 0; i < scores.size(); i++) {
                if (i > 0) {
                    Object[] previous = scores.get(i - 1);
                    Object[] current = scores.get(i);
                    boolean sameRank = hasSameScore(previous, current);
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

    private static final String[] GERMAN_MONTHS = {
        "Jänner", "Februar", "März", "April", "Mai", "Juni",
        "Juli", "August", "September", "Oktober", "November", "Dezember"
    };

    private String deriveQuizTitle(java.time.LocalDate pubDate) {
        return pubDate.getYear() + " " + GERMAN_MONTHS[pubDate.getMonthValue() - 1];
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

    private record AverageTeamStats(Long teamId, String teamName, int quizCount, double averagePoints) {
    }

    private record QuizTeamScore(Long teamId, String teamName, int totalPoints, int fiveCount, int threeCount) {
    }

    private boolean sameQuizRankMetrics(QuizTeamScore left, QuizTeamScore right) {
        return left.totalPoints() == right.totalPoints()
                && left.fiveCount() == right.fiveCount()
                && left.threeCount() == right.threeCount();
    }

    private static long scoreValue(Object[] row, int index) {
        return ((Number) row[index]).longValue();
    }

    private static int compareScoresDesc(
            long leftTotal,
            long leftFives,
            long leftThrees,
            long rightTotal,
            long rightFives,
            long rightThrees
    ) {
        int totalCmp = Long.compare(rightTotal, leftTotal);
        if (totalCmp != 0) {
            return totalCmp;
        }

        int fiveCmp = Long.compare(rightFives, leftFives);
        if (fiveCmp != 0) {
            return fiveCmp;
        }

        return Long.compare(rightThrees, leftThrees);
    }

    private static boolean hasSameScore(Object[] left, Object[] right) {
        return scoreValue(left, 3) == scoreValue(right, 3)
                && scoreValue(left, 4) == scoreValue(right, 4)
                && scoreValue(left, 5) == scoreValue(right, 5);
    }

    private static int compareScoreRowsDesc(Object[] left, Object[] right) {
        return compareScoresDesc(
                scoreValue(left, 3),
                scoreValue(left, 4),
                scoreValue(left, 5),
                scoreValue(right, 3),
                scoreValue(right, 4),
                scoreValue(right, 5)
        );
    }

    private static int compareTopResultRowsByQuizTieBreakDesc(Object[] left, Object[] right) {
        return compareScoresDesc(
                scoreValue(left, 4),
                scoreValue(left, 5),
                scoreValue(left, 6),
                scoreValue(right, 4),
                scoreValue(right, 5),
                scoreValue(right, 6)
        );
    }

    private static boolean hasSameTopResultQuizScore(Object[] left, Object[] right) {
        return scoreValue(left, 4) == scoreValue(right, 4)
                && scoreValue(left, 5) == scoreValue(right, 5)
                && scoreValue(left, 6) == scoreValue(right, 6);
    }

    private static int compareTopResultRowsForGlobalList(Object[] left, Object[] right) {
        int pointsCmp = Long.compare(scoreValue(right, 4), scoreValue(left, 4));
        if (pointsCmp != 0) {
            return pointsCmp;
        }

        LocalDate leftDate = (LocalDate) left[1];
        LocalDate rightDate = (LocalDate) right[1];
        int dateCmp = rightDate.compareTo(leftDate);
        if (dateCmp != 0) {
            return dateCmp;
        }

        String leftTeamName = (String) left[3];
        String rightTeamName = (String) right[3];
        int teamNameCmp = leftTeamName.compareTo(rightTeamName);
        if (teamNameCmp != 0) {
            return teamNameCmp;
        }

        long leftTeamId = scoreValue(left, 2);
        long rightTeamId = scoreValue(right, 2);
        return Long.compare(leftTeamId, rightTeamId);
    }

    private static class MedalAccumulator {
        private int goldCount;
        private int silverCount;
        private int bronzeCount;
    }
}
