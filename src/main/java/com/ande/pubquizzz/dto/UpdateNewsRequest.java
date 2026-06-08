package com.ande.pubquizzz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNewsRequest {
    @NotBlank(message = "Titel darf nicht leer sein")
    @Size(max = 200, message = "Titel darf maximal 200 Zeichen haben")
    private String title;

    @NotBlank(message = "Text darf nicht leer sein")
    @Size(max = 5000, message = "Text darf maximal 5000 Zeichen haben")
    private String text;

    @NotNull(message = "Startseiten-Sichtbarkeit muss gesetzt sein")
    private Boolean showOnHomePage;
}
