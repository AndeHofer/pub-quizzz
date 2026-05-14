package com.ande.pubquizzz;

import com.ande.pubquizzz.database.entities.UsageEvent;
import com.ande.pubquizzz.database.repositories.UsageEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthenticationUsageEventPersistenceTest {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private UsageEventRepository usageEventRepository;

    @Test
    void authenticationSuccessEventPersistsUsageRow() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", "n/a");
        applicationEventPublisher.publishEvent(new AuthenticationSuccessEvent(authentication));

        List<UsageEvent> events = usageEventRepository.findAll();
        assertTrue(events.stream().anyMatch(event ->
                "AUTH_SUCCESS".equals(event.getEventType())
                        && "alice".equals(event.getUsername())
                        && event.getOccurredAt() != null
        ));
    }
}
