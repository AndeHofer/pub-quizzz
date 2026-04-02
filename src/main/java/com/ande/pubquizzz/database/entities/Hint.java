package com.ande.pubquizzz.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "question_hints")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Hint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "quiz_id",        referencedColumnName = "quiz_id"),
            @JoinColumn(name = "question_number", referencedColumnName = "question_number")
    })
    private Question question;

    @Column(name = "hint_order")
    private Integer hintOrder;

    @Column(name = "hint_text", nullable = true)
    private String hintText;

    @Column(name = "image_url_at_start")
    private String imageUrlAtStart;

    @Column(name = "image_url_as_hint")
    private String imageUrlAsHint;
}
