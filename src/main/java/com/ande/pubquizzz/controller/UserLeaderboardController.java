package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.AllTimeLeaderboardEntry;
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

    @GetMapping("/leaderboard")
    public List<AllTimeLeaderboardEntry> getLeaderboard() {
        log.info("GET /api/leaderboard");
        return resultService.getAllTimeLeaderboard();
    }
}
