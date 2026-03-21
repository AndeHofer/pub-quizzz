package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.TeamService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTeamController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, SecurityTestConfig.class})
class AdminTeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTeams_returnsListOfTeams() throws Exception {
        TeamDTO dto = new TeamDTO();
        dto.setTeamsId(1L);
        dto.setTeamName("Die Besten");

        when(teamService.getAllTeams()).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamName").value("Die Besten"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeamById_whenFound_returnsTeam() throws Exception {
        TeamDTO dto = new TeamDTO();
        dto.setTeamsId(2L);
        dto.setTeamName("Quiz Champions");

        when(teamService.getTeamById(2L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/admin/team/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Quiz Champions"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeamById_whenNotFound_returns404() throws Exception {
        when(teamService.getTeamById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/team/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeam_withValidName_returnsOk() throws Exception {
        TeamDTO dto = new TeamDTO();
        dto.setTeamsId(3L);
        dto.setTeamName("Neues Team");

        when(teamService.createTeam("Neues Team")).thenReturn(dto);

        mockMvc.perform(post("/admin/team")
                        .with(csrf())
                        .param("teamName", "Neues Team"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeam_withDuplicateName_returnsBadRequest() throws Exception {
        when(teamService.createTeam(anyString()))
                .thenThrow(new IllegalArgumentException("Team name already exists"));

        mockMvc.perform(post("/admin/team")
                        .with(csrf())
                        .param("teamName", "Duplikat"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTeam_whenFound_returnsOk() throws Exception {
        when(teamService.deleteTeam(1L)).thenReturn(true);

        mockMvc.perform(delete("/admin/team/1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTeam_whenNotFound_returns404() throws Exception {
        when(teamService.deleteTeam(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/admin/team/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTeams_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/admin/teams"))
                .andExpect(status().is3xxRedirection());
    }
}
