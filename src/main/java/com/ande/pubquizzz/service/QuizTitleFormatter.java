package com.ande.pubquizzz.service;

import java.time.LocalDate;

final class QuizTitleFormatter {

    private static final String[] GERMAN_MONTHS = {
            "Jänner", "Februar", "März", "April", "Mai", "Juni",
            "Juli", "August", "September", "Oktober", "November", "Dezember"
    };

    private QuizTitleFormatter() {
    }

    static String deriveQuizTitle(LocalDate pubDate) {
        return pubDate.getYear() + " " + GERMAN_MONTHS[pubDate.getMonthValue() - 1];
    }
}
