package com.ande.pubquizzz.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
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
        String sessionId = resolveSessionId(request);
        String sessionValid = resolveSessionValid(request);
        String remoteAddr = safeLogValue(request.getRemoteAddr());
        String userAgent = safeLogValue(request.getHeader("User-Agent"));
        String forwardedFor = safeLogValue(request.getHeader("X-Forwarded-For"));
        String forwardedProto = safeLogValue(request.getHeader("X-Forwarded-Proto"));
        String forwardedHost = safeLogValue(request.getHeader("X-Forwarded-Host"));

        log.warn(
                "403 Forbidden - method={}, path={}, user={}, sessionId={}, sessionValid={}, remoteAddr={}, userAgent={}, forwardedFor={}, forwardedProto={}, forwardedHost={}, csrfHeaderPresent={}, csrfCookiePresent={}, csrfHeaderPreview={}, csrfCookiePreview={}, exceptionType={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                username,
                sessionId,
                sessionValid,
                remoteAddr,
                userAgent,
                forwardedFor,
                forwardedProto,
                forwardedHost,
                csrfHeader != null && !csrfHeader.isBlank(),
                csrfCookieValue != null && !csrfCookieValue.isBlank(),
                tokenPreview(csrfHeader),
                tokenPreview(csrfCookieValue),
                accessDeniedException.getClass().getSimpleName(),
                accessDeniedException.getMessage()
        );

        if (isLoginPostWithInvalidCsrf(request, accessDeniedException)) {
            response.sendRedirect("/login");
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        if (isGetOrHeadRequest(request)) {
            request.getRequestDispatcher("/403.html").forward(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }

    private boolean isLoginPostWithInvalidCsrf(HttpServletRequest request, AccessDeniedException exception) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/login".equals(request.getRequestURI())
                && exception instanceof InvalidCsrfTokenException;
    }

    private boolean isGetOrHeadRequest(HttpServletRequest request) {
        String method = request.getMethod();
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
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

    private String resolveSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return safeLogValue(session.getId());
        }
        return safeLogValue(request.getRequestedSessionId());
    }

    private String resolveSessionValid(HttpServletRequest request) {
        if (request.getRequestedSessionId() == null || request.getRequestedSessionId().isBlank()) {
            return "-";
        }
        return String.valueOf(request.isRequestedSessionIdValid());
    }

    private String safeLogValue(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
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
