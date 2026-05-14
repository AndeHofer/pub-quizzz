package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.repositories.UsageEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsageEventServiceTest {

    @Mock
    private UsageEventRepository usageEventRepository;

    @InjectMocks
    private UsageEventService usageEventService;

    @Test
    void tracksAuthSuccessWithUsernameAndCurrentTimestamp() {
        usageEventService.trackAuthenticationSuccess("alice");

        verify(usageEventRepository).save(argThat(event ->
                "AUTH_SUCCESS".equals(event.getEventType())
                        && "alice".equals(event.getUsername())
                        && event.getOccurredAt() != null
        ));
    }
}
