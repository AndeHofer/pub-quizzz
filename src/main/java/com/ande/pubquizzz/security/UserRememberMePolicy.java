package com.ande.pubquizzz.security;

import com.ande.pubquizzz.database.entities.Role;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Determines which authorities may use persistent, cookie-based authentication.
 */
final class UserRememberMePolicy {

    private UserRememberMePolicy() {
    }

    static boolean isEligible(Collection<? extends GrantedAuthority> authorities) {
        boolean user = authorities.stream()
                .anyMatch(authority -> Role.USER.springSecurityAuthority().equals(authority.getAuthority()));
        boolean admin = authorities.stream()
                .anyMatch(authority -> Role.ADMIN.springSecurityAuthority().equals(authority.getAuthority()));
        return user && !admin;
    }
}
