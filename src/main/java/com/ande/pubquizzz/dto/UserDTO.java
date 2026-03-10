package com.ande.pubquizzz.dto;

import com.ande.pubquizzz.database.entities.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String password;
    private Role role;
}
