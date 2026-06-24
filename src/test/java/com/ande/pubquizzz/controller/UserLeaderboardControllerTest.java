package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.PointsLeaderboardEntry;
import com.ande.pubquizzz.dto.AverageLeaderboardEntry;
import com.ande.pubquizzz.dto.MedalLeaderboardEntry;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.ResultService;
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
    private ResultService resultService;

    @Test
    @WithMockUser
    void getPointsLeaderboard_authenticated_returnsLeaderboard() throws Exception {
        PointsLeaderboardEntry entry = new PointsLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setTotalPoints(150);
        entry.setQuizCount(3);

        when(resultService.getPointsLeaderboard()).thenReturn(List.of(entry));

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
        when(resultService.getPointsLeaderboard()).thenReturn(List.of());

        mockMvc.perform(get("/api/leaderboard/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
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

        when(resultService.getMedalLeaderboard()).thenReturn(List.of(entry));

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
    void getAverageLeaderboard_authenticated_returnsLeaderboard() throws Exception {
        AverageLeaderboardEntry entry = new AverageLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamId(1L);
        entry.setTeamName("Alpha Team");
        entry.setAveragePoints(42.5);
        entry.setQuizCount(4);

        when(resultService.getAverageLeaderboard()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].teamId").value(1))
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].averagePoints").value(42.5))
                .andExpect(jsonPath("$[0].quizCount").value(4));
    }
}
