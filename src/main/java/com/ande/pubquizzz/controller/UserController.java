package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final BuildProperties buildProperties;

    @GetMapping("/api/is-admin")
    public boolean isAdmin(Authentication authentication) {
        boolean returnValue = false;
        if (authentication != null && authentication.isAuthenticated()) {
            String adminAuthority = Role.ADMIN.springSecurityAuthority();
            returnValue = authentication.getAuthorities().stream()
                    .anyMatch(authority -> Objects.equals(authority.getAuthority(), adminAuthority));
        }
        log.info("isAdmin returnValue={}", returnValue);
        return returnValue;
    }

    @GetMapping("/api/version")
    public String version() {
        log.info("version returnValue={}", buildProperties.getVersion());
        return buildProperties.getVersion();
    }
}
