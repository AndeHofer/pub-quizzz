package com.ande.pubquizzz.config;

import com.ande.pubquizzz.database.entities.User;
import com.ande.pubquizzz.database.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class DefaultAdminConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.default.username:admin}")
    private String defaultUsername;

    @Value("${admin.default.password:admin123}")
    private String defaultPassword;


    @PostConstruct
    public void createDefaultAdmin() {
        if (userRepository.findByUsername(defaultUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(defaultUsername);
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            userRepository.save(admin);

            log.info("Default admin user created: {}", defaultUsername);
            log.info("⚠️ Please change the default password for security!");
        } else {
            log.info("Admin user already exists.");
        }
    }
}