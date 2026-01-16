package com.ande.pubquizzz.security;

import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.database.repositories.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        // Look for the user in the database
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Convert our User entity into a Spring Security UserDetails object
        return User
                .withUsername(appUser.getUsername())
                .password(appUser.getPassword()) // This is the hashed password from DB
                .roles("USER")
                .build();
    }
}
