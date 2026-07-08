package com.universidad.inscripciones.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${app.security.admin-token-secret}")
    private String secret;

    @Value("${app.security.admin-token-hours}")
    private long tokenHours;

    public TokenData generate(String username) {
        long expiresAt = System.currentTimeMillis() + (tokenHours * 60 * 60 * 1000);
        String payload = username + "|" + expiresAt;
        String encodedPayload = base64Url(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);
        return new TokenData(
                encodedPayload + "." + signature,
                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(expiresAt), ZoneId.systemDefault()));
    }

    public String validateAndGetUsername(String token) {
        if (token == null || token.isBlank() || !token.contains(".")) {
            throw new IllegalArgumentException("Token de administrador no valido.");
        }

        String[] parts = token.split("\\.", 2);
        String expectedSignature = sign(parts[0]);
        if (!constantTimeEquals(expectedSignature, parts[1])) {
            throw new IllegalArgumentException("Token de administrador no valido.");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] values = payload.split("\\|", 2);
        if (values.length != 2) {
            throw new IllegalArgumentException("Token de administrador no valido.");
        }

        long expiresAt = Long.parseLong(values[1]);
        if (System.currentTimeMillis() > expiresAt) {
            throw new IllegalArgumentException("La sesion de administrador expiro.");
        }

        return values[0];
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64Url(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo firmar el token de administrador.", ex);
        }
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }

    public record TokenData(String token, LocalDateTime expiresAt) {
    }
}
