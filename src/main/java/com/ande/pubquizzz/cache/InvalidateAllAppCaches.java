package com.ande.pubquizzz.cache;

import org.springframework.cache.annotation.CacheEvict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ande.pubquizzz.config.CacheConfig.AVERAGE_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.MEDAL_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.NEWS_ADMIN_ALL;
import static com.ande.pubquizzz.config.CacheConfig.NEWS_LATEST;
import static com.ande.pubquizzz.config.CacheConfig.POINTS_LEADERBOARD;
import static com.ande.pubquizzz.config.CacheConfig.QUIZ_BY_ID;
import static com.ande.pubquizzz.config.CacheConfig.QUIZ_DETAIL_BY_ID;
import static com.ande.pubquizzz.config.CacheConfig.QUIZ_SUMMARIES;
import static com.ande.pubquizzz.config.CacheConfig.QUIZZES_ALL;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_FOR_QUIZ;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_FOR_TEAM;
import static com.ande.pubquizzz.config.CacheConfig.RESULTS_LIST;
import static com.ande.pubquizzz.config.CacheConfig.TEAMS_ALL;
import static com.ande.pubquizzz.config.CacheConfig.TEAM_BY_ID;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@CacheEvict(value = {
        QUIZZES_ALL,
        QUIZ_BY_ID,
        QUIZ_DETAIL_BY_ID,
        TEAMS_ALL,
        TEAM_BY_ID,
        QUIZ_SUMMARIES,
        RESULTS_FOR_QUIZ,
        RESULTS_LIST,
        POINTS_LEADERBOARD,
        AVERAGE_LEADERBOARD,
        MEDAL_LEADERBOARD,
        RESULTS_FOR_TEAM,
        NEWS_LATEST,
        NEWS_ADMIN_ALL
}, allEntries = true)
public @interface InvalidateAllAppCaches {
}
