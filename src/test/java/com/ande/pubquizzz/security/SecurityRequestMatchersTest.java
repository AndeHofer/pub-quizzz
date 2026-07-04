package com.ande.pubquizzz.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityRequestMatchersTest {

    @Test
    void isApiStyleRequest_trueForApiPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/leaderboard/points");

        assertTrue(SecurityRequestMatchers.isApiStyleRequest(request));
    }

    @Test
    void isApiStyleRequest_trueForJsonAcceptHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/quizzes");
        request.addHeader("Accept", "application/json");

        assertTrue(SecurityRequestMatchers.isApiStyleRequest(request));
    }

    @Test
    void isApiStyleRequest_trueForXmlHttpRequestHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/quizzes");
        request.addHeader("X-Requested-With", "XMLHttpRequest");

        assertTrue(SecurityRequestMatchers.isApiStyleRequest(request));
    }

    @Test
    void isApiStyleRequest_falseForBrowserRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/quizzes.html");
        request.addHeader("Accept", "text/html");

        assertFalse(SecurityRequestMatchers.isApiStyleRequest(request));
    }

    @Test
    void apiStyleRequestMatcher_delegatesToSharedLogic() {
        RequestMatcher matcher = SecurityRequestMatchers.apiStyleRequestMatcher();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/news");

        assertTrue(matcher.matches(request));
    }
}
