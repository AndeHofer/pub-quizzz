package com.ande.pubquizzz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTeamRequest {
    @NotBlank(message = "Team-Name darf nicht leer sein")
    private String teamName;
}
