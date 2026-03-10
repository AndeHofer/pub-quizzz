package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "result_answer")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ResultAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    @ToString.Exclude
    private Result result;

    @Column(name = "question_number", nullable = false)
    @NotNull
    @Min(1)
    @Max(8)
    private Integer questionNumber;

    @Column(name = "points", nullable = false)
    private int points = 0;

    @Column(name = "changed")
    private Boolean changed;
}
