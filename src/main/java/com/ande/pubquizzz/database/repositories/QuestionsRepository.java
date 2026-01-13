package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {
}
