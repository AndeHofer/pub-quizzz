package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.database.repositories.UserRepository;
import com.ande.pubquizzz.dto.CreateUserRequest;
import com.ande.pubquizzz.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void register(CreateUserRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        AppUser appUser = new AppUser();
        appUser.setUsername(request.getUsername());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(request.getRole());
        userRepository.save(appUser);
        log.info("User '{}' registered successfully", request.getUsername());
    }

    @Transactional
    public boolean deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        log.info("User {} deleted successfully", id);
        return true;
    }

    /**
     * Ensures the given user exists, creating it if not. Used for seeding default users on startup.
     */
    @Transactional
    public void ensureUserExists(String username, String rawPassword, Role role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            userRepository.save(user);
            log.info("Default {} user created: {}", role, username);
        }
    }

    private UserDTO toDTO(AppUser user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getAppUserId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        return dto;
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
