
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

    @Query("SELECT r FROM Result r JOIN FETCH r.team JOIN FETCH r.quiz ORDER BY " +
            "(r.answer1Points + r.answer2Points + r.answer3Points + r.answer4Points + " +
            "r.answer5Points + r.answer6Points + r.answer7Points + r.answer8Points) DESC")
    List<Result> findAllOrderByTotalPointsDesc();

    @Query("SELECT r FROM Result r JOIN FETCH r.team JOIN FETCH r.quiz WHERE r.quiz.quizId = :quizId ORDER BY " +
            "(r.answer1Points + r.answer2Points + r.answer3Points + r.answer4Points + " +
            "r.answer5Points + r.answer6Points + r.answer7Points + r.answer8Points) DESC")
    List<Result> findByQuizIdOrderByTotalPointsDesc(Long quizId);
}
