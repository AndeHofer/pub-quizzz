package com.ande.pubquizzz.config;

import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.database.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class UserConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USER}")
    private String adminName;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${DEFAULT_USER}")
    private String defaultUser;

    @Value("${DEFAULT_PASSWORD}")
    private String defaultPassword;


    @PostConstruct
    public void createDefaultUsers() {
        if (userRepository.findByUsername(adminName).isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername(adminName);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("Default admin user created: {}", adminName);
        }
        if (userRepository.findByUsername(defaultUser).isEmpty()) {
            AppUser user = new AppUser();
            user.setUsername(defaultUser);
            user.setPassword(passwordEncoder.encode(defaultPassword));
            user.setRole(Role.USER);
            userRepository.save(user);
            log.info("Default user created: {}", defaultUser);
        }
    }
}