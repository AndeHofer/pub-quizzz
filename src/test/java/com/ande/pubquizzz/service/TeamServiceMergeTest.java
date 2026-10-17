package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Result;
import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.TeamMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceMergeTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;

    @Test
    void mergeTeams_movesResultsToTargetAndDeletesSource() {
        Team sourceTeam = new Team();
        sourceTeam.setTeamName("Altes Team");

        Team targetTeam = new Team();
        targetTeam.setTeamName("Neues Team");

        Result firstResult = new Result();
        firstResult.setTeam(sourceTeam);

        Result secondResult = new Result();
        secondResult.setTeam(sourceTeam);

        TeamDTO expectedDto = new TeamDTO();
        expectedDto.setTeamName("Neues Team");

        when(teamRepository.findById(2L)).thenReturn(Optional.of(targetTeam));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sourceTeam));
        when(resultRepository.findConflictingQuizIdsForTeamMerge(1L, 2L))
                .thenReturn(List.of());
        when(resultRepository.findByTeamIdOrderByPubDateDesc(1L))
                .thenReturn(List.of(firstResult, secondResult));
        when(teamMapper.toDTO(targetTeam)).thenReturn(expectedDto);

        TeamDTO result = teamService.mergeTeams(1L, 2L);

        assertSame(targetTeam, firstResult.getTeam());
        assertSame(targetTeam, secondResult.getTeam());
        assertEquals("Neues Team", result.getTeamName());

        verify(resultRepository).saveAll(List.of(firstResult, secondResult));
        verify(teamRepository).delete(sourceTeam);
        verify(teamMapper).toDTO(targetTeam);

        InOrder inOrder = inOrder(resultRepository, teamRepository);
        inOrder.verify(resultRepository).saveAll(List.of(firstResult, secondResult));
        inOrder.verify(teamRepository).delete(sourceTeam);
    }

    @Test
    void mergeTeams_withIdenticalIds_throwsBusinessValidationException() {
        assertThrows(BusinessValidationException.class,
                () -> teamService.mergeTeams(1L, 1L));

        verifyNoInteractions(teamRepository, resultRepository, teamMapper);
    }

    @Test
    void mergeTeams_whenTargetTeamDoesNotExist_throwsResourceNotFoundException() {
        when(teamRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> teamService.mergeTeams(1L, 2L));

        verifyNoInteractions(resultRepository, teamMapper);
    }

    @Test
    void mergeTeams_whenSourceTeamDoesNotExist_throwsResourceNotFoundException() {
        Team targetTeam = new Team();
        targetTeam.setTeamName("Neues Team");

        when(teamRepository.findById(2L)).thenReturn(Optional.of(targetTeam));
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> teamService.mergeTeams(1L, 2L));

        verifyNoInteractions(resultRepository, teamMapper);
    }

    @Test
    void mergeTeams_withConflictingQuizResults_throwsBusinessValidationException() {
        Team sourceTeam = new Team();
        sourceTeam.setTeamName("Altes Team");

        Team targetTeam = new Team();
        targetTeam.setTeamName("Neues Team");

        when(teamRepository.findById(2L)).thenReturn(Optional.of(targetTeam));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sourceTeam));
        when(resultRepository.findConflictingQuizIdsForTeamMerge(1L, 2L))
                .thenReturn(List.of(7L, 3L));

        assertThrows(BusinessValidationException.class,
                () -> teamService.mergeTeams(1L, 2L));

        verify(resultRepository, never()).findByTeamIdOrderByPubDateDesc(1L);
        verify(resultRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        verify(teamRepository, never()).delete(sourceTeam);
        verifyNoInteractions(teamMapper);
    }

    @Test
    void mergeTeams_withSourceTeamWithoutResults_deletesSourceTeam() {
        Team sourceTeam = new Team();
        sourceTeam.setTeamName("Leeres Team");

        Team targetTeam = new Team();
        targetTeam.setTeamName("Bestehendes Team");

        TeamDTO expectedDto = new TeamDTO();
        expectedDto.setTeamName("Bestehendes Team");

        when(teamRepository.findById(2L)).thenReturn(Optional.of(targetTeam));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sourceTeam));
        when(resultRepository.findConflictingQuizIdsForTeamMerge(1L, 2L))
                .thenReturn(List.of());
        when(resultRepository.findByTeamIdOrderByPubDateDesc(1L))
                .thenReturn(List.of());
        when(teamMapper.toDTO(targetTeam)).thenReturn(expectedDto);

        TeamDTO result = teamService.mergeTeams(1L, 2L);

        assertEquals("Bestehendes Team", result.getTeamName());
        verify(resultRepository).saveAll(List.of());
        verify(teamRepository).delete(sourceTeam);
    }


}
