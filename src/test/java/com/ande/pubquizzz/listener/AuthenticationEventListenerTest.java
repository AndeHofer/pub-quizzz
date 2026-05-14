package com.ande.pubquizzz.listener;

import com.ande.pubquizzz.service.UsageEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationEventListenerTest {

    @Mock
    private UsageEventService usageEventService;

    @InjectMocks
    private AuthenticationEventListener authenticationEventListener;

    @Test
    void delegatesSuccessfulAuthenticationToUsageEventService() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", "n/a");
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);

        authenticationEventListener.onAuthenticationSuccess(event);

        verify(usageEventService).trackAuthenticationSuccess("alice");
    }
}
