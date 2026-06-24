package com.ande.pubquizzz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheAdminServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache quizzesCache;

    @Mock
    private Cache leaderboardCache;

    @Test
    void evictAllCaches_clearsEveryCacheAndReturnsSummary() {
        when(cacheManager.getCacheNames()).thenReturn(List.of("quizzes.all", "leaderboard.points"));
        when(cacheManager.getCache("quizzes.all")).thenReturn(quizzesCache);
        when(cacheManager.getCache("leaderboard.points")).thenReturn(leaderboardCache);

        CacheAdminService service = new CacheAdminService(cacheManager);

        Map<String, Object> result = service.evictAllCaches();

        verify(quizzesCache).clear();
        verify(leaderboardCache).clear();
        assertEquals(true, result.get("success"));
        assertEquals(List.of("quizzes.all", "leaderboard.points"), result.get("clearedCaches"));
        assertEquals(2, result.get("clearedCount"));
    }
}
