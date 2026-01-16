package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "appUser")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long appUserId;

    @Column(unique = true)
    @NotNull
    private String username;
    @NotNull
    private String password;
}
