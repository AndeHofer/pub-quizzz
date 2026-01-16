package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "team")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private long teamsId;

    @NotNull
    @Column(nullable = false, unique = true)
    private String teamName;
}
