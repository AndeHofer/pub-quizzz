package com.ande.pubquizzz.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;

@Slf4j
public class NoiseFilter implements Filter {

    private static final Set<String> SILENT_PAGES = Set.of(
            "/apple-touch-icon.png",
            "/apple-touch-icon-precomposed.png",
            "/browserconfig.xml",
            "/ads.txt",
            "/app-ads.txt",
            "/.well-known/appspecific/com.chrome.devtools.json"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String lowerUri = httpRequest.getRequestURI().toLowerCase();
        if (SILENT_PAGES.contains(lowerUri)) {
            httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
            log.debug("NoiseFilter: Ignoring request for '{}'", httpRequest.getRequestURI());
            return;
        }

        if (lowerUri.startsWith("/.")
                || lowerUri.contains("/.")
                || lowerUri.contains(".php")
                || lowerUri.contains("/phpmyadmin/")
                || lowerUri.contains("/manager/html")
                || lowerUri.contains("/actuator/")) {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND); // 404
            log.debug("NoiseFilter: Blocking suspicious request for '{}'", httpRequest.getRequestURI());
            return;
        }

        chain.doFilter(request, response);
    }
}