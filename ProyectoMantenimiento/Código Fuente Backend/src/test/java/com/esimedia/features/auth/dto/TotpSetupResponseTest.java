package com.esimedia.features.auth.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TotpSetupResponseTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        TotpSetupResponse response = new TotpSetupResponse();
        response.setQrCodeUrl("https://qrcode.com/example");
        response.setSecretKey("ABC123XYZ");

        assertEquals("https://qrcode.com/example", response.getQrCodeUrl());
        assertEquals("ABC123XYZ", response.getSecretKey());
    }

    @Test
    void testAllArgsConstructor() {
        TotpSetupResponse response = new TotpSetupResponse("url", "key");
        assertEquals("url", response.getQrCodeUrl());
        assertEquals("key", response.getSecretKey());
    }

    @Test
    void testEqualsAndHashCode() {
        TotpSetupResponse r1 = new TotpSetupResponse("url", "key");
        TotpSetupResponse r2 = new TotpSetupResponse("url", "key");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        r2.setSecretKey("different");
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        TotpSetupResponse response = new TotpSetupResponse("https://example.com", "XYZ");
        String str = response.toString();
        assertTrue(str.contains("https://example.com"));
        assertTrue(str.contains("XYZ"));
    }
}
