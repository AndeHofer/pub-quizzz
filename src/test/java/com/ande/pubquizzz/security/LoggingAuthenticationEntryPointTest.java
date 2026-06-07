package com.ande.pubquizzz.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingAuthenticationEntryPointTest {

    @Test
    void commence_jsonAccept_returnsJson401() throws Exception {
        LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/quizzes");
        request.addHeader("Accept", "application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Auth required"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("Nicht authentifiziert"));
    }

    @Test
    void commence_apiPath_returnsJson401() throws Exception {
        LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/quizzes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Auth required"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("Nicht authentifiziert"));
    }

    @Test
    void commence_htmlRequest_redirectsToLogin() throws Exception {
        LoggingAuthenticationEntryPoint entryPoint = new LoggingAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/quizzes.html");
        request.addHeader("Accept", "text/html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Auth required"));

        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getRedirectedUrl());
    }
}
