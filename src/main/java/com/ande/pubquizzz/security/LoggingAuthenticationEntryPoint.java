package com.ande.pubquizzz.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.io.IOException;

@Slf4j
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String UNAUTHENTICATED_ERROR_BODY = "{\"error\":\"Nicht authentifiziert\"}";

    private final LoginUrlAuthenticationEntryPoint loginRedirectEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException authException) throws IOException {
        boolean apiStyleRequest = isApiStyleRequest(request);
        String username = resolveUsername();

        if (apiStyleRequest) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(JSON_CONTENT_TYPE);
            response.getWriter().write(UNAUTHENTICATED_ERROR_BODY);

            log.warn("401 Unauthorized - mode=json, method={}, path={}, user={}, exceptionType={}, message={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    username,
                    authException.getClass().getSimpleName(),
                    authException.getMessage());
            return;
        }

        log.warn("401 Unauthorized - mode=redirect, method={}, path={}, user={}, target=/login, exceptionType={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                username,
                authException.getClass().getSimpleName(),
                authException.getMessage());
        try {
            loginRedirectEntryPoint.commence(request, response, authException);
        } catch (jakarta.servlet.ServletException ex) {
            throw new IOException("Login redirect failed", ex);
        }
    }

    private boolean isApiStyleRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) {
            return true;
        }

        String accept = request.getHeader("Accept");
        if (accept != null && accept.toLowerCase().contains(JSON_CONTENT_TYPE)) {
            return true;
        }

        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
