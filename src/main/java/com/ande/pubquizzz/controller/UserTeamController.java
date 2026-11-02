package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.TeamResultEntry;
import com.ande.pubquizzz.dto.TeamLeaderboardStatistics;
import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.service.ResultService;
import com.ande.pubquizzz.service.TeamStatisticsService;
import com.ande.pubquizzz.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class UserTeamController {

    private final ResultService resultService;
    private final TeamStatisticsService teamStatisticsService;
    private final TeamService teamService;

    @GetMapping
    public List<TeamDTO> getParticipatingTeams() {
        log.debug("GET /api/teams");
        return teamService.getParticipatingTeams();
    }

    @GetMapping("/{teamId}/results")
    public List<TeamResultEntry> getTeamResults(@PathVariable Long teamId) {
        log.debug("GET /api/teams/{}/results", teamId);
        return resultService.getResultsForTeam(teamId);
    }

    @GetMapping("/{teamId}/statistics")
    public List<TeamLeaderboardStatistics> getTeamStatistics(@PathVariable Long teamId) {
        log.debug("GET /api/teams/{}/statistics", teamId);
        return teamStatisticsService.getStatistics(teamId);
    }
}
