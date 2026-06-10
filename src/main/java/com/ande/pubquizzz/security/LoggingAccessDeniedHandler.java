package com.ande.pubquizzz.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.security.Principal;

/**
 * Access-denied handler that logs rich request/security context for authorization failures and then returns the
 * appropriate 403 response behavior.
 *
 * <p>Usage in security flow:
 *
 * <ul>
 *   <li>Configured in Spring Security via exception handling as the active {@link AccessDeniedHandler}.
 *   <li>Called when an authenticated request is not authorized for the requested resource, or when CSRF validation
 *       fails.
 *   <li>For invalid CSRF on {@code POST /login}, redirects to {@code /login} to recover cleanly.
 *   <li>For other denied requests, returns HTTP 403 and forwards GET/HEAD to {@code /403.html}.</li>
 * </ul>
 *
 * <p>The log entry includes principal/authentication data, session metadata, client/proxy headers, and masked CSRF
 * token previews for diagnostics and incident correlation.
 */
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
        String sessionId = SecurityLogHelper.resolveSessionId(request);
        String sessionValid = SecurityLogHelper.resolveSessionValid(request);
        String remoteAddr = SecurityLogHelper.safeLogValue(request.getRemoteAddr());
        String userAgent = SecurityLogHelper.safeLogValue(request.getHeader("User-Agent"));
        String forwardedFor = SecurityLogHelper.safeLogValue(request.getHeader("X-Forwarded-For"));
        String forwardedProto = SecurityLogHelper.safeLogValue(request.getHeader("X-Forwarded-Proto"));
        String forwardedHost = SecurityLogHelper.safeLogValue(request.getHeader("X-Forwarded-Host"));
        String rawQueryString = request.getQueryString();
        String queryString = SecurityLogHelper.sanitizeQueryStringForLog(rawQueryString);
        int queryStringLength = SecurityLogHelper.queryStringLength(rawQueryString);

        log.warn(
                "403 Forbidden - method={}\npath={}\nuser={}\nsessionId={}\nsessionValid={}\nremoteAddr={}\nuserAgent={}\nforwardedFor={}\nforwardedProto={}\nforwardedHost={}\nqueryString={}\nqueryStringLength={}\ncsrfHeaderPresent={}\ncsrfCookiePresent={}\ncsrfHeaderPreview={}\ncsrfCookiePreview={}\nexceptionType={}\nmessage={}",
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
                queryString,
                queryStringLength,
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
