package com.ande.pubquizzz.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheMetricsLoggingServiceTest {

    @Test
    void collectSnapshotsForLogging_returnsCaffeineStatsAndDeltas() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder().recordStats().maximumSize(100));
        cacheManager.setCacheNames(List.of("leaderboard.points"));

        Cache cache = cacheManager.getCache("leaderboard.points");
        cache.get("a", () -> "1");
        cache.get("a", () -> "1");

        CacheMetricsLoggingService service = new CacheMetricsLoggingService(cacheManager, true);
        List<CacheMetricsLoggingService.CacheMetricsSnapshot> first = service.collectSnapshotsForLogging();

        assertEquals(1, first.size());
        assertEquals(1L, first.getFirst().hits());
        assertEquals(1L, first.getFirst().misses());
        assertEquals(1L, first.getFirst().deltaHits());
        assertEquals(1L, first.getFirst().deltaMisses());

        cache.get("a", () -> "1");
        cache.get("b", () -> "2");

        List<CacheMetricsLoggingService.CacheMetricsSnapshot> second = service.collectSnapshotsForLogging();
        assertEquals(2L, second.getFirst().hits());
        assertEquals(2L, second.getFirst().misses());
        assertEquals(1L, second.getFirst().deltaHits());
        assertEquals(1L, second.getFirst().deltaMisses());
    }

    @Test
    void collectSnapshotsForLogging_whenDisabled_returnsEmpty() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder().recordStats().maximumSize(100));
        cacheManager.setCacheNames(List.of("leaderboard.points"));

        CacheMetricsLoggingService service = new CacheMetricsLoggingService(cacheManager, false);
        assertTrue(service.collectSnapshotsForLogging().isEmpty());
    }

    @Test
    void collectSnapshotsForLogging_skipsNonCaffeineCaches() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("plain");

        CacheMetricsLoggingService service = new CacheMetricsLoggingService(cacheManager, true);
        assertTrue(service.collectSnapshotsForLogging().isEmpty());
    }

    @Test
    void buildPrettyMetricsLog_formatsSingleReadableEntryWithTotals() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        CacheMetricsLoggingService service = new CacheMetricsLoggingService(cacheManager, true);

        List<CacheMetricsLoggingService.CacheMetricsSnapshot> snapshots = List.of(
                new CacheMetricsLoggingService.CacheMetricsSnapshot(
                        "leaderboard.points.super.long.cache.name.with.extra.words", 12, 3, 0.8, 2, 1
                ),
                new CacheMetricsLoggingService.CacheMetricsSnapshot(
                        "news.latest", 4, 1, 0.8, 1, 0
                )
        );

        String pretty = service.buildPrettyMetricsLog(snapshots);
        String[] lines = pretty.split(System.lineSeparator());

        assertTrue(pretty.contains("Cache metrics summary"));
        assertTrue(pretty.contains("leaderboard.points.super.long.cache.name.with.extra.words"));
        assertTrue(pretty.contains("news.latest"));
        assertTrue(pretty.contains("| TOTAL"));
        assertFalse(pretty.contains("EVICTIONS"));

        List<String> tableRows = Stream.of(lines)
                .filter(line -> line.startsWith("| "))
                .toList();
        assertFalse(tableRows.isEmpty());

        int firstPipeIndex = tableRows.getFirst().indexOf("|", 2);
        int secondPipeIndex = tableRows.getFirst().indexOf("|", firstPipeIndex + 1);
        int thirdPipeIndex = tableRows.getFirst().indexOf("|", secondPipeIndex + 1);

        for (String row : tableRows) {
            assertEquals(firstPipeIndex, row.indexOf("|", 2));
            assertEquals(secondPipeIndex, row.indexOf("|", firstPipeIndex + 1));
            assertEquals(thirdPipeIndex, row.indexOf("|", secondPipeIndex + 1));
        }

        assertTrue(pretty.contains("16 (+3)"));
        assertTrue(pretty.contains("4 (+1)"));
    }

    @Test
    void buildPrettyMetricsLog_whenNoSnapshots_returnsEmptyString() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        CacheMetricsLoggingService service = new CacheMetricsLoggingService(cacheManager, true);

        String pretty = service.buildPrettyMetricsLog(List.of());

        assertFalse(pretty.contains("Cache metrics summary"));
        assertTrue(pretty.isBlank());
    }
}
