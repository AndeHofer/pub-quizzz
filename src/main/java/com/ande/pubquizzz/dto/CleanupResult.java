package com.ande.pubquizzz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CleanupResult {
    private final int deletedCount;
    private final List<String> deletedFiles;
}
