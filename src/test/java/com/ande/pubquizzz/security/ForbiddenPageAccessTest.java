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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBackupController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class,
        SecurityTestConfig.class})
class ForbiddenPageAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackupService backupService;

    @Test
    @WithMockUser(roles = "USER")
    void adminPath_authenticatedNonAdmin_returnsCustom403Page() throws Exception {
        mockMvc.perform(get("/admin/backup/export"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403.html"));

        mockMvc.perform(get("/403.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("bg-gray-50 min-h-screen")))
                .andExpect(content().string(containsString("Zugriff verweigert")))
                .andExpect(content().string(containsString("Zur Startseite")))
                .andExpect(content().string(containsString("Neu Anmelden")))
                .andExpect(content().string(not(containsString("Whitelabel Error Page"))));
    }
}
