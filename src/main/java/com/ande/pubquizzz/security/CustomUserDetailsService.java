package com.ande.pubquizzz.security;

import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.database.repositories.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * {@link UserDetailsService} implementation that loads application users from {@link UserRepository} for Spring
 * Security authentication.
 *
 * <p>Usage in security flow:
 *
 * <ul>
 *   <li>Detected as a Spring {@code @Service} and injected into the authentication infrastructure.</li>
 *   <li>Invoked during form-login authentication to resolve a username to {@link UserDetails}.</li>
 *   <li>Maps persisted {@link AppUser} data to Spring Security's {@link User} (username, hashed password, role).</li>
 * </ul>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User
                .withUsername(appUser.getUsername())
                .password(appUser.getPassword())
                .roles(appUser.getRole().name())
                .build();
    }
}
