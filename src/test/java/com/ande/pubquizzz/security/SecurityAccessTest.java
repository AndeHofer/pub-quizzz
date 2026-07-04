package com.ande.pubquizzz.security;

import com.ande.pubquizzz.controller.AdminBackupController;
import com.ande.pubquizzz.controller.SecurityTestConfig;
import com.ande.pubquizzz.service.BackupService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

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
    void errorDispatcherRequest_unauthenticated_isNotRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/error")
                        .with(request -> {
                            request.setDispatcherType(DispatcherType.ERROR);
                            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/missing-page");
                            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
                            return request;
                        }))
                .andExpect(status().is4xxClientError())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void apiPath_unauthenticated_returnsJson401() throws Exception {
        mockMvc.perform(get("/api/leaderboard/points"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nicht authentifiziert")));
    }

    @Test
    void apiPath_unauthenticated_withJsonAccept_returnsJson401() throws Exception {
        mockMvc.perform(get("/api/leaderboard/points")
                        .header("Accept", "application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nicht authentifiziert")));
    }

    @Test
    void adminPath_unauthenticated_withJsonAccept_returnsJson401() throws Exception {
        mockMvc.perform(get("/admin/quizzes")
                        .header("Accept", "application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nicht authentifiziert")));
    }

    @Test
    void loginPage_unauthenticated_isNotCacheable() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().dateValue("Expires", 0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void forbiddenPage_containsReloginLogoutForm() throws Exception {
        mockMvc.perform(get("/403.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"reloginForm\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("id=\"reloginCsrfToken\"")));
    }

    @Test
    @WithMockUser
    void loginPage_authenticated_redirectsToIndex() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginPost_withoutCsrf_isForbidden() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginPost_withInvalidCsrf_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf().useInvalidToken())
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    void logoutPost_withCsrf_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    void logoutPost_withoutCsrf_isForbidden() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminPath_authenticatedNonAdmin_isForbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminImportBackup_withoutCsrf_isForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "backup.zip",
                "application/zip",
                "dummy".getBytes()
        );

        mockMvc.perform(multipart("/admin/backup/import").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminImportBackup_withCsrf_isOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "backup.zip",
                "application/zip",
                "dummy".getBytes()
        );

        mockMvc.perform(multipart("/admin/backup/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
