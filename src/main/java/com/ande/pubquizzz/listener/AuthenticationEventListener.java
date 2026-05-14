package com.ande.pubquizzz.listener;

import com.ande.pubquizzz.service.UsageEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private final UsageEventService usageEventService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        usageEventService.trackAuthenticationSuccess(username);
        log.info("User {} logged in successfully", username);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String exceptionClass = event.getException().getClass().getSimpleName();
        String message = event.getException().getMessage();
        log.warn("User {} failed to log in: {} - {}", username, exceptionClass, message != null ? message : "no message");
    }
}
