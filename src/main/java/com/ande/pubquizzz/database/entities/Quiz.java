package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long quizId;

    @Column(nullable = false)
    @NotNull
    private LocalDate pubDate;
    @Column(nullable = false)
    private LocalDate submitDate;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id.questionNumber ASC")
    @ToString.Exclude
    private List<Question> questions = new ArrayList<>();

    // Helper method to add questions
    public void addQuestion(int number, String text, String answer, String note, List<String> hints) {
        // 1. Validate hint count based on question number
        int hintCount = (hints == null) ? 0 : hints.size();

        if (number >= 1 && number <= 4) {
            if (hintCount != 4) {
                throw new IllegalArgumentException("Questions 1-4 must have exactly 4 hints. Provided: " + hintCount);
            }
        } else if (number >= 5 && number <= 8) {
            if (hintCount != 3) {
                throw new IllegalArgumentException("Questions 5-8 must have exactly 3 hints. Provided: " + hintCount);
            }
        } else {
            throw new IllegalArgumentException("Question number must be between 1 and 8.");
        }

        // 2. Map the Entity
        Question q = new Question();
        q.setId(new QuestionId(this.quizId, number));
        q.setQuiz(this);
        q.setQuestion(text);
        q.setAnswer(answer);
        q.setNote(note);
        q.setHints(new ArrayList<>(hints)); // defensive copy

        this.questions.add(q);
    }
}
