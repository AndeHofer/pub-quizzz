package com.ande.pubquizzz.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RememberMeKeyTest {

    @Test
    void acceptsBase64KeyWithAtLeast32Bytes() {
        String key = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
        assertEquals(key, RememberMeKey.validate(key));
    }

    @Test
    void rejectsMissingKey() {
        assertThrows(IllegalArgumentException.class, () -> RememberMeKey.validate(null));
        assertThrows(IllegalArgumentException.class, () -> RememberMeKey.validate(""));
        assertThrows(IllegalArgumentException.class, () -> RememberMeKey.validate("  "));
    }

    @Test
    void rejectsInvalidBase64AndShortKeys() {
        assertThrows(IllegalArgumentException.class, () -> RememberMeKey.validate("not base64!"));
        assertThrows(IllegalArgumentException.class, () -> RememberMeKey.validate("c2hvcnQ="));
    }
}
