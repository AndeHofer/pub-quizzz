package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Question {

    @EmbeddedId
    private QuestionId id;

    @ManyToOne
    @MapsId("quizId")
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(nullable = false)
    @NotNull
    private String question;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("hintOrder ASC")
    private List<Hint> hints = new ArrayList<>();

    @Column(nullable = false)
    @NotNull
    private String answer;
    private String note;
}
