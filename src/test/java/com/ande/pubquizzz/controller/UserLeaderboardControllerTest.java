package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.PointsLeaderboardEntry;
import com.ande.pubquizzz.dto.AverageLeaderboardEntry;
import com.ande.pubquizzz.dto.MedalLeaderboardEntry;
import com.ande.pubquizzz.dto.TopResultLeaderboardEntry;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserLeaderboardController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class UserLeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @Test
    @WithMockUser
    void getPointsLeaderboard_authenticated_returnsLeaderboard() throws Exception {
        PointsLeaderboardEntry entry = new PointsLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setTotalPoints(150);
        entry.setQuizCount(3);

        when(leaderboardService.getPointsLeaderboard(null)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].teamId").value(1))
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].totalPoints").value(150))
                .andExpect(jsonPath("$[0].quizCount").value(3));
    }

    @Test
    @WithMockUser
    void getPointsLeaderboard_whenEmpty_returnsEmptyArray() throws Exception {
        when(leaderboardService.getPointsLeaderboard(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/leaderboard/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser
    void getPointsLeaderboard_withYear_returnsYearSpecificLeaderboard() throws Exception {
        PointsLeaderboardEntry entry = new PointsLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setTotalPoints(80);
        entry.setQuizCount(2);

        when(leaderboardService.getPointsLeaderboard(2025)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/points").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].totalPoints").value(80));

        verify(leaderboardService).getPointsLeaderboard(2025);
    }

    @Test
    void getPointsLeaderboard_unauthenticated_returnsJson401() throws Exception {
        mockMvc.perform(get("/api/leaderboard/points"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getMedalLeaderboard_authenticated_returnsLeaderboard() throws Exception {
        MedalLeaderboardEntry entry = new MedalLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setGoldCount(2);
        entry.setSilverCount(1);
        entry.setBronzeCount(0);

        when(leaderboardService.getMedalLeaderboard(null)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/medals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].teamId").value(1))
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].goldCount").value(2))
                .andExpect(jsonPath("$[0].silverCount").value(1))
                .andExpect(jsonPath("$[0].bronzeCount").value(0));
    }

    @Test
    @WithMockUser
    void getMedalLeaderboard_withYear_returnsYearSpecificLeaderboard() throws Exception {
        MedalLeaderboardEntry entry = new MedalLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setGoldCount(1);
        entry.setSilverCount(0);
        entry.setBronzeCount(0);

        when(leaderboardService.getMedalLeaderboard(2025)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/medals").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].goldCount").value(1));

        verify(leaderboardService).getMedalLeaderboard(2025);
    }

    @Test
    @WithMockUser
    void getAverageLeaderboard_authenticated_returnsLeaderboard() throws Exception {
        AverageLeaderboardEntry entry = new AverageLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setAveragePoints(42.5);
        entry.setQuizCount(4);

        when(leaderboardService.getAverageLeaderboard(null)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].teamId").value(1))
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].averagePoints").value(42.5))
                .andExpect(jsonPath("$[0].quizCount").value(4));
    }

    @Test
    @WithMockUser
    void getAverageLeaderboard_withYear_returnsYearSpecificLeaderboard() throws Exception {
        AverageLeaderboardEntry entry = new AverageLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setAveragePoints(40.0);
        entry.setQuizCount(2);

        when(leaderboardService.getAverageLeaderboard(2025)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/average").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].averagePoints").value(40.0));

        verify(leaderboardService).getAverageLeaderboard(2025);
    }

    @Test
    @WithMockUser
    void getTopResultsLeaderboard_authenticated_returnsLeaderboard() throws Exception {
        TopResultLeaderboardEntry entry = new TopResultLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setQuizId(7L);
        entry.setQuizTitle("2026 Mai");
        entry.setQuizDate("2026-05-01");
        entry.setTotalPoints(50);
        entry.setQuizRank(2);

        when(leaderboardService.getTopResultsLeaderboard(null)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/top-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].teamId").value(1))
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].quizId").value(7))
                .andExpect(jsonPath("$[0].quizTitle").value("2026 Mai"))
                .andExpect(jsonPath("$[0].quizDate").value("2026-05-01"))
                .andExpect(jsonPath("$[0].totalPoints").value(50))
                .andExpect(jsonPath("$[0].quizRank").value(2));
    }

    @Test
    @WithMockUser
    void getTopResultsLeaderboard_withYear_returnsYearSpecificLeaderboard() throws Exception {
        TopResultLeaderboardEntry entry = new TopResultLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setQuizId(7L);
        entry.setQuizTitle("2025 Mai");
        entry.setQuizDate("2025-05-01");
        entry.setTotalPoints(50);
        entry.setQuizRank(1);

        when(leaderboardService.getTopResultsLeaderboard(2025)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/top-results").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].quizDate").value("2025-05-01"));

        verify(leaderboardService).getTopResultsLeaderboard(2025);
    }

    @Test
    @WithMockUser
    void getLeaderboardYears_returnsDescendingYears() throws Exception {
        when(leaderboardService.getLeaderboardYears()).thenReturn(List.of(2026, 2025));

        mockMvc.perform(get("/api/leaderboard/years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(2026))
                .andExpect(jsonPath("$[1]").value(2025));

        verify(leaderboardService).getLeaderboardYears();
    }

    @Test
    void getTopResultsLeaderboard_unauthenticated_returnsJson401() throws Exception {
        mockMvc.perform(get("/api/leaderboard/top-results"))
                .andExpect(status().isUnauthorized());
    }
}
