package com.ande.pubquizzz.config;

import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserConfig {

    private final UserService userService;

    @Value("${ADMIN_USER}")
    private String adminName;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${DEFAULT_USER}")
    private String defaultUser;

    @Value("${DEFAULT_PASSWORD}")
    private String defaultPassword;

    @PostConstruct
    public void ensureDefaultUsersExist() {
        userService.ensureUserExists(adminName, adminPassword, Role.ADMIN);
        userService.ensureUserExists(defaultUser, defaultPassword, Role.USER);
    }
}
