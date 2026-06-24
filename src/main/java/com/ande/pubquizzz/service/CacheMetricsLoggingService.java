package com.ande.pubquizzz.service;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class CacheMetricsLoggingService {

    private final CacheManager cacheManager;
    private final boolean enabled;
    private final Map<String, CacheStats> previousStatsByCache = new HashMap<>();

    public CacheMetricsLoggingService(
            CacheManager cacheManager,
            @Value("${app.cache.metrics.enabled:true}") boolean enabled
    ) {
        this.cacheManager = cacheManager;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${app.cache.metrics.log-interval-ms:60000}")
    public void logCacheMetrics() {
        String prettyMetrics = buildPrettyMetricsLog(collectSnapshotsForLogging());
        if (!prettyMetrics.isBlank()) {
            log.info(prettyMetrics);
        }
    }

    String buildPrettyMetricsLog(List<CacheMetricsSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return "";
        }

        List<CacheMetricsSnapshot> sorted = snapshots.stream()
                .sorted(java.util.Comparator.comparing(CacheMetricsSnapshot::cacheName))
                .toList();

        long totalHits = 0;
        long totalMisses = 0;
        long totalDeltaHits = 0;
        long totalDeltaMisses = 0;

        List<String> hitsCells = new ArrayList<>();
        List<String> missesCells = new ArrayList<>();
        List<String> hitRateCells = new ArrayList<>();
        int cacheNameWidth = "CACHE".length();
        int hitsWidth = "HITS (+DELTA)".length();
        int missesWidth = "MISSES (+DELTA)".length();
        int hitRateWidth = "HIT RATE".length();

        for (CacheMetricsSnapshot snapshot : sorted) {
            String hitsCell = formatValueAndDelta(snapshot.hits(), snapshot.deltaHits());
            String missesCell = formatValueAndDelta(snapshot.misses(), snapshot.deltaMisses());
            String hitRateCell = String.format(Locale.ROOT, "%.3f", snapshot.hitRate());

            hitsCells.add(hitsCell);
            missesCells.add(missesCell);
            hitRateCells.add(hitRateCell);

            cacheNameWidth = Math.max(cacheNameWidth, snapshot.cacheName().length());
            hitsWidth = Math.max(hitsWidth, hitsCell.length());
            missesWidth = Math.max(missesWidth, missesCell.length());
            hitRateWidth = Math.max(hitRateWidth, hitRateCell.length());
        }

        for (int i = 0; i < sorted.size(); i++) {
            CacheMetricsSnapshot snapshot = sorted.get(i);
            totalHits += snapshot.hits();
            totalMisses += snapshot.misses();
            totalDeltaHits += snapshot.deltaHits();
            totalDeltaMisses += snapshot.deltaMisses();
        }

        double totalHitRate = (totalHits + totalMisses) == 0
                ? 0.0
                : (double) totalHits / (double) (totalHits + totalMisses);

        String totalHitsCell = formatValueAndDelta(totalHits, totalDeltaHits);
        String totalMissesCell = formatValueAndDelta(totalMisses, totalDeltaMisses);
        String totalHitRateCell = String.format(Locale.ROOT, "%.3f", totalHitRate);

        hitsWidth = Math.max(hitsWidth, totalHitsCell.length());
        missesWidth = Math.max(missesWidth, totalMissesCell.length());
        hitRateWidth = Math.max(hitRateWidth, totalHitRateCell.length());

        StringBuilder sb = new StringBuilder();
        sb.append("Cache metrics summary (caches=").append(sorted.size()).append(")");
        sb.append(System.lineSeparator())
                .append(formatTableRow("CACHE", "HITS (+DELTA)", "MISSES (+DELTA)", "HIT RATE",
                        cacheNameWidth, hitsWidth, missesWidth, hitRateWidth));
        sb.append(System.lineSeparator())
                .append(formatSeparatorRow(cacheNameWidth, hitsWidth, missesWidth, hitRateWidth));

        for (int i = 0; i < sorted.size(); i++) {
            CacheMetricsSnapshot snapshot = sorted.get(i);
            sb.append(System.lineSeparator())
                    .append(formatTableRow(
                            snapshot.cacheName(),
                            hitsCells.get(i),
                            missesCells.get(i),
                            hitRateCells.get(i),
                            cacheNameWidth,
                            hitsWidth,
                            missesWidth,
                            hitRateWidth
                    ));
        }

        sb.append(System.lineSeparator())
                .append(formatSeparatorRow(cacheNameWidth, hitsWidth, missesWidth, hitRateWidth));
        sb.append(System.lineSeparator())
                .append(formatTableRow(
                        "TOTAL",
                        totalHitsCell,
                        totalMissesCell,
                        totalHitRateCell,
                        cacheNameWidth,
                        hitsWidth,
                        missesWidth,
                        hitRateWidth
                ));

        return sb.toString();
    }

    private String formatValueAndDelta(long value, long delta) {
        return value + " (+" + delta + ")";
    }

    private String formatTableRow(
            String cache,
            String hits,
            String misses,
            String hitRate,
            int cacheNameWidth,
            int hitsWidth,
            int missesWidth,
            int hitRateWidth
    ) {
        return String.format(
                Locale.ROOT,
                "| %1$-" + cacheNameWidth + "s | %2$" + hitsWidth + "s | %3$" + missesWidth + "s | %4$" + hitRateWidth + "s |",
                cache,
                hits,
                misses,
                hitRate
        );
    }

    private String formatSeparatorRow(
            int cacheNameWidth,
            int hitsWidth,
            int missesWidth,
            int hitRateWidth
    ) {
        return "|" + "-".repeat(cacheNameWidth + 2)
                + "|" + "-".repeat(hitsWidth + 2)
                + "|" + "-".repeat(missesWidth + 2)
                + "|" + "-".repeat(hitRateWidth + 2)
                + "|";
    }

    List<CacheMetricsSnapshot> collectSnapshotsForLogging() {
        if (!enabled) {
            return List.of();
        }

        List<CacheMetricsSnapshot> snapshots = new ArrayList<>();
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (!(cache instanceof CaffeineCache caffeineCache)) {
                continue;
            }

            CacheStats current = caffeineCache.getNativeCache().stats();
            CacheStats previous = previousStatsByCache.getOrDefault(cacheName, CacheStats.empty());

            CacheMetricsSnapshot snapshot = new CacheMetricsSnapshot(
                    cacheName,
                    current.hitCount(),
                    current.missCount(),
                    current.hitRate(),
                    current.hitCount() - previous.hitCount(),
                    current.missCount() - previous.missCount()
            );
            snapshots.add(snapshot);
            previousStatsByCache.put(cacheName, current);
        }
        return snapshots;
    }

    record CacheMetricsSnapshot(
            String cacheName,
            long hits,
            long misses,
            double hitRate,
            long deltaHits,
            long deltaMisses
    ) {
    }
}
