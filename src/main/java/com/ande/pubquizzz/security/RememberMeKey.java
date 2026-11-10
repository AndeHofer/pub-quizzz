package com.ande.pubquizzz.security;

import java.util.Base64;

/**
 * Validates the externally supplied signing key without exposing its value.
 */
final class RememberMeKey {

    private static final String INVALID_KEY_MESSAGE =
            "REMEMBER_ME_KEY must be a Base64-encoded random key of at least 32 bytes";

    private RememberMeKey() {
    }

    static String validate(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(INVALID_KEY_MESSAGE);
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(key);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(INVALID_KEY_MESSAGE);
        }
        if (decoded.length < 32) {
            throw new IllegalArgumentException(INVALID_KEY_MESSAGE);
        }
        return key;
    }
}
