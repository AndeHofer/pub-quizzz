package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.database.repositories.UserRepository;
import com.ande.pubquizzz.dto.CreateUserRequest;
import com.ande.pubquizzz.dto.UserDTO;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Transactional
    public void register(CreateUserRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessValidationException("Username already exists: " + request.getUsername());
        }
        AppUser appUser = createAndSaveUser(request.getUsername(), request.getPassword(), request.getRole());
        log.info("User '{}' registered successfully", appUser.getUsername());
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

    @Transactional
    public void ensureUserExists(String username, String rawPassword, Role role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            createAndSaveUser(username, rawPassword, role);
            log.info("Default {} user created: {}", role, username);
        }
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private AppUser createAndSaveUser(String username, String rawPassword, Role role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }
}
