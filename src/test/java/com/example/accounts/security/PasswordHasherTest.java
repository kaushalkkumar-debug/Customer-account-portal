package com.example.accounts.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void matchesReturnsTrueForTheCorrectPassword() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash("correct horse battery staple", salt);

        assertTrue(PasswordHasher.matches("correct horse battery staple", salt, hash));
    }

    @Test
    void matchesReturnsFalseForTheWrongPassword() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash("correct horse battery staple", salt);

        assertFalse(PasswordHasher.matches("wrong password", salt, hash));
    }

    @Test
    void theSamePasswordWithDifferentSaltsProducesDifferentHashes() {
        String saltA = PasswordHasher.generateSalt();
        String saltB = PasswordHasher.generateSalt();

        String hashA = PasswordHasher.hash("same password", saltA);
        String hashB = PasswordHasher.hash("same password", saltB);

        assertNotEquals(hashA, hashB, "identical passwords must not produce identical hashes when salted differently");
    }

    @Test
    void hashingIsDeterministicForTheSameSalt() {
        String salt = PasswordHasher.generateSalt();
        assertEquals(PasswordHasher.hash("password", salt), PasswordHasher.hash("password", salt));
    }

    @Test
    void generatedSaltsAreNotAllIdentical() {
        assertNotEquals(PasswordHasher.generateSalt(), PasswordHasher.generateSalt());
    }
}
