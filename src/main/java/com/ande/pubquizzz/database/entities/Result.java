package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "result")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long resultsId;

    @ManyToOne
    @JoinColumn(name = "teamId")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "quizId")
    private Quiz quiz;


    private Integer answer1Points = 0;
    private Boolean changedAnswer1;


    private Integer answer2Points = 0;
    private Boolean changedAnswer2;


    private Integer answer3Points = 0;
    private Boolean changedAnswer3;


    private Integer answer4Points = 0;
    private Boolean changedAnswer4;


    private Integer answer5Points = 0;
    private Boolean changedAnswer5;


    private Integer answer6Points = 0;
    private Boolean changedAnswer6;


    private Integer answer7Points = 0;
    private Boolean changedAnswer7;

    private Integer answer8Points = 0;
    private Boolean changedAnswer8;
}
