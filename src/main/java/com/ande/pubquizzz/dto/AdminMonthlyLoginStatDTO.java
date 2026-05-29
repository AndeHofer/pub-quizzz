package com.ande.pubquizzz.dto;

import com.ande.pubquizzz.database.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMonthlyLoginStatDTO {
    private String month;
    private Role role;
    private long loginCount;
}
