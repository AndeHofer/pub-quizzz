package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ResultRepository resultRepository;
    private final TeamMapper teamMapper;

    @Transactional(readOnly = true)
    public List<TeamDTO> getAllTeams() {
        log.info("Fetching all teams");
        return teamRepository.findAll().stream()
                .map(teamMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TeamDTO> getTeamById(Long id) {
        log.info("Fetching team with ID: {}", id);
        return teamRepository.findById(id).map(teamMapper::toDTO);
    }

    @Transactional
    public TeamDTO createTeam(String teamName) {
        log.info("Creating new team: {}", teamName);
        if (teamRepository.existsByTeamName(teamName)) {
            throw new BusinessValidationException("Team name already exists");
        }
        Team team = new Team();
        team.setTeamName(teamName);
        teamRepository.save(team);
        log.info("Team '{}' created successfully", teamName);
        return teamMapper.toDTO(team);
    }

    @Transactional
    public TeamDTO renameTeam(Long id, String newName) {
        log.info("Renaming team with ID: {} to '{}'", id, newName);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team nicht gefunden: " + id));
        if (!team.getTeamName().equals(newName) && teamRepository.existsByTeamName(newName)) {
            throw new BusinessValidationException("Team-Name bereits vergeben");
        }
        team.setTeamName(newName);
        Team saved = teamRepository.save(team);
        log.info("Team {} renamed to '{}' successfully", id, newName);
        return teamMapper.toDTO(saved);
    }

    @Transactional
    public boolean deleteTeam(Long id) {
        log.info("Deleting team with ID: {}", id);
        if (!teamRepository.existsById(id)) {
            return false;
        }
        resultRepository.deleteByTeamTeamsId(id);
        teamRepository.deleteById(id);
        log.info("Team {} deleted successfully", id);
        return true;
    }
}
