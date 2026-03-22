package com.ande.pubquizzz.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessValidationException_returns400WithErrorBody() {
        var ex = new BusinessValidationException("invalid input");
        ResponseEntity<Map<String, String>> response = handler.handleBusinessValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid input", response.getBody().get("error"));
    }

    @Test
    void handleResourceNotFoundException_returns404WithErrorBody() {
        var ex = new ResourceNotFoundException("not found");
        ResponseEntity<Map<String, String>> response = handler.handleResourceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("not found", response.getBody().get("error"));
    }

    @Test
    void handleGenericException_returns500WithGenericMessage() {
        var ex = new RuntimeException("internal details that must not leak");
        ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody().get("error"));
        assertFalse(response.getBody().get("error").contains("internal details"),
                "Server internals must not be leaked to client");
    }
}
