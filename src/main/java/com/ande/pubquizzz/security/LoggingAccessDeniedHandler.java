package com.ande.pubquizzz.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.security.Principal;

@Slf4j
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Principal principal = request.getUserPrincipal();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = resolveUsername(principal, authentication);
        String csrfHeader = request.getHeader("X-XSRF-TOKEN");
        var csrfCookie = WebUtils.getCookie(request, "XSRF-TOKEN");
        String csrfCookieValue = csrfCookie != null ? csrfCookie.getValue() : null;

        log.warn(
                "403 Forbidden - method={}, path={}, user={}, sessionId={}, sessionValid={}, csrfHeaderPresent={}, csrfCookiePresent={}, csrfHeaderPreview={}, csrfCookiePreview={}, exceptionType={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                username,
                request.getRequestedSessionId(),
                request.isRequestedSessionIdValid(),
                csrfHeader != null && !csrfHeader.isBlank(),
                csrfCookieValue != null && !csrfCookieValue.isBlank(),
                tokenPreview(csrfHeader),
                tokenPreview(csrfCookieValue),
                accessDeniedException.getClass().getSimpleName(),
                accessDeniedException.getMessage()
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/403.html").forward(request, response);
    }

    private String tokenPreview(String token) {
        if (token == null || token.isBlank()) {
            return "-";
        }
        int len = token.length();
        if (len <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "..." + token.substring(len - 4);
    }

    private String resolveUsername(Principal principal, Authentication authentication) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        return "anonymous";
    }
}
