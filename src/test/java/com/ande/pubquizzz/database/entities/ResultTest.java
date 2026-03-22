package com.ande.pubquizzz.database.entities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultTest {

    @Test
    void calculateTotalPoints_sumsAllAnswerPoints() {
        Result result = new Result();
        ResultAnswer a1 = new ResultAnswer(); a1.setPoints(3);
        ResultAnswer a2 = new ResultAnswer(); a2.setPoints(5);
        ResultAnswer a3 = new ResultAnswer(); a3.setPoints(0);
        result.setAnswers(List.of(a1, a2, a3));

        assertEquals(8, result.calculateTotalPoints());
    }

    @Test
    void calculateTotalPoints_emptyAnswers_returnsZero() {
        Result result = new Result();
        result.setAnswers(List.of());
        assertEquals(0, result.calculateTotalPoints());
    }
}
