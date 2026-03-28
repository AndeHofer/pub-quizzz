package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.mapper.TeamMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceDeleteTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;

    @Test
    void deleteTeam_deletesResultsBeforeTeam() {
        when(teamRepository.existsById(1L)).thenReturn(true);

        boolean result = teamService.deleteTeam(1L);

        assertTrue(result);
        InOrder order = inOrder(resultRepository, teamRepository);
        order.verify(resultRepository).deleteByTeamTeamsId(1L);
        order.verify(teamRepository).deleteById(1L);
    }

    @Test
    void deleteTeam_notFound_returnsFalse() {
        when(teamRepository.existsById(99L)).thenReturn(false);

        boolean result = teamService.deleteTeam(99L);

        assertFalse(result);
        verifyNoInteractions(resultRepository);
        verify(teamRepository, never()).deleteById(any());
    }
}
