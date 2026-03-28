package com.ande.pubquizzz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateTeamRequest {
    @NotBlank(message = "Team-Name darf nicht leer sein")
    private String teamName;
}
