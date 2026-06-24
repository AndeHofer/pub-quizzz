package com.ande.pubquizzz.service;

import com.ande.pubquizzz.cache.InvalidateAllAppCaches;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CacheAnnotationsTest {

    @Test
    void quizReadMethodsAreCacheable() throws NoSuchMethodException {
        assertHasCacheable(QuizService.class.getMethod("getAllQuizzes"));
        assertHasCacheable(QuizService.class.getMethod("getQuizById", Long.class));
        assertHasCacheable(QuizService.class.getMethod("getQuizDetailById", Long.class));
    }

    @Test
    void quizWriteMethodsEvictCaches() throws NoSuchMethodException {
        assertHasGlobalInvalidate(QuizService.class.getMethod("createQuiz", com.ande.pubquizzz.dto.CreateQuizRequest.class));
        assertHasGlobalInvalidate(QuizService.class.getMethod("updateQuiz", Long.class, java.time.LocalDate.class, java.time.LocalDate.class));
        assertHasGlobalInvalidate(QuizService.class.getMethod("updateQuizFull", Long.class, com.ande.pubquizzz.dto.CreateQuizRequest.class));
        assertHasGlobalInvalidate(QuizService.class.getMethod("deleteQuiz", Long.class));
    }

    @Test
    void resultReadMethodsAreCacheable() throws NoSuchMethodException {
        assertHasCacheable(ResultService.class.getMethod("getQuizSummaries"));
        assertHasCacheable(ResultService.class.getMethod("getResultsForQuiz", Long.class));
        assertHasCacheable(ResultService.class.getMethod("getResults", Long.class));
        assertHasCacheable(ResultService.class.getMethod("getPointsLeaderboard"));
        assertHasCacheable(ResultService.class.getMethod("getAverageLeaderboard"));
        assertHasCacheable(ResultService.class.getMethod("getMedalLeaderboard"));
        assertHasCacheable(ResultService.class.getMethod("getResultsForTeam", Long.class));
    }

    @Test
    void resultWriteMethodsEvictCaches() throws NoSuchMethodException {
        assertHasGlobalInvalidate(ResultService.class.getMethod("createResult", com.ande.pubquizzz.dto.CreateResultRequest.class));
        assertHasGlobalInvalidate(ResultService.class.getMethod("deleteResult", Long.class));
        assertHasGlobalInvalidate(ResultService.class.getMethod("updateResult", Long.class, com.ande.pubquizzz.dto.UpdateResultRequest.class));
    }

    @Test
    void teamReadMethodsAreCacheable() throws NoSuchMethodException {
        assertHasCacheable(TeamService.class.getMethod("getAllTeams"));
        assertHasCacheable(TeamService.class.getMethod("getTeamById", Long.class));
    }

    @Test
    void teamWriteMethodsEvictCaches() throws NoSuchMethodException {
        assertHasGlobalInvalidate(TeamService.class.getMethod("createTeam", String.class));
        assertHasGlobalInvalidate(TeamService.class.getMethod("renameTeam", Long.class, String.class));
        assertHasGlobalInvalidate(TeamService.class.getMethod("deleteTeam", Long.class));
    }

    @Test
    void newsReadMethodsAreCacheable() throws NoSuchMethodException {
        assertHasCacheable(NewsService.class.getMethod("getLatestNews", int.class));
        assertHasCacheable(NewsService.class.getMethod("getAllNewsForAdmin"));
    }

    @Test
    void newsWriteMethodsEvictCaches() throws NoSuchMethodException {
        assertHasGlobalInvalidate(NewsService.class.getMethod("createNews", com.ande.pubquizzz.dto.CreateNewsRequest.class));
        assertHasGlobalInvalidate(NewsService.class.getMethod("updateNews", Long.class, com.ande.pubquizzz.dto.UpdateNewsRequest.class));
        assertHasGlobalInvalidate(NewsService.class.getMethod("deleteNews", Long.class));
    }

    private static void assertHasCacheable(Method method) {
        assertNotNull(method.getAnnotation(Cacheable.class));
    }

    private static void assertHasGlobalInvalidate(Method method) {
        assertNotNull(method.getAnnotation(InvalidateAllAppCaches.class),
                "Expected @InvalidateAllAppCaches on " + method.getName());
    }
}
