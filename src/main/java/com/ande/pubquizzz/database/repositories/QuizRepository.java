package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("""
        SELECT q, COUNT(r) FROM Quiz q
        LEFT JOIN Result r ON r.quiz = q
        GROUP BY q
        ORDER BY q.pubDate DESC
    """)
    List<Object[]> findAllWithResultCount();
}
