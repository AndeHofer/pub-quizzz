package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.ResultAnswer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultAnswerMappingTest {

    @Test
    void resultCanHoldEightAnswers() {
        Result result = new Result();
        for (int i = 1; i <= 8; i++) {
            ResultAnswer a = new ResultAnswer();
            a.setQuestionNumber(i);
            a.setPoints(i * 10);
            a.setChanged(false);
            result.getAnswers().add(a);
        }
        assertThat(result.getAnswers()).hasSize(8);
        assertThat(result.getAnswers().get(0).getPoints()).isEqualTo(10);
    }
}
