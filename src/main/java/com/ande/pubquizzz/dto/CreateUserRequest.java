package com.ande.pubquizzz.dto;

import com.ande.pubquizzz.database.entities.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Username darf nicht leer sein")
    private String username;

    @NotBlank(message = "Passwort darf nicht leer sein")
    private String password;

    @NotNull(message = "Rolle muss angegeben werden")
    private Role role;
}
