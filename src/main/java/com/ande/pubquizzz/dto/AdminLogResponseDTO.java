package com.ande.pubquizzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogResponseDTO {
    private List<AdminLogEntryDTO> entries;
    private int appliedLimit;
    private int returnedCount;
}
