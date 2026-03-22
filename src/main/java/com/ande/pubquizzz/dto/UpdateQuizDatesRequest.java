package com.ande.pubquizzz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateQuizDatesRequest {
    @NotNull(message = "Veröffentlichungsdatum muss angegeben werden")
    private LocalDate pubDate;

    @NotNull(message = "Abgabedatum muss angegeben werden")
    private LocalDate submitDate;
}
