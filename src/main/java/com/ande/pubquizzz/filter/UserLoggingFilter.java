package com.ande.pubquizzz.filter;

import jakarta.servlet.*;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            MDC.put("user", "[" + auth.getName() + "] ");
        } else {
            MDC.put("user", "");
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // Important: prevent data leaking between threads
        }
    }
}