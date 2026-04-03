package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final BuildProperties buildProperties;

    @GetMapping("/api/is-admin")
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String adminAuthority = Role.ADMIN.springSecurityAuthority();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(adminAuthority));
    }

    @GetMapping("/api/version")
    public Map<String, String> version() {
        return Map.of("version", buildProperties.getVersion());
    }
}
