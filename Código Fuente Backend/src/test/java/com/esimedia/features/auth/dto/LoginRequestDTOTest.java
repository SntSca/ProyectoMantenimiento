package com.esimedia.features.auth.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("user@example.com");
        dto.setPassword("securePassword");

        assertEquals("user@example.com", dto.getEmail());
        assertEquals("securePassword", dto.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        LoginRequestDTO dto = new LoginRequestDTO("test@mail.com", "12345");
        assertEquals("test@mail.com", dto.getEmail());
        assertEquals("12345", dto.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        LoginRequestDTO dto1 = new LoginRequestDTO("mail@x.com", "pass");
        LoginRequestDTO dto2 = new LoginRequestDTO("mail@x.com", "pass");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        dto2.setEmail("different@x.com");
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToString() {
        LoginRequestDTO dto = new LoginRequestDTO("email@domain.com", "pwd");
        String result = dto.toString();
        assertTrue(result.contains("email@domain.com"));
        assertTrue(result.contains("pwd"));
    }
}
