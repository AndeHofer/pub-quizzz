package com.ande.pubquizzz.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.io.IOException;

/**
 * Authentication entry point that logs all unauthorized access attempts with request context and then starts the
 * appropriate unauthenticated response flow.
 *
 * <p>Usage in security flow:
 *
 * <ul>
 *   <li>Configured in Spring Security as the {@link AuthenticationEntryPoint} for protected endpoints.
 *   <li>Called by Spring Security when a request is unauthenticated and authentication is required.
 *   <li>For API-style requests (for example {@code /api/**}, JSON {@code Accept}, or AJAX), returns HTTP 401 with a
 *       JSON body.
 *   <li>For browser-style requests, delegates to {@link LoginUrlAuthenticationEntryPoint} and redirects to
 *       {@code /login}.
 * </ul>
 *
 * <p>In both modes, the class writes structured warning logs with method, path, user, session, client/proxy headers,
 * and exception details to support security monitoring and incident analysis.
 */
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
        String sessionId = resolveSessionId(request);
        String sessionValid = resolveSessionValid(request);
        String remoteAddr = safeLogValue(request.getRemoteAddr());
        String userAgent = safeLogValue(request.getHeader("User-Agent"));
        String forwardedFor = safeLogValue(request.getHeader("X-Forwarded-For"));
        String forwardedProto = safeLogValue(request.getHeader("X-Forwarded-Proto"));
        String forwardedHost = safeLogValue(request.getHeader("X-Forwarded-Host"));

        if (apiStyleRequest) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType(JSON_CONTENT_TYPE);
            response.getWriter().write(UNAUTHENTICATED_ERROR_BODY);

            log.warn("401 Unauthorized - mode=json, method={}\npath={}\nuser={}\nsessionId={}\nsessionValid={}\nremoteAddr={}\nuserAgent={}\nforwardedFor={}\nforwardedProto={}\nforwardedHost={}\nexceptionType={}\nmessage={}",
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
                    authException.getClass().getSimpleName(),
                    authException.getMessage());
            return;
        }

        log.warn("401 Unauthorized - mode=redirect, method={}\npath={}\nuser={}\nsessionId={}\nsessionValid={}\nremoteAddr={}\nuserAgent={}\nforwardedFor={}\nforwardedProto={}\nforwardedHost={}\ntarget=/login\nexceptionType={}\nmessage={}",
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
}
