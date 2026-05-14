package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {
}
