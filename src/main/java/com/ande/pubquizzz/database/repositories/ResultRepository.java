package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findByQuiz_QuizId(Long quizId);

    Optional<Result> findByTeam_TeamsIdAndQuiz_QuizId(Long teamId, Long quizId);

    @Query("SELECT r FROM Result r JOIN FETCH r.answers JOIN FETCH r.team JOIN FETCH r.quiz WHERE r.resultsId = :id")
    Optional<Result> findByIdWithAnswers(Long id);

    void deleteByTeamTeamsId(Long teamId);

    void deleteByQuizQuizId(Long quizId);

    @Query("""
            SELECT t.teamsId,
                   t.teamName,
                   COALESCE(SUM(ra.points), 0) AS totalPoints,
                   COUNT(DISTINCT r.quiz.quizId) AS quizCount
            FROM Result r
            JOIN r.team t
            JOIN r.answers ra
            WHERE (:year IS NULL OR FUNCTION('YEAR', r.quiz.pubDate) = :year)
            GROUP BY t.teamsId, t.teamName
            ORDER BY COALESCE(SUM(ra.points), 0) DESC, t.teamName ASC
            """)
    List<Object[]> findLeaderboardRaw(@Param("year") Integer year);

    @Query("""
            SELECT r.quiz.quizId,
                   t.teamsId,
                   t.teamName,
                   COALESCE(SUM(ra.points), 0),
                   SUM(CASE WHEN ra.points = 5 THEN 1 ELSE 0 END),
                   SUM(CASE WHEN ra.points = 3 THEN 1 ELSE 0 END)
            FROM Result r
            JOIN r.team t
            JOIN r.answers ra
            WHERE (:year IS NULL OR FUNCTION('YEAR', r.quiz.pubDate) = :year)
            GROUP BY r.quiz.quizId, t.teamsId, t.teamName
            """)
    List<Object[]> findPerQuizTeamScoreBreakdownRaw(@Param("year") Integer year);

    @Query("""
        SELECT DISTINCT r FROM Result r
        JOIN FETCH r.team t
        JOIN FETCH r.quiz q
        JOIN FETCH r.answers
                WHERE t.teamsId = :teamId
        ORDER BY q.pubDate DESC
    """)
    List<Result> findByTeamIdOrderByPubDateDesc(@Param("teamId") Long teamId);

    @Query("""
        SELECT DISTINCT r FROM Result r
        JOIN FETCH r.team
        JOIN FETCH r.quiz
        JOIN FETCH r.answers
        WHERE r.quiz.quizId = :quizId
    """)
    List<Result> findByQuizIdWithTeamAndAnswers(@Param("quizId") Long quizId);

    @Query("""
                SELECT r.quiz.quizId, r.team.teamsId, r.team.teamName,
                       COALESCE(SUM(a.points), 0),
                       SUM(CASE WHEN a.points = 5 THEN 1 ELSE 0 END),
                       SUM(CASE WHEN a.points = 3 THEN 1 ELSE 0 END)
                FROM Result r
                JOIN r.answers a
                WHERE r.quiz.quizId IN :quizIds
                GROUP BY r.quiz.quizId, r.resultsId, r.team.teamsId, r.team.teamName
            """)
    List<Object[]> findScoresByQuizIds(@Param("quizIds") List<Long> quizIds);

    @Query("""
                SELECT r.quiz.quizId,
                       r.quiz.pubDate,
                       r.team.teamsId,
                       r.team.teamName,
                       COALESCE(SUM(a.points), 0),
                       SUM(CASE WHEN a.points = 5 THEN 1 ELSE 0 END),
                       SUM(CASE WHEN a.points = 3 THEN 1 ELSE 0 END)
                FROM Result r
                JOIN r.answers a
                WHERE (:year IS NULL OR FUNCTION('YEAR', r.quiz.pubDate) = :year)
                GROUP BY r.quiz.quizId, r.quiz.pubDate, r.resultsId, r.team.teamsId, r.team.teamName
            """)
    List<Object[]> findTopResultsScoreBreakdownRaw(@Param("year") Integer year);

    @Query("""
            SELECT DISTINCT FUNCTION('YEAR', r.quiz.pubDate)
            FROM Result r
            WHERE r.quiz.pubDate IS NOT NULL
            ORDER BY FUNCTION('YEAR', r.quiz.pubDate) DESC
            """)
    List<Integer> findAvailableLeaderboardYears();
}
