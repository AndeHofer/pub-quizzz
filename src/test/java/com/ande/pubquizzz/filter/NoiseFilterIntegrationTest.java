package com.ande.pubquizzz.filter;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoiseFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void wellKnownDevToolsProbe_unauthenticated_returns204_withoutRedirect() throws Exception {
        mockMvc.perform(get("/.well-known/appspecific/com.chrome.devtools.json"))
                .andExpect(status().isNoContent())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void gitProbe_unauthenticated_returns404_withoutRedirect() throws Exception {
        mockMvc.perform(get("/.git/config"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void protectedPath_unauthenticated_still_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
