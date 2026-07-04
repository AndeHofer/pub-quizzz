package com.ande.pubquizzz.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

final class SecurityRequestMatchers {

    private static final String JSON_CONTENT_TYPE = "application/json";

    private SecurityRequestMatchers() {
    }

    static RequestMatcher apiStyleRequestMatcher() {
        return SecurityRequestMatchers::isApiStyleRequest;
    }

    static boolean isApiStyleRequest(HttpServletRequest request) {
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
}
