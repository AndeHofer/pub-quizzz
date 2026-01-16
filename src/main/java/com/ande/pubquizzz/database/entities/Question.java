package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "question")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long questionId;

    @Column(nullable = false)
    @NotNull
    private LocalDate pubDate;
    @Column(nullable = false)
    private LocalDate submitDate;
    @Column(nullable = false)
    @NotNull
    private String question1;
    @Column(nullable = false)
    @NotNull
    private String hint1_1;
    @Column(nullable = false)
    @NotNull
    private String hint1_2;
    @Column(nullable = false)
    @NotNull
    private String hint1_3;
    @Column(nullable = false)
    @NotNull
    private String hint1_4;
    @Column(nullable = false)
    @NotNull
    private String answer1;
    private String note1;

    @Column(nullable = false)
    @NotNull
    private String question2;
    @Column(nullable = false)
    @NotNull
    private String hint2_1;
    @Column(nullable = false)
    @NotNull
    private String hint2_2;
    @Column(nullable = false)
    @NotNull
    private String hint2_3;
    @Column(nullable = false)
    @NotNull
    private String hint2_4;
    @Column(nullable = false)
    @NotNull
    private String answer2;
    private String note2;

    @Column(nullable = false)
    @NotNull
    private String question3;
    @Column(nullable = false)
    @NotNull
    private String hint3_1;
    @Column(nullable = false)
    @NotNull
    private String hint3_2;
    @Column(nullable = false)
    @NotNull
    private String hint3_3;
    @Column(nullable = false)
    @NotNull
    private String hint3_4;
    @Column(nullable = false)
    @NotNull
    private String answer3;
    private String note3;

    @Column(nullable = false)
    @NotNull
    private String question4;
    @Column(nullable = false)
    @NotNull
    private String hint4_1;
    @Column(nullable = false)
    @NotNull
    private String hint4_2;
    @Column(nullable = false)
    @NotNull
    private String hint4_3;
    @Column(nullable = false)
    @NotNull
    private String hint4_4;
    @Column(nullable = false)
    @NotNull
    private String answer4;
    private String note4;

    @Column(nullable = false)
    @NotNull
    private String question5;
    @Column(nullable = false)
    @NotNull
    private String hint5_1;
    @Column(nullable = false)
    @NotNull
    private String hint5_2;
    @Column(nullable = false)
    @NotNull
    private String hint5_3;
    @Column(nullable = false)
    @NotNull
    private String answer5;
    private String note5;

    @Column(nullable = false)
    @NotNull
    private String question6;
    @Column(nullable = false)
    @NotNull
    private String hint6_1;
    @Column(nullable = false)
    @NotNull
    private String hint6_2;
    @Column(nullable = false)
    @NotNull
    private String hint6_3;
    @Column(nullable = false)
    @NotNull
    private String answer6;
    private String note6;

    @Column(nullable = false)
    @NotNull
    private String question7;
    @Column(nullable = false)
    @NotNull
    private String hint7_1;
    @Column(nullable = false)
    @NotNull
    private String hint7_2;
    @Column(nullable = false)
    @NotNull
    private String hint7_3;
    @Column(nullable = false)
    @NotNull
    private String answer7;
    private String note7;

    @Column(nullable = false)
    @NotNull
    private String question8;
    @Column(nullable = false)
    @NotNull
    private String hint8_1;
    @Column(nullable = false)
    @NotNull
    private String hint8_2;
    @Column(nullable = false)
    @NotNull
    private String hint8_3;
    @Column(nullable = false)
    @NotNull
    private String answer8;
    private String note8;
}
