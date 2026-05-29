package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.dto.AdminLogEntryDTO;
import com.ande.pubquizzz.dto.AdminLogResponseDTO;
import com.ande.pubquizzz.dto.AdminMonthlyLoginStatDTO;
import com.ande.pubquizzz.dto.CreateUserRequest;
import com.ande.pubquizzz.dto.UserDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.UsageEventService;
import com.ande.pubquizzz.service.AdminLogService;
import com.ande.pubquizzz.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UsageEventService usageEventService;

    @MockitoBean
    private AdminLogService adminLogService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLogs_returnsFilteredEntriesForAdmin() throws Exception {
        AdminLogEntryDTO entry = new AdminLogEntryDTO(
                "2026-05-29T08:15:00",
                "ERROR",
                "AdminUserController.getLogs:61",
                "Fehler beim Laden",
                "2026-05-29 08:15:00 ERROR AdminUserController.getLogs:61 - Fehler beim Laden\n"
                        + "java.lang.RuntimeException: boom\n"
                        + "\tat com.ande.pubquizzz.controller.AdminUserController.getLogs(AdminUserController.java:61)"
        );
        AdminLogResponseDTO response = new AdminLogResponseDTO(List.of(entry), 200, 1);

        when(adminLogService.getLogs("fehler", "ERROR", "2026-05-29T08:00:00", "2026-05-29T09:00:00", 200))
                .thenReturn(response);

        mockMvc.perform(get("/admin/logs")
                        .param("q", "fehler")
                        .param("level", "ERROR")
                        .param("from", "2026-05-29T08:00:00")
                        .param("to", "2026-05-29T09:00:00")
                        .param("limit", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedLimit").value(200))
                .andExpect(jsonPath("$.returnedCount").value(1))
                .andExpect(jsonPath("$.entries[0].level").value("ERROR"))
                .andExpect(jsonPath("$.entries[0].message").value("Fehler beim Laden"))
                .andExpect(jsonPath("$.entries[0].rawLine").value(org.hamcrest.Matchers.containsString("RuntimeException: boom")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLogs_withInvalidFilters_returnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Ungueltiger Level-Filter: bad"))
                .when(adminLogService).getLogs("x", "bad", null, null, 10);

        mockMvc.perform(get("/admin/logs")
                        .param("q", "x")
                        .param("level", "bad")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ungueltiger Level-Filter: bad"));
    }

    @Test
    void getLogs_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/admin/logs"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getLogs_nonAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/admin/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMonthlyLoginStatsByRole_returnsAggregatedStats() throws Exception {
        AdminMonthlyLoginStatDTO userStat = new AdminMonthlyLoginStatDTO("2026-05", Role.USER, 12L);
        AdminMonthlyLoginStatDTO adminStat = new AdminMonthlyLoginStatDTO("2026-05", Role.ADMIN, 3L);

        when(usageEventService.getMonthlyLoginStatsByRole()).thenReturn(List.of(userStat, adminStat));

        mockMvc.perform(get("/admin/login-stats/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2026-05"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].loginCount").value(12))
                .andExpect(jsonPath("$[1].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].loginCount").value(3));
    }

    @Test
    void getMonthlyLoginStatsByRole_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/admin/login-stats/monthly"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returnsListOfUsers() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setUserId(1L);
        dto.setUsername("admin");
        dto.setRole(Role.ADMIN);

        when(userService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerUser_withValidRequest_returnsOk() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("secret");
        request.setRole(Role.USER);

        mockMvc.perform(post("/admin/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerUser_withDuplicateUsername_returnsBadRequest() throws Exception {
        doThrow(new BusinessValidationException("Username already exists: dup"))
                .when(userService).register(any(CreateUserRequest.class));

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("dup");
        request.setPassword("pass");
        request.setRole(Role.USER);

        mockMvc.perform(post("/admin/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_whenFound_returnsOk() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/admin/user/1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_whenNotFound_returns404() throws Exception {
        when(userService.deleteUser(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/admin/user/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }
}
