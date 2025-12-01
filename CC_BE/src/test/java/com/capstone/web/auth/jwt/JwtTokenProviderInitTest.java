package com.capstone.web.auth.jwt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderInitTest {

    @Test
    @DisplayName("init() accepts non-Base64 (falls back to raw) when length >= 32")
    void init_acceptsRawSecret() {
        JwtProperties props = new JwtProperties();
        props.setSecret("TeBN3BMojFjhg-LW6XqZNKU80egfswI6PcJpc0WtadiXuf1dZUmse6GpPdpg6HHkK"); // contains '-'
        props.setExpirationMillis(3600000);

        JwtTokenProvider provider = new JwtTokenProvider(props);
        assertDoesNotThrow(provider::init);
    }

    @Test
    @DisplayName("init() accepts standard Base64 secrets")
    void init_acceptsBase64() {
        // Default from application.yml (decodes to a long-enough secret)
        String base64 = "dGVzdF9qd3Rfc2VjcmV0X2tleV9zdHJpbmdfd2hpY2hfaXNfbG9uZ19lbm91Z2hfMzJieXRlcw==";
        JwtProperties props = new JwtProperties();
        props.setSecret(base64);
        props.setExpirationMillis(3600000);

        JwtTokenProvider provider = new JwtTokenProvider(props);
        assertDoesNotThrow(provider::init);
    }

    @Test
    @DisplayName("init() rejects too-short secrets (<32 bytes)")
    void init_rejectsTooShort() {
        JwtProperties props = new JwtProperties();
        props.setSecret("short-secret-1234567890"); // < 32 bytes
        props.setExpirationMillis(3600000);

        JwtTokenProvider provider = new JwtTokenProvider(props);
        IllegalStateException ex = assertThrows(IllegalStateException.class, provider::init);
        assertTrue(ex.getMessage().contains("too short"));
    }
}
