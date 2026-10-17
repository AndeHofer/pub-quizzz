package com.ande.pubquizzz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MergeTeamsRequest {
    @NotNull(message = "Die ID des Quellteams darf nicht fehlen")
    @Positive(message = "Die ID des Quellteams muss positiv sein")
    private Long sourceTeamId;

    @NotNull(message = "Die ID des Zielteams darf nicht fehlen")
    @Positive(message = "Die ID des Zielteams muss positiv sein")
    private Long targetTeamId;
}
