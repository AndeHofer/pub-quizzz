package com.ande.pubquizzz.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import java.time.Duration;

/**
 * Signed remember-me cookies are available only to non-admin users.
 */
final class UserRememberMeServices extends TokenBasedRememberMeServices {

    static final int VALIDITY_SECONDS = (int) Duration.ofDays(365).toSeconds();

    UserRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
        setAlwaysRemember(true);
        setTokenValiditySeconds(VALIDITY_SECONDS);
        setCookieCustomizer(cookie -> cookie.setAttribute("SameSite", "Lax"));
    }

    @Override
    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
                               Authentication authentication) {
        if (!UserRememberMePolicy.isEligible(authentication.getAuthorities())) {
            cancelCookie(request, response);
            return;
        }
        super.onLoginSuccess(request, response, authentication);
    }

    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
                                                 HttpServletResponse response) {
        UserDetails user = super.processAutoLoginCookie(cookieTokens, request, response);
        if (!UserRememberMePolicy.isEligible(user.getAuthorities())) {
            throw new InvalidCookieException("Remember-me is not available for this account");
        }
        return user;
    }

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = super.autoLogin(request, response);
        if (authentication != null) {
            onLoginSuccess(request, response, authentication);
        }
        return authentication;
    }
}
