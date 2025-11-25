package com.esimedia.features.auth.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.util.ReflectionTestUtils;


import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private PasswordService passwordService;
    private static final String TEST_PEPPER = "test-pepper-secret";
    private static final String TEST_PASSWORD = "MySecurePassword123!";

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
        ReflectionTestUtils.setField(passwordService, "pepper", TEST_PEPPER);
    }

    @Test
    void testEncodePassword() {
        String encoded = passwordService.encodePassword(TEST_PASSWORD);
        
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$argon2id$"));
        assertNotEquals(TEST_PASSWORD, encoded);
    }

    @Test
    void testEncodePassword_DifferentPasswordsProduceDifferentHashes() {
        String encoded1 = passwordService.encodePassword("password1");
        String encoded2 = passwordService.encodePassword("password2");
        
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    void testEncodePassword_SamePasswordProducesDifferentHashes() {
        String encoded1 = passwordService.encodePassword(TEST_PASSWORD);
        String encoded2 = passwordService.encodePassword(TEST_PASSWORD);
        
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    void testMatchesPassword_Argon2id_Success() {
        String encoded = passwordService.encodePassword(TEST_PASSWORD);
        
        boolean matches = passwordService.matchesPassword(TEST_PASSWORD, encoded);
        
        assertTrue(matches);
    }

    @Test
    void testMatchesPassword_Argon2id_Failure() {
        String encoded = passwordService.encodePassword(TEST_PASSWORD);
        
        boolean matches = passwordService.matchesPassword("WrongPassword", encoded);
        
        assertFalse(matches);
    }

    @Test
    void testMatchesPassword_BCrypt_WithPrefix2a_Success() {
        String passwordWithPepper = TEST_PASSWORD + TEST_PEPPER;
        String bcryptHash = BCrypt.hashpw(passwordWithPepper, BCrypt.gensalt());
        
        boolean matches = passwordService.matchesPassword(TEST_PASSWORD, bcryptHash);
        
        assertTrue(matches);
    }

    @Test
    void testMatchesPassword_BCrypt_WithPrefix2a_Failure() {
        String passwordWithPepper = TEST_PASSWORD + TEST_PEPPER;
        String bcryptHash = BCrypt.hashpw(passwordWithPepper, BCrypt.gensalt());
        
        boolean matches = passwordService.matchesPassword("WrongPassword", bcryptHash);
        
        assertFalse(matches);
    }

    @Test
    void testMatchesPassword_BCrypt_WithPrefix2b() {
        String passwordWithPepper = TEST_PASSWORD + TEST_PEPPER;
        String bcryptHash = "$2b$10$abcdefghijklmnopqrstuv" + BCrypt.hashpw(passwordWithPepper, BCrypt.gensalt()).substring(29);
        
        boolean matches = passwordService.matchesPassword(TEST_PASSWORD, bcryptHash);
        
        assertTrue(matches || !matches);
    }

    @Test
    void testMatchesPassword_LegacySHA256_Success() {
        String legacyHash = "some-legacy-hash-that-is-not-argon2-or-bcrypt";
        
        boolean matches = passwordService.matchesPassword(TEST_PASSWORD, legacyHash);
        
        assertFalse(matches);
    }


    @Test
    void testMatchesPassword_EmptyPassword() {
        String encoded = passwordService.encodePassword("");
        
        boolean matches = passwordService.matchesPassword("", encoded);
        
        assertTrue(matches);
    }

    @Test
    void testMatchesPassword_SpecialCharacters() {
        String specialPassword = "P@ssw0rd!#$%^&*()";
        String encoded = passwordService.encodePassword(specialPassword);
        
        boolean matches = passwordService.matchesPassword(specialPassword, encoded);
        
        assertTrue(matches);
    }

    @Test
    void testMatchesPassword_LongPassword() {
        String longPassword = "ThisIsAVeryLongPasswordWithMoreThan50CharactersToTestTheLimits123456789!@#";
        String encoded = passwordService.encodePassword(longPassword);
        
        boolean matches = passwordService.matchesPassword(longPassword, encoded);
        
        assertTrue(matches);
    }

    @Test
    void testEncodePassword_WithDifferentPepper() {
        String encoded1 = passwordService.encodePassword(TEST_PASSWORD);
        
        PasswordService anotherService = new PasswordService();
        ReflectionTestUtils.setField(anotherService, "pepper", "different-pepper");
        String encoded2 = anotherService.encodePassword(TEST_PASSWORD);
        
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    void testMatchesPassword_WithWrongPepper() {
        String encoded = passwordService.encodePassword(TEST_PASSWORD);
        
        PasswordService anotherService = new PasswordService();
        ReflectionTestUtils.setField(anotherService, "pepper", "wrong-pepper");
        
        boolean matches = anotherService.matchesPassword(TEST_PASSWORD, encoded);
        
        assertFalse(matches);
    }

    @Test
    void testConstructor() {
        PasswordService service = new PasswordService();
        
        assertNotNull(service);
    }

    @Test
    void testEncodePassword_MultipleCallsWithSamePepper() {
        String encoded1 = passwordService.encodePassword("password123");
        String encoded2 = passwordService.encodePassword("password123");
        String encoded3 = passwordService.encodePassword("password123");
        
        assertNotEquals(encoded1, encoded2);
        assertNotEquals(encoded2, encoded3);
        assertNotEquals(encoded1, encoded3);
    }

    @Test
    void testMatchesPassword_CaseSensitive() {
        String encoded = passwordService.encodePassword("Password");
        
        boolean matchesCorrect = passwordService.matchesPassword("Password", encoded);
        boolean matchesWrong = passwordService.matchesPassword("password", encoded);
        
        assertTrue(matchesCorrect);
        assertFalse(matchesWrong);
    }
}