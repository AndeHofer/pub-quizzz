package com.ande.pubquizzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedalLeaderboardEntry {
    private int rank;
    private Long teamId;
    private String teamName;
    private int goldCount;
    private int silverCount;
    private int bronzeCount;
}
