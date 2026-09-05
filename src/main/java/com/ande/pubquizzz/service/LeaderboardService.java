package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.dto.AverageLeaderboardEntry;
import com.ande.pubquizzz.dto.MedalLeaderboardEntry;
import com.ande.pubquizzz.dto.PointsLeaderboardEntry;
import com.ande.pubquizzz.dto.TopResultLeaderboardEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ande.pubquizzz.config.CacheConfig.AVERAGE_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.LEADERBOARD_YEARS;
import static com.ande.pubquizzz.config.CacheConfig.MEDAL_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.POINTS_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.TOP_RESULTS_LEADERBOARD;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final ResultRepository resultRepository;

    @Transactional(readOnly = true)
    @Cacheable(POINTS_LEADERBOARD)
    public List<PointsLeaderboardEntry> getPointsLeaderboard() {
        return getPointsLeaderboard(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = POINTS_LEADERBOARD, key = "#year == null ? 'all' : #year")
    public List<PointsLeaderboardEntry> getPointsLeaderboard(Integer year) {
        log.debug("Fetching points leaderboard");
        List<Object[]> rows = new ArrayList<>(resultRepository.findLeaderboardRaw(year));
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
        return getAverageLeaderboard(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = AVERAGE_LEADERBOARD, key = "#year == null ? 'all' : #year")
    public List<AverageLeaderboardEntry> getAverageLeaderboard(Integer year) {
        log.debug("Fetching average leaderboard");
        List<Object[]> rows = resultRepository.findLeaderboardRaw(year);

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
        return getMedalLeaderboard(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = MEDAL_LEADERBOARD, key = "#year == null ? 'all' : #year")
    public List<MedalLeaderboardEntry> getMedalLeaderboard(Integer year) {
        log.debug("Fetching medal leaderboard");
        List<Object[]> rows = resultRepository.findPerQuizTeamScoreBreakdownRaw(year);

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
        return getTopResultsLeaderboard(null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = TOP_RESULTS_LEADERBOARD, key = "#year == null ? 'all' : #year")
    public List<TopResultLeaderboardEntry> getTopResultsLeaderboard(Integer year) {
        log.debug("Fetching top results leaderboard");
        List<Object[]> rows = new ArrayList<>(resultRepository.findTopResultsScoreBreakdownRaw(year));
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Object[]>> rowsByQuizId = rows.stream()
                .collect(Collectors.groupingBy(row -> ((Number) row[0]).longValue()));

        Map<Long, Map<Long, Integer>> quizRanksByQuizAndTeam = new HashMap<>();
        for (Map.Entry<Long, List<Object[]>> entry : rowsByQuizId.entrySet()) {
            List<Object[]> quizRows = new ArrayList<>(entry.getValue());
            quizRows.sort((left, right) -> RankingUtils.compareScoreRowsDesc(left, right, 4, 5, 6));

            Map<Long, Integer> rankByTeamId = new HashMap<>();
            int quizRank = 1;
            for (int i = 0; i < quizRows.size(); i++) {
                if (i > 0 && !RankingUtils.hasSameScore(quizRows.get(i - 1), quizRows.get(i), 4, 5, 6)) {
                    quizRank = i + 1;
                }
                Long teamId = ((Number) quizRows.get(i)[2]).longValue();
                rankByTeamId.putIfAbsent(teamId, quizRank);
            }
            quizRanksByQuizAndTeam.put(entry.getKey(), rankByTeamId);
        }

        rows.sort(LeaderboardService::compareTopResultRowsForGlobalList);

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
            dto.setQuizTitle(QuizTitleFormatter.deriveQuizTitle(quizDate));
            dto.setTotalPoints(totalPoints);
            dto.setQuizRank(quizRanksByQuizAndTeam
                    .getOrDefault(quizId, Map.of())
                    .getOrDefault(teamId, 1));
            leaderboard.add(dto);
        }

        return leaderboard;
    }

    @Transactional(readOnly = true)
    @Cacheable(LEADERBOARD_YEARS)
    public List<Integer> getLeaderboardYears() {
        log.debug("Fetching available leaderboard years");
        return resultRepository.findAvailableLeaderboardYears();
    }

    private record AverageTeamStats(Long teamId, String teamName, int quizCount, double averagePoints) {
    }

    private record QuizTeamScore(Long teamId, String teamName, int totalPoints, int fiveCount, int threeCount) {
    }

    private boolean sameQuizRankMetrics(QuizTeamScore left, QuizTeamScore right) {
        return left.totalPoints() == right.totalPoints()
                && left.fiveCount() == right.fiveCount()
                && left.threeCount() == right.threeCount();
    }

    private static int compareTopResultRowsForGlobalList(Object[] left, Object[] right) {
        int pointsCmp = Long.compare(RankingUtils.scoreValue(right, 4), RankingUtils.scoreValue(left, 4));
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

        long leftTeamId = RankingUtils.scoreValue(left, 2);
        long rightTeamId = RankingUtils.scoreValue(right, 2);
        return Long.compare(leftTeamId, rightTeamId);
    }

    private static class MedalAccumulator {
        private int goldCount;
        private int silverCount;
        private int bronzeCount;
    }
}
