package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CreateTeamRequest;
import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.dto.UpdateTeamRequest;
import com.ande.pubquizzz.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminTeamController {

    private final TeamService teamService;

    @GetMapping("/teams")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        log.debug("GET /admin/teams");
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Long id) {
        log.debug("GET /admin/team/{}", id);
        return teamService.getTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/team")
    public ResponseEntity<String> createTeam(@RequestBody @Valid CreateTeamRequest request) {
        log.info("POST /admin/team - teamName={}", request.getTeamName());
        teamService.createTeam(request.getTeamName());
        return ResponseEntity.ok("Team created successfully");
    }

    @PutMapping("/team/{id}")
    public ResponseEntity<TeamDTO> renameTeam(@PathVariable Long id, @RequestBody @Valid UpdateTeamRequest request) {
        log.info("PUT /admin/team/{} - teamName={}", id, request.getTeamName());
        return ResponseEntity.ok(teamService.renameTeam(id, request.getTeamName()));
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long id) {
        log.info("DELETE /admin/team/{}", id);
        if (!teamService.deleteTeam(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Team deleted successfully");
    }
}
