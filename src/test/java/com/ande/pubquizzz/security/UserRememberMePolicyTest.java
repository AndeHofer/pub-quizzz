package com.ande.pubquizzz.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRememberMePolicyTest {

    @Test
    void userIsEligible() {
        assertTrue(UserRememberMePolicy.isEligible(List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    void adminIsNotEligible() {
        assertFalse(UserRememberMePolicy.isEligible(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    void adminWithUserRoleIsNotEligible() {
        assertFalse(UserRememberMePolicy.isEligible(List.of(
                new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    void noRolesAreNotEligible() {
        assertFalse(UserRememberMePolicy.isEligible(List.of()));
    }

    @Test
    void unrelatedAuthorityIsNotEligible() {
        assertFalse(UserRememberMePolicy.isEligible(List.of(new SimpleGrantedAuthority("ROLE_OTHER"))));
    }
}
