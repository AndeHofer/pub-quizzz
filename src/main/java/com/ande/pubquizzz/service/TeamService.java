package com.ande.pubquizzz.service;

import com.ande.pubquizzz.cache.InvalidateAllAppCaches;
import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.ande.pubquizzz.config.CacheConfig.TEAMS_ALL;
import static com.ande.pubquizzz.config.CacheConfig.TEAM_BY_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ResultRepository resultRepository;
    private final TeamMapper teamMapper;

    @Transactional(readOnly = true)
    @Cacheable(TEAMS_ALL)
    public List<TeamDTO> getAllTeams() {
        log.debug("Fetching all teams");
        return teamRepository.findAll().stream()
                .map(teamMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = TEAM_BY_ID, key = "#id")
    public Optional<TeamDTO> getTeamById(Long id) {
        log.debug("Fetching team with ID: {}", id);
        return teamRepository.findById(id).map(teamMapper::toDTO);
    }

    @Transactional
    @InvalidateAllAppCaches
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
    @InvalidateAllAppCaches
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
    @InvalidateAllAppCaches
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

    /**
     * Merges two teams by moving all results from the source team to the target team.
     * Throws a BusinessValidationException if the source and target teams are the same.
     * Throws a ResourceNotFoundException if either the source or target team is not found.
     * Throws a BusinessValidationException if the teams have conflicting quiz IDs.
     *
     * @param sourceTeamId the ID of the source team
     * @param targetTeamId the ID of the target team
     * @return the DTO of the target team after the merge
     * @author AndeHofer
     * @since 26.10.3
     */
    @Transactional
    @InvalidateAllAppCaches
    public TeamDTO mergeTeams(Long sourceTeamId, Long targetTeamId) {
        // check if possible
        if (sourceTeamId.equals(targetTeamId)) {
            throw new BusinessValidationException("Quellteam und Zielteam dürfen nicht identisch sein");
        }
        Team targetTeam = teamRepository.findById(targetTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("Zielteam nicht gefunden: " + targetTeamId));
        Team sourceTeam = teamRepository.findById(sourceTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("Quellteam nicht gefunden: " + sourceTeamId));
        List<Long> conflictingQuizIds = resultRepository.findConflictingQuizIdsForTeamMerge(sourceTeamId, targetTeamId);
        if (!conflictingQuizIds.isEmpty()) {
            throw new BusinessValidationException(
                    "Teams können nicht zusammengelegt werden. Beide Teams haben Ergebnisse für Quiz-IDs: "
                            + conflictingQuizIds);
        }

        // do the merge
        List<Result> sourceResults = resultRepository.findByTeamIdOrderByPubDateDesc(sourceTeamId);
        sourceResults.forEach(result -> result.setTeam(targetTeam));
        resultRepository.saveAll(sourceResults);
        teamRepository.delete(sourceTeam);
        return teamMapper.toDTO(targetTeam);
    }
}
