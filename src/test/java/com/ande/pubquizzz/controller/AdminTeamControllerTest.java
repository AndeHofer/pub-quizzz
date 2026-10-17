package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.TeamDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTeamController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
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
        dto.setTeamName("Die Besten");

        when(teamService.createTeam("Die Besten")).thenReturn(dto);

        mockMvc.perform(post("/admin/team")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\":\"Die Besten\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeam_withBlankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/team")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeam_withDuplicateName_returnsBadRequest() throws Exception {
        when(teamService.createTeam(anyString()))
                .thenThrow(new BusinessValidationException("Team name already exists"));

        mockMvc.perform(post("/admin/team")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\":\"Duplikat\"}"))
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void renameTeam_withValidRequest_returnsOk() throws Exception {
        TeamDTO dto = new TeamDTO();
        dto.setTeamsId(1L);
        dto.setTeamName("Neuer Name");
        when(teamService.renameTeam(1L, "Neuer Name")).thenReturn(dto);

        mockMvc.perform(put("/admin/team/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\":\"Neuer Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Neuer Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void renameTeam_withBlankName_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/admin/team/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void renameTeam_notFound_returnsNotFound() throws Exception {
        when(teamService.renameTeam(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Team nicht gefunden: 99"));

        mockMvc.perform(put("/admin/team/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void mergeTeams_withValidRequest_returnsTargetTeam() throws Exception {
        TeamDTO targetTeam = new TeamDTO();
        targetTeam.setTeamsId(2L);
        targetTeam.setTeamName("Quizmeister");

        when(teamService.mergeTeams(1L, 2L)).thenReturn(targetTeam);

        mockMvc.perform(post("/admin/teams/merge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceTeamId": 1,
                                  "targetTeamId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamsId").value(2))
                .andExpect(jsonPath("$.teamName").value("Quizmeister"));

        verify(teamService).mergeTeams(1L, 2L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void mergeTeams_withoutSourceTeamId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/teams/merge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetTeamId": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Die ID des Quellteams darf nicht fehlen"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void mergeTeams_withNonPositiveTargetTeamId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/admin/teams/merge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceTeamId": 1,
                                  "targetTeamId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Die ID des Zielteams muss positiv sein"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void mergeTeams_withSameTeamIds_returnsBadRequest() throws Exception {
        when(teamService.mergeTeams(1L, 1L))
                .thenThrow(new BusinessValidationException(
                        "Quellteam und Zielteam dürfen nicht identisch sein"));

        mockMvc.perform(post("/admin/teams/merge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceTeamId": 1,
                                  "targetTeamId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Quellteam und Zielteam dürfen nicht identisch sein"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void mergeTeams_withConflictingQuizResults_returnsBadRequest() throws Exception {
        when(teamService.mergeTeams(1L, 2L))
                .thenThrow(new BusinessValidationException(
                        "Teams können nicht zusammengelegt werden. "
                                + "Beide Teams haben Ergebnisse für Quiz-IDs: [7, 3]"));

        mockMvc.perform(post("/admin/teams/merge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceTeamId": 1,
                                  "targetTeamId": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Teams können nicht zusammengelegt werden. "
                                + "Beide Teams haben Ergebnisse für Quiz-IDs: [7, 3]"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void mergeTeams_asNonAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(post("/admin/teams/merge")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceTeamId": 1,
                                  "targetTeamId": 2
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}

