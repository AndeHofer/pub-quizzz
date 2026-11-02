package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Team;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.mapper.TeamMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceParticipatingTeamsTest {
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMapper teamMapper;
    @InjectMocks
    private TeamService teamService;

    @Test
    void mapsOnlyTeamsReturnedByParticipationQuery() {
        Team team = new Team();
        team.setTeamName("Team A");
        TeamDTO dto = new TeamDTO();
        dto.setTeamName("Team A");
        when(teamRepository.findTeamsWithParticipations()).thenReturn(List.of(team));
        when(teamMapper.toDTO(team)).thenReturn(dto);

        assertThat(teamService.getParticipatingTeams()).containsExactly(dto);
        verify(teamRepository).findTeamsWithParticipations();
    }

    @Test
    void returnsEmptyWhenThereAreNoParticipatingTeams() {
        when(teamRepository.findTeamsWithParticipations()).thenReturn(List.of());
        assertThat(teamService.getParticipatingTeams()).isEmpty();
    }
}
