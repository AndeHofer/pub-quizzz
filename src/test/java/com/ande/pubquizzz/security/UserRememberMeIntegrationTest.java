package com.ande.pubquizzz.security;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.database.repositories.UserRepository;
import com.ande.pubquizzz.listener.AuthenticationEventListener;
import com.ande.pubquizzz.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserRememberMeIntegrationTest {

    private static final String USERNAME = "remember-test-user";
    private static final String ADMIN = "remember-test-admin";
    private static final String PASSWORD = "test-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRememberMeServices rememberMeServices;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        userService.ensureUserExists(USERNAME, PASSWORD, Role.USER);
        userService.ensureUserExists(ADMIN, PASSWORD, Role.ADMIN);
    }

    @Test
    void userCanAutomaticallyLogInWithoutExistingSessionAndCookieIsRenewed() throws Exception {
        Cookie rememberMe = rememberMeCookie(login(USERNAME));

        MvcResult result = mockMvc.perform(get("/api/bootstrap").cookie(rememberMe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andReturn();

        assertEquals(UserRememberMeServices.VALIDITY_SECONDS, rememberMeCookie(result).getMaxAge());
        mockMvc.perform(get("/admin/users").cookie(rememberMe))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void successfulAndFailedLoginsDoNotRecreateStatisticsTable() throws Exception {
        login(USERNAME);
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", USERNAME).param("password", "incorrect"))
                .andExpect(status().is3xxRedirection());

        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'APP_USAGE_EVENT'
                """, Integer.class);
        assertEquals(0, tableCount);
    }

    @Test
    void passwordAndAutomaticLoginsProduceTheOriginalSuccessMessage() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationEventListener.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            Cookie cookie = rememberMeCookie(login(USERNAME));
            mockMvc.perform(get("/api/bootstrap").cookie(cookie))
                    .andExpect(status().isOk());

            long loginMessages = appender.list.stream()
                    .filter(event -> event.getFormattedMessage().equals("User " + USERNAME + " logged in successfully"))
                    .count();
            assertEquals(2, loginMessages);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void adminLoginClearsUserCookieAndDoesNotEnableAutomaticAdminLogin() throws Exception {
        Cookie userCookie = rememberMeCookie(login(USERNAME));
        MvcResult adminLogin = mockMvc.perform(post("/login").with(csrf())
                        .cookie(userCookie).param("username", ADMIN).param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie clearedCookie = rememberMeCookie(adminLogin);
        assertEquals(0, clearedCookie.getMaxAge());
        MockHttpSession session = (MockHttpSession) adminLogin.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/api/bootstrap").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true));
        mockMvc.perform(get("/api/bootstrap"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void logoutInOneBrowserDoesNotPreventOtherBrowserFromLoggingIn() throws Exception {
        MvcResult browserA = login(USERNAME);
        Cookie cookieA = rememberMeCookie(browserA);
        Cookie cookieB = rememberMeCookie(login(USERNAME));
        MockHttpSession sessionA = (MockHttpSession) browserA.getRequest().getSession(false);
        assertNotNull(sessionA);

        MvcResult logout = mockMvc.perform(post("/logout").with(csrf()).session(sessionA).cookie(cookieA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        assertEquals(0, rememberMeCookie(logout).getMaxAge());
        mockMvc.perform(get("/api/bootstrap").cookie(cookieB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(false));
    }

    @Test
    void copiedCookieRemainsUsableAfterTheOriginalBrowserLogsOut() throws Exception {
        MvcResult browser = login(USERNAME);
        Cookie copiedCookie = rememberMeCookie(browser);
        MockHttpSession session = (MockHttpSession) browser.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(post("/logout").with(csrf()).session(session).cookie(copiedCookie))
                .andExpect(status().is3xxRedirection());

        // Stateless cookies cannot be revoked remotely; logout only deletes the browser's copy.
        mockMvc.perform(get("/api/bootstrap").cookie(copiedCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(false));
    }

    @Test
    void modifiedCookieSignatureIsRejected() throws Exception {
        Cookie validCookie = rememberMeCookie(login(USERNAME));
        String decoded = new String(Base64.getDecoder().decode(validCookie.getValue()), StandardCharsets.UTF_8);
        char last = decoded.charAt(decoded.length() - 1);
        String tampered = decoded.substring(0, decoded.length() - 1) + (last == '0' ? '1' : '0');
        Cookie modifiedCookie = new Cookie("remember-me", Base64.getEncoder().encodeToString(
                tampered.getBytes(StandardCharsets.UTF_8)));

        MvcResult result = mockMvc.perform(get("/api/bootstrap").cookie(modifiedCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        assertEquals(0, rememberMeCookie(result).getMaxAge());
    }

    @Test
    void expiredSignedCookieIsRejected() throws Exception {
        String passwordHash = userRepository.findByUsername(USERNAME).orElseThrow().getPassword();
        String signedFields = USERNAME + ":1:" + passwordHash + ":" + rememberMeServices.getKey();
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(signedFields.getBytes(StandardCharsets.UTF_8));
        String payload = USERNAME + ":1:SHA256:" + HexFormat.of().formatHex(digest);
        Cookie expiredCookie = new Cookie("remember-me", Base64.getEncoder().encodeToString(
                payload.getBytes(StandardCharsets.UTF_8)));

        MvcResult result = mockMvc.perform(get("/api/bootstrap").cookie(expiredCookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        assertEquals(0, rememberMeCookie(result).getMaxAge());
    }

    @Test
    void promotedUserCannotAutomaticallyLogInAsAdmin() throws Exception {
        Cookie cookie = rememberMeCookie(login(USERNAME));
        AppUser user = userRepository.findByUsername(USERNAME).orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.saveAndFlush(user);

        MvcResult result = mockMvc.perform(get("/api/bootstrap").cookie(cookie))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        assertEquals(0, rememberMeCookie(result).getMaxAge());
    }

    private MvcResult login(String username) throws Exception {
        return mockMvc.perform(post("/login").with(csrf())
                        .param("username", username).param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    private static Cookie rememberMeCookie(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie("remember-me");
        assertNotNull(cookie);
        return cookie;
    }
}
