package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.PointsLeaderboardEntry;
import com.ande.pubquizzz.dto.AverageLeaderboardEntry;
import com.ande.pubquizzz.dto.MedalLeaderboardEntry;
import com.ande.pubquizzz.dto.TopResultLeaderboardEntry;
import com.ande.pubquizzz.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserLeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/leaderboard/points")
    public List<PointsLeaderboardEntry> getPointsLeaderboard(@RequestParam(required = false) Integer year) {
        log.debug("GET /api/leaderboard/points - year={}", year);
        return leaderboardService.getPointsLeaderboard(year);
    }

    @GetMapping("/leaderboard/medals")
    public List<MedalLeaderboardEntry> getMedalLeaderboard(@RequestParam(required = false) Integer year) {
        log.debug("GET /api/leaderboard/medals - year={}", year);
        return leaderboardService.getMedalLeaderboard(year);
    }

    @GetMapping("/leaderboard/average")
    public List<AverageLeaderboardEntry> getAverageLeaderboard(@RequestParam(required = false) Integer year) {
        log.debug("GET /api/leaderboard/average - year={}", year);
        return leaderboardService.getAverageLeaderboard(year);
    }

    @GetMapping("/leaderboard/top-results")
    public List<TopResultLeaderboardEntry> getTopResultsLeaderboard(@RequestParam(required = false) Integer year) {
        log.debug("GET /api/leaderboard/top-results - year={}", year);
        return leaderboardService.getTopResultsLeaderboard(year);
    }

    @GetMapping("/leaderboard/years")
    public List<Integer> getLeaderboardYears() {
        log.debug("GET /api/leaderboard/years");
        return leaderboardService.getLeaderboardYears();
    }
}
