package com.ande.pubquizzz.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class QuizTitleFormatterTest {

    @Test
    void deriveQuizTitle_formatsGermanMonthAndYear() {
        assertThat(QuizTitleFormatter.deriveQuizTitle(LocalDate.of(2026, 3, 15))).isEqualTo("2026 März");
    }

    @Test
    void deriveQuizTitle_handlesJanuaryWithSpecialGermanSpelling() {
        assertThat(QuizTitleFormatter.deriveQuizTitle(LocalDate.of(2026, 1, 10))).isEqualTo("2026 Jänner");
    }

    @Test
    void deriveQuizTitle_handlesDecember() {
        assertThat(QuizTitleFormatter.deriveQuizTitle(LocalDate.of(2025, 12, 1))).isEqualTo("2025 Dezember");
    }
}
