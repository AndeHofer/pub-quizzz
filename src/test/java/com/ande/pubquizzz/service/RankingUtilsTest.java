package com.ande.pubquizzz.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RankingUtilsTest {

    @Test
    void compareScoresDesc_ordersByTotalPointsDescending() {
        assertThat(RankingUtils.compareScoresDesc(50, 0, 0, 40, 0, 0)).isLessThan(0);
    }

    @Test
    void compareScoresDesc_tieOnTotal_ordersByFivesDescending() {
        assertThat(RankingUtils.compareScoresDesc(40, 3, 0, 40, 2, 0)).isLessThan(0);
    }

    @Test
    void compareScoresDesc_tieOnTotalAndFives_ordersByThreesDescending() {
        assertThat(RankingUtils.compareScoresDesc(40, 2, 3, 40, 2, 1)).isLessThan(0);
    }

    @Test
    void compareScoresDesc_allEqual_returnsZero() {
        assertThat(RankingUtils.compareScoresDesc(40, 2, 1, 40, 2, 1)).isZero();
    }

    @Test
    void compareScoreRowsDesc_usesConfiguredIndices() {
        Object[] left = {99L, 99L, 99L, 40L, 3L, 1L};
        Object[] right = {1L, 1L, 1L, 40L, 2L, 5L};

        assertThat(RankingUtils.compareScoreRowsDesc(left, right, 3, 4, 5)).isLessThan(0);
    }

    @Test
    void hasSameScore_trueWhenConfiguredIndicesMatch() {
        Object[] left = {1L, 2L, 3L, 40L, 2L, 1L};
        Object[] right = {9L, 9L, 9L, 40L, 2L, 1L};

        assertThat(RankingUtils.hasSameScore(left, right, 3, 4, 5)).isTrue();
    }

    @Test
    void hasSameScore_falseWhenAnyConfiguredIndexDiffers() {
        Object[] left = {1L, 2L, 3L, 40L, 2L, 1L};
        Object[] right = {9L, 9L, 9L, 40L, 2L, 2L};

        assertThat(RankingUtils.hasSameScore(left, right, 3, 4, 5)).isFalse();
    }
}
