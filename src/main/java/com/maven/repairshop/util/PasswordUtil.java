package com.maven.repairshop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final SecureRandom RNG = new SecureRandom();

    private PasswordUtil() {}

    // Format stocké : base64(salt) + ":" + base64(hash)
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Mot de passe obligatoire.");
        }
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);

        byte[] digest = sha256(concat(salt, rawPassword.getBytes(StandardCharsets.UTF_8)));

        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(digest);
    }

    public static boolean verify(String rawPassword, String stored) {
        if (rawPassword == null || rawPassword.isBlank() || stored == null || stored.isBlank()) {
            return false;
        }
        String[] parts = stored.split(":");
        if (parts.length != 2) return false;

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);

        byte[] actual = sha256(concat(salt, rawPassword.getBytes(StandardCharsets.UTF_8)));
        return constantTimeEquals(expected, actual);
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 indisponible", e);
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int r = 0;
        for (int i = 0; i < a.length; i++) r |= (a[i] ^ b[i]);
        return r == 0;
    }
}