package com.ande.pubquizzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogEntryDTO {
    private String timestamp;
    private String level;
    private String source;
    private String message;
    private String rawLine;
}
