package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.AllTimeLeaderboardEntry;
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
    void getLeaderboard_authenticated_returnsLeaderboard() throws Exception {
        AllTimeLeaderboardEntry entry = new AllTimeLeaderboardEntry();
        entry.setRank(1);
        entry.setTeamName("Alpha Team");
        entry.setTotalPoints(150);
        entry.setQuizCount(3);

        when(resultService.getAllTimeLeaderboard()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].teamName").value("Alpha Team"))
                .andExpect(jsonPath("$[0].totalPoints").value(150))
                .andExpect(jsonPath("$[0].quizCount").value(3));
    }

    @Test
    @WithMockUser
    void getLeaderboard_whenEmpty_returnsEmptyArray() throws Exception {
        when(resultService.getAllTimeLeaderboard()).thenReturn(List.of());

        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getLeaderboard_unauthenticated_returns3xxRedirect() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().is3xxRedirection());
    }
}
