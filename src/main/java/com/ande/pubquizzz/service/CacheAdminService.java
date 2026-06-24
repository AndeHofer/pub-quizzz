package com.ande.pubquizzz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheAdminService {

    private final CacheManager cacheManager;

    public Map<String, Object> evictAllCaches() {
        List<String> clearedCaches = new ArrayList<>();
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                continue;
            }
            cache.clear();
            clearedCaches.add(cacheName);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("clearedCaches", clearedCaches);
        response.put("clearedCount", clearedCaches.size());
        log.debug("Cleared caches.");
        return response;
    }
}
