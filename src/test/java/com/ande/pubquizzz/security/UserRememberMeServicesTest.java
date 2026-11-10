package com.ande.pubquizzz.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserRememberMeServicesTest {

    private static final String COOKIE_NAME = "remember-me";

    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final UserRememberMeServices services = new UserRememberMeServices("test-signing-key", userDetailsService);
    private final UserDetails user = user("USER");

    @Test
    void userLoginCreatesPersistentHttpOnlyCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSecure(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        services.loginSuccess(request, response, authentication(user));

        Cookie cookie = response.getCookie(COOKIE_NAME);
        assertNotNull(cookie);
        assertEquals(UserRememberMeServices.VALIDITY_SECONDS, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("Lax", cookie.getAttribute("SameSite"));
    }

    @Test
    void adminLoginClearsAnyExistingUserCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        services.loginSuccess(new MockHttpServletRequest(), response, authentication(user("ADMIN")));

        assertNotNull(response.getCookie(COOKIE_NAME));
        assertEquals(0, response.getCookie(COOKIE_NAME).getMaxAge());
    }

    @Test
    void mixedAdminAndUserLoginDoesNotCreateRememberMeCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        services.loginSuccess(new MockHttpServletRequest(), response, authentication(user("USER", "ADMIN")));

        assertNotNull(response.getCookie(COOKIE_NAME));
        assertEquals(0, response.getCookie(COOKIE_NAME).getMaxAge());
    }

    @Test
    void successfulAutomaticLoginRenewsCookieForOneYear() {
        services.setTokenValiditySeconds(60);
        Cookie initialCookie = loginCookie(user);
        services.setTokenValiditySeconds(UserRememberMeServices.VALIDITY_SECONDS);
        when(userDetailsService.loadUserByUsername("shared")).thenReturn(user);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = services.autoLogin(requestWith(initialCookie), response);

        assertNotNull(authentication);
        assertEquals("shared", authentication.getName());
        assertNotNull(response.getCookie(COOKIE_NAME));
        assertEquals(UserRememberMeServices.VALIDITY_SECONDS, response.getCookie(COOKIE_NAME).getMaxAge());
        assertFalse(initialCookie.getValue().equals(response.getCookie(COOKIE_NAME).getValue()));
    }

    @Test
    void promotedUserCannotAutomaticallyLogInAsAdmin() {
        Cookie cookie = loginCookie(user);
        when(userDetailsService.loadUserByUsername("shared")).thenReturn(user("ADMIN"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertNull(services.autoLogin(requestWith(cookie), response));
        assertNotNull(response.getCookie(COOKIE_NAME));
        assertEquals(0, response.getCookie(COOKIE_NAME).getMaxAge());
    }

    @Test
    void disabledUserIsRejectedWithoutRenewal() {
        Cookie cookie = loginCookie(user);
        UserDetails disabled = User.withUsername("shared").password("encoded-password")
                .roles("USER").disabled(true).build();
        when(userDetailsService.loadUserByUsername("shared")).thenReturn(disabled);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertNull(services.autoLogin(requestWith(cookie), response));
        assertNotNull(response.getCookie(COOKIE_NAME));
        assertEquals(0, response.getCookie(COOKIE_NAME).getMaxAge());
    }

    @Test
    void invalidCookieIsRejectedWithoutRenewal() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertNull(services.autoLogin(requestWith(new Cookie(COOKIE_NAME, "invalid")), response));
        assertNotNull(response.getCookie(COOKIE_NAME));
        assertEquals(0, response.getCookie(COOKIE_NAME).getMaxAge());
    }

    @Test
    void logoutClearsOnlyTheCurrentResponseCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        services.logout(requestWith(loginCookie(user)), response, authentication(user));

        assertNotNull(response.getCookie(COOKIE_NAME));
        assertEquals(0, response.getCookie(COOKIE_NAME).getMaxAge());
    }

    private Cookie loginCookie(UserDetails details) {
        MockHttpServletResponse response = new MockHttpServletResponse();
        services.loginSuccess(new MockHttpServletRequest(), response, authentication(details));
        Cookie cookie = response.getCookie(COOKIE_NAME);
        assertNotNull(cookie);
        return cookie;
    }

    private static UserDetails user(String... roles) {
        return User.withUsername("shared").password("encoded-password").roles(roles).build();
    }

    private static Authentication authentication(UserDetails details) {
        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    }

    private static MockHttpServletRequest requestWith(Cookie cookie) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(cookie);
        return request;
    }
}
