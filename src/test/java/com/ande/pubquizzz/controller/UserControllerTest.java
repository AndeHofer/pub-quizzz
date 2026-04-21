package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuildProperties buildProperties;

    @Test
    @WithMockUser
    void getVersion_authenticated_returnsVersion() throws Exception {
        when(buildProperties.getVersion()).thenReturn("1.0.1-SNAPSHOT");

        mockMvc.perform(get("/api/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("1.0.1-SNAPSHOT"));
    }

    @Test
    void getVersion_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/version"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void isAdmin_withAdminRole_returnsTrue() throws Exception {
        mockMvc.perform(get("/api/is-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @WithMockUser
    void isAdmin_withUserRole_returnsFalse() throws Exception {
        mockMvc.perform(get("/api/is-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @WithMockUser
    void adminEndpoint_withUserRole_isForbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
}
