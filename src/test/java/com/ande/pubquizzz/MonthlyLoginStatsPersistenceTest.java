package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.AppUser;
import com.ande.pubquizzz.database.entities.Role;
import com.ande.pubquizzz.database.entities.UsageEvent;
import com.ande.pubquizzz.database.repositories.UsageEventRepository;
import com.ande.pubquizzz.database.repositories.UsageEventRepository.MonthlyLoginStatRow;
import com.ande.pubquizzz.database.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MonthlyLoginStatsPersistenceTest {

    @Autowired
    private UsageEventRepository usageEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void aggregatesAuthSuccessEventsByMonthAndRoleNewestFirst() {
        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setPassword("x");
        user.setRole(Role.USER);
        userRepository.save(user);

        AppUser admin = new AppUser();
        admin.setUsername("admin1");
        admin.setPassword("x");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        usageEventRepository.save(authSuccess("alice", "2026-05-05T08:00:00Z"));
        usageEventRepository.save(authSuccess("alice", "2026-05-06T08:00:00Z"));
        usageEventRepository.save(authSuccess("admin1", "2026-05-07T08:00:00Z"));
        usageEventRepository.save(authSuccess("alice", "2026-04-02T08:00:00Z"));
        usageEventRepository.save(otherEvent("alice", "2026-05-08T08:00:00Z"));

        List<MonthlyLoginStatRow> stats = usageEventRepository.findMonthlyLoginStatsByRole();

        assertEquals(3, stats.size());

        assertEquals("2026-05", stats.get(0).getMonthKey());
        assertEquals("ADMIN", stats.get(0).getRole());
        assertEquals(1L, stats.get(0).getLoginCount());

        assertEquals("2026-05", stats.get(1).getMonthKey());
        assertEquals("USER", stats.get(1).getRole());
        assertEquals(2L, stats.get(1).getLoginCount());

        assertEquals("2026-04", stats.get(2).getMonthKey());
        assertEquals("USER", stats.get(2).getRole());
        assertEquals(1L, stats.get(2).getLoginCount());
    }

    private UsageEvent authSuccess(String username, String instant) {
        UsageEvent event = new UsageEvent();
        event.setEventType("AUTH_SUCCESS");
        event.setUsername(username);
        event.setOccurredAt(Instant.parse(instant));
        return event;
    }

    private UsageEvent otherEvent(String username, String instant) {
        UsageEvent event = new UsageEvent();
        event.setEventType("AUTH_FAILURE");
        event.setUsername(username);
        event.setOccurredAt(Instant.parse(instant));
        return event;
    }
}
