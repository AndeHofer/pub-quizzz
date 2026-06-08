package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.dto.BootstrapResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final BuildProperties buildProperties;

    private boolean hasAdminRole(Authentication authentication) {
        boolean returnValue = false;
        if (authentication != null && authentication.isAuthenticated()) {
            String adminAuthority = Role.ADMIN.springSecurityAuthority();
            returnValue = authentication.getAuthorities().stream()
                    .anyMatch(authority -> Objects.equals(authority.getAuthority(), adminAuthority));
        }
        return returnValue;
    }

    @GetMapping("/api/bootstrap")
    public ResponseEntity<BootstrapResponse> bootstrap(Authentication authentication) {
        boolean admin = hasAdminRole(authentication);
        String version = buildProperties.getVersion();
        log.debug("bootstrap returnValue=isAdmin={}, version={}", admin, version);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new BootstrapResponse(admin, version));
    }
}
