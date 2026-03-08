package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/is-admin")
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String adminRoleAuthority = "ROLE_" + Role.ADMIN.name();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(adminRoleAuthority));
    }
}