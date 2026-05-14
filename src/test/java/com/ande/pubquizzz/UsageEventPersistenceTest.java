package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.UsageEvent;
import com.ande.pubquizzz.database.repositories.UsageEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UsageEventPersistenceTest {

    @Autowired
    private UsageEventRepository usageEventRepository;

    @Test
    void savesAuthSuccessUsageEventWithUsernameAndTimestamp() {
        UsageEvent usageEvent = new UsageEvent();
        usageEvent.setEventType("AUTH_SUCCESS");
        usageEvent.setUsername("alice");
        usageEvent.setOccurredAt(Instant.now());

        usageEventRepository.save(usageEvent);

        List<UsageEvent> events = usageEventRepository.findAll();
        assertEquals(1, events.size());
        assertEquals("AUTH_SUCCESS", events.getFirst().getEventType());
        assertEquals("alice", events.getFirst().getUsername());
        assertNotNull(events.getFirst().getOccurredAt());
    }
}
