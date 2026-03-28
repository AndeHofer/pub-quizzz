package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.database.repositories.TeamRepository;
import com.ande.pubquizzz.dto.AllTimeLeaderboardEntry;
import com.ande.pubquizzz.mapper.ResultMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceLeaderboardTest {

    @Mock ResultRepository resultRepository;
    @Mock QuizRepository quizRepository;
    @Mock TeamRepository teamRepository;
    @Mock ResultMapper resultMapper;

    @InjectMocks ResultService resultService;

    @Test
    void getAllTimeLeaderboard_returnsEntriesRankedByPoints() {
        Object[] row1 = {"Alpha Team", 150L, 3L};
        Object[] row2 = {"Beta Team", 90L, 2L};
        when(resultRepository.findAllTimeLeaderboardRaw()).thenReturn(List.of(row1, row2));

        List<AllTimeLeaderboardEntry> result = resultService.getAllTimeLeaderboard();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(0).getTeamName()).isEqualTo("Alpha Team");
        assertThat(result.get(0).getTotalPoints()).isEqualTo(150);
        assertThat(result.get(0).getQuizCount()).isEqualTo(3);
        assertThat(result.get(1).getRank()).isEqualTo(2);
        assertThat(result.get(1).getTeamName()).isEqualTo("Beta Team");
        assertThat(result.get(1).getTotalPoints()).isEqualTo(90);
        assertThat(result.get(1).getQuizCount()).isEqualTo(2);
    }

    @Test
    void getAllTimeLeaderboard_whenEmpty_returnsEmptyList() {
        when(resultRepository.findAllTimeLeaderboardRaw()).thenReturn(List.of());

        List<AllTimeLeaderboardEntry> result = resultService.getAllTimeLeaderboard();

        assertThat(result).isEmpty();
    }
}
