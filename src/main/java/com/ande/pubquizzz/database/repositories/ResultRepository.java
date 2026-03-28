package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findByQuiz_QuizId(Long quizId);

    Optional<Result> findByTeam_TeamsIdAndQuiz_QuizId(Long teamId, Long quizId);

    @Query("SELECT r FROM Result r JOIN FETCH r.answers JOIN FETCH r.team JOIN FETCH r.quiz WHERE r.resultsId = :id")
    Optional<Result> findByIdWithAnswers(Long id);

    @Query("""
            SELECT t.teamName,
                   COALESCE(SUM(ra.points), 0),
                   COUNT(DISTINCT r.quiz.quizId)
            FROM Result r
            JOIN r.team t
            JOIN r.answers ra
            GROUP BY t.teamsId, t.teamName
            ORDER BY COALESCE(SUM(ra.points), 0) DESC
            """)
    List<Object[]> findAllTimeLeaderboardRaw();
}
