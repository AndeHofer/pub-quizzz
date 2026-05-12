package com.ande.pubquizzz.security;

import com.ande.pubquizzz.controller.AdminBackupController;
import com.ande.pubquizzz.controller.SecurityTestConfig;
import com.ande.pubquizzz.service.BackupService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies tightened security policy for unauthenticated access:
 * only favicon and Spring login are public; static/assets/uploads must redirect
 * to /login until the user is authenticated.
 */
@WebMvcTest(AdminBackupController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class,
        SecurityTestConfig.class})
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackupService backupService;

    // --- Static assets must require authentication ---

    @Test
    void assetsStylesheet_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/assets/styles-YSxDQI3P.css"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void staticPath_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/static/does-not-exist.txt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void uploadedImage_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/uploads/some-uuid.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void favicon_unauthenticated_isNotRedirectedToLogin() throws Exception {
        // favicon.ico is served directly (200 OK) — it must not return 302
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().is2xxSuccessful());
    }

    // --- Protected paths must still require authentication ---

    @Test
    void adminPath_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void apiPath_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/leaderboard/points"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminPath_authenticatedNonAdmin_isForbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
}
