
package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByTeamName(String teamName);
    boolean existsByTeamName(String teamName);

    @Query("SELECT DISTINCT t FROM Result r JOIN r.team t WHERE r.quiz IS NOT NULL")
    List<Team> findTeamsWithParticipations();
}
