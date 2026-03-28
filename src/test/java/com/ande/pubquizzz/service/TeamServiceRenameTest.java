package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.TeamMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceRenameTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;

    @Test
    void renameTeam_success() {
        Team team = new Team();
        team.setTeamName("Alt");
        TeamDTO expectedDto = new TeamDTO();
        expectedDto.setTeamName("Neu");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.existsByTeamName("Neu")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamMapper.toDTO(any(Team.class))).thenReturn(expectedDto);

        TeamDTO result = teamService.renameTeam(1L, "Neu");

        assertEquals("Neu", result.getTeamName());
        verify(teamRepository).save(team);
    }

    @Test
    void renameTeam_sameNameAllowed() {
        Team team = new Team();
        team.setTeamName("Gleich");
        TeamDTO expectedDto = new TeamDTO();
        expectedDto.setTeamName("Gleich");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        // existsByTeamName should NOT be called (or returns true but guard skips it)
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamMapper.toDTO(any(Team.class))).thenReturn(expectedDto);

        TeamDTO result = teamService.renameTeam(1L, "Gleich");
        assertEquals("Gleich", result.getTeamName());
    }

    @Test
    void renameTeam_notFound_throwsResourceNotFoundException() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.renameTeam(99L, "X"));
    }

    @Test
    void renameTeam_duplicateName_throwsBusinessValidationException() {
        Team team = new Team();
        team.setTeamName("Alt");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.existsByTeamName("Vergeben")).thenReturn(true);

        assertThrows(BusinessValidationException.class, () -> teamService.renameTeam(1L, "Vergeben"));
    }
}
