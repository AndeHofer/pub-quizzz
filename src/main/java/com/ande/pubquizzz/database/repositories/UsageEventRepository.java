package com.ande.pubquizzz.database.repositories;

import com.ande.pubquizzz.database.entities.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {

    interface MonthlyLoginStatRow {
        String getMonthKey();

        String getRole();

        long getLoginCount();
    }

    @Query(value = "SELECT FORMATDATETIME(ue.occurred_at, 'yyyy-MM') AS month_key, "
            + "au.role AS role, "
            + "COUNT(*) AS loginCount "
            + "FROM app_usage_event ue "
            + "JOIN app_user au ON au.username = ue.username "
            + "WHERE ue.event_type = 'AUTH_SUCCESS' "
            + "GROUP BY FORMATDATETIME(ue.occurred_at, 'yyyy-MM'), au.role "
            + "ORDER BY FORMATDATETIME(ue.occurred_at, 'yyyy-MM') DESC, au.role ASC",
            nativeQuery = true)
    List<MonthlyLoginStatRow> findMonthlyLoginStatsByRole();
}
