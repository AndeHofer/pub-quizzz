package com.ande.pubquizzz.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class QuestionId implements Serializable {

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "question_number")
    @Min(1)
    @Max(8)
    private Integer questionNumber;

    public QuestionId() {}

    public QuestionId(Long quizId, Integer questionNumber) {
        this.quizId = quizId;
        this.questionNumber = questionNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionId that)) return false;
        return Objects.equals(quizId, that.quizId) &&
                Objects.equals(questionNumber, that.questionNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizId, questionNumber);
    }
}