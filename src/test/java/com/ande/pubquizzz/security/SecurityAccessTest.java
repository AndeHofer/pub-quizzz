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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that Spring Security allows unauthenticated access to static assets
 * (/assets/**, /uploads/**) so Vite-built CSS/JS and hint images load correctly
 * without being redirected to /login.
 * <p>
 * In a @WebMvcTest slice there is no ResourceHttpRequestHandler, so static paths
 * return 404 — that is correct here. The critical assertion is that Security does
 * NOT return a 302 redirect to /login (which would cause the browser to receive
 * HTML instead of CSS/JS and log a MIME type error).
 * <p>
 * We narrow the slice to AdminBackupController to avoid loading all controllers
 * (which have unresolvable bean dependencies in a WebMvcTest context).
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

    // --- Static assets must pass through Security without a redirect ---

    @Test
    void assetsStylesheet_unauthenticated_isNotRedirectedToLogin() throws Exception {
        // Security must NOT redirect to /login (the bug was: 302 → /login)
        // Any non-redirect status (200, 404, 500) is acceptable — the resource handler
        // behaviour in the test slice is irrelevant; what matters is no security redirect.
        var result = mockMvc.perform(get("/assets/styles-YSxDQI3P.css"))
                .andReturn();
        int status = result.getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(302, status,
                "Expected no redirect to /login but got 302");
    }

    @Test
    void uploadedImage_unauthenticated_isNotRedirectedToLogin() throws Exception {
        var result = mockMvc.perform(get("/uploads/some-uuid.jpg"))
                .andReturn();
        int status = result.getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(302, status,
                "Expected no redirect to /login but got 302");
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
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
