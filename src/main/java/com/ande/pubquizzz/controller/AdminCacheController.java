package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.service.CacheAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminCacheController {

    private final CacheAdminService cacheAdminService;

    @PostMapping("/invalidate")
    public ResponseEntity<Map<String, Object>> invalidateAllCaches() {
        Map<String, Object> response = cacheAdminService.evictAllCaches();
        log.info("Cache invalidated: all caches (trigger=admin, cleared={})", response.get("clearedCount"));
        return ResponseEntity.ok(response);
    }
}
