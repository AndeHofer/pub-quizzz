package com.ande.pubquizzz.service;

/**
 * Shared tie-break ranking logic: total points DESC, count of 5-point answers DESC,
 * count of 3-point answers DESC. Row layout (which columns hold total/fives/threes)
 * varies by query, so callers pass explicit column indices.
 */
final class RankingUtils {

    private RankingUtils() {
    }

    static long scoreValue(Object[] row, int index) {
        return ((Number) row[index]).longValue();
    }

    static int compareScoresDesc(
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

    static int compareScoreRowsDesc(Object[] left, Object[] right, int totalIndex, int fivesIndex, int threesIndex) {
        return compareScoresDesc(
                scoreValue(left, totalIndex), scoreValue(left, fivesIndex), scoreValue(left, threesIndex),
                scoreValue(right, totalIndex), scoreValue(right, fivesIndex), scoreValue(right, threesIndex)
        );
    }

    static boolean hasSameScore(Object[] left, Object[] right, int totalIndex, int fivesIndex, int threesIndex) {
        return scoreValue(left, totalIndex) == scoreValue(right, totalIndex)
                && scoreValue(left, fivesIndex) == scoreValue(right, fivesIndex)
                && scoreValue(left, threesIndex) == scoreValue(right, threesIndex);
    }
}
