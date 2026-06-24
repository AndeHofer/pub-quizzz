package com.ande.pubquizzz.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CacheConfig {

    public static final String QUIZZES_ALL = "quizzes.all";
    public static final String QUIZ_BY_ID = "quiz.byId";
    public static final String QUIZ_DETAIL_BY_ID = "quiz.detail.byId";
    public static final String TEAMS_ALL = "teams.all";
    public static final String TEAM_BY_ID = "team.byId";
    public static final String QUIZ_SUMMARIES = "results.quizSummaries";
    public static final String RESULTS_FOR_QUIZ = "results.byQuiz";
    public static final String RESULTS_LIST = "results.list";
    public static final String POINTS_LEADERBOARD = "leaderboard.points";
    public static final String AVERAGE_LEADERBOARD = "leaderboard.average";
    public static final String MEDAL_LEADERBOARD = "leaderboard.medal";
    public static final String RESULTS_FOR_TEAM = "results.byTeam";
    public static final String NEWS_LATEST = "news.latest";
    public static final String NEWS_ADMIN_ALL = "news.adminAll";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(500).recordStats());
        manager.setCacheNames(cacheNames());
        return manager;
    }

    public static List<String> cacheNames() {
        return List.of(
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
        );
    }
}
