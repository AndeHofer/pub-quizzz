package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "results")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Results {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long resultsId;

    @ManyToOne
    @JoinColumn(name = "teamId")
    private Teams team;

    @ManyToOne
    @JoinColumn(name = "questionId")
    private Questions question;

    @Column(columnDefinition = "integer default 0")
    private Integer answer1Points;
    private Boolean changedAnswer1;

    @Column(columnDefinition = "integer default 0")
    private Integer answer2Points;
    private Boolean changedAnswer2;

    @Column(columnDefinition = "integer default 0")
    private Integer answer3Points;
    private Boolean changedAnswer3;

    @Column(columnDefinition = "integer default 0")
    private Integer answer4Points;
    private Boolean changedAnswer4;

    @Column(columnDefinition = "integer default 0")
    private Integer answer5Points;
    private Boolean changedAnswer5;

    @Column(columnDefinition = "integer default 0")
    private Integer answer6Points;
    private Boolean changedAnswer6;

    @Column(columnDefinition = "integer default 0")
    private Integer answer7Points;
    private Boolean changedAnswer7;

    @Column(columnDefinition = "integer default 0")
    private Integer answer8Points;
    private Boolean changedAnswer8;
}
