package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Quiz, Long> {
}
