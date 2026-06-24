package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.TeamResultEntry;
import com.ande.pubquizzz.service.ResultService;
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

    @GetMapping("/{teamId}/results")
    public List<TeamResultEntry> getTeamResults(@PathVariable Long teamId) {
        log.debug("GET /api/teams/{}/results", teamId);
        return resultService.getResultsForTeam(teamId);
    }
}
