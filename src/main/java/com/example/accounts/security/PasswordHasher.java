package com.example.accounts.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 — real per-account hashing (never a stored plaintext
 * password), though honestly not what a from-scratch design would use
 * today: a proper KDF (bcrypt/scrypt/Argon2) is deliberately slow to
 * resist brute-forcing, and SHA-256 is fast, which is a real weakness.
 * Kept simple and dependency-free here since it's illustrating "role-
 * based secure login" for a portfolio project, not shipping production
 * auth — see README "About password hashing".
 */
public final class PasswordHasher {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String plaintextPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(plaintextPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed present on every standard JVM.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String plaintextPassword, String salt, String expectedHash) {
        String actual = hash(plaintextPassword, salt);
        return constantTimeEquals(actual, expectedHash);
    }

    /** Ordinary String.equals() short-circuits on the first mismatched character — a timing side-channel for a hash comparison. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private PasswordHasher() {
    }
}
