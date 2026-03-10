package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UserController {

    @GetMapping("/api/is-admin")
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String adminAuthority = Role.ADMIN.springSecurityAuthority();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(adminAuthority));
    }
}