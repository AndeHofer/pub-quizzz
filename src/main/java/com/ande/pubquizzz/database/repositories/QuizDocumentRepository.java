package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.QuizDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizDocumentRepository extends JpaRepository<QuizDocument, Long> {

    List<QuizDocument> findByQuiz_QuizId(Long quizId);

    void deleteByQuiz_QuizId(Long quizId);
}
