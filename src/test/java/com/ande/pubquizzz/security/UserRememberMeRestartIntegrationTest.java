package com.ande.pubquizzz.security;

import com.ande.pubquizzz.PubQuizzzApplication;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

class UserRememberMeRestartIntegrationTest {

    @Test
    void rememberMeCookieSurvivesApplicationContextRestart() throws Exception {
        String database = "remember_restart_" + UUID.randomUUID().toString().replace("-", "");
        String[] args = {
                "--spring.profiles.active=test",
                "--server.port=0",
                "--spring.datasource.url=jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1",
                "--spring.jpa.hibernate.ddl-auto=update",
                "--app.upload.dir=.",
                "--app.backup.restore-dir=target/remember-restart-" + database
        };
        Cookie cookie;

        try (ConfigurableApplicationContext first = start(args)) {
            MvcResult login = mockMvc(first).perform(post("/login").with(csrf())
                            .param("username", "test").param("password", "test123"))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();
            cookie = login.getResponse().getCookie("remember-me");
            assertNotNull(cookie);
        }

        try (ConfigurableApplicationContext restarted = start(args)) {
            mockMvc(restarted).perform(get("/api/bootstrap").cookie(cookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isAdmin").value(false));
        }
    }

    private static ConfigurableApplicationContext start(String[] args) {
        return new SpringApplicationBuilder(PubQuizzzApplication.class).run(args);
    }

    private static MockMvc mockMvc(ConfigurableApplicationContext context) {
        return webAppContextSetup((WebApplicationContext) context).apply(springSecurity()).build();
    }
}
