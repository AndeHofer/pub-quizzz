package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "result", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "quiz_id"})
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long resultsId;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("questionNumber ASC")
    @ToString.Exclude
    private List<ResultAnswer> answers = new ArrayList<>();

    public int calculateTotalPoints() {
        return answers.stream().mapToInt(ResultAnswer::getPoints).sum();
    }
}
