package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.database.repositories.UserRepository;
import com.ande.pubquizzz.dto.CreateUserRequest;
import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.mapper.UserMapper;
import com.ande.pubquizzz.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationValidationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void rejectsBlankUsername() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("  ");
        request.setPassword("secret");
        request.setRole(Role.USER);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void rejectsNullPassword() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setPassword(null);
        request.setRole(Role.USER);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
    }

    @Test
    void rejectsNullRole() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setPassword("secret");
        request.setRole(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role");
    }

    @Test
    void rejectsDuplicateUsername() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new com.ande.pubquizzz.database.entities.AppUser()));

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setPassword("secret");
        request.setRole(Role.USER);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already exists");
    }
}
