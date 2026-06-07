package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.PointsLeaderboardEntry;
import com.ande.pubquizzz.dto.AverageLeaderboardEntry;
import com.ande.pubquizzz.dto.MedalLeaderboardEntry;
import com.ande.pubquizzz.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserLeaderboardController {

    private final ResultService resultService;

    @GetMapping("/leaderboard/points")
    public List<PointsLeaderboardEntry> getPointsLeaderboard() {
        log.debug("GET /api/leaderboard/points");
        return resultService.getPointsLeaderboard();
    }

    @GetMapping("/leaderboard/medals")
    public List<MedalLeaderboardEntry> getMedalLeaderboard() {
        log.debug("GET /api/leaderboard/medals");
        return resultService.getMedalLeaderboard();
    }

    @GetMapping("/leaderboard/average")
    public List<AverageLeaderboardEntry> getAverageLeaderboard() {
        log.debug("GET /api/leaderboard/average");
        return resultService.getAverageLeaderboard();
    }
}
