package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findAllByOrderByCreatedAtDescNewsIdDesc(Pageable pageable);

    List<News> findAllByShowOnHomePageTrueOrderByCreatedAtDescNewsIdDesc(Pageable pageable);

    List<News> findAllByOrderByCreatedAtDescNewsIdDesc();
}
