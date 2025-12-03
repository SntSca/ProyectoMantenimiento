package com.esimedia.features.auth.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodeVerificationDTOTest {

    @Test
    void testSettersAndGetters() {
        CodeVerificationDTO dto = new CodeVerificationDTO();
        dto.setCode("123456");
        assertEquals("123456", dto.getCode());
    }

    @Test
    void testAllArgsConstructor() {
        CodeVerificationDTO dto = new CodeVerificationDTO("654321");
        assertEquals("654321", dto.getCode());
    }

    @Test
    void testEqualsHashCodeToString() {
        CodeVerificationDTO dto1 = new CodeVerificationDTO("111111");
        CodeVerificationDTO dto2 = new CodeVerificationDTO("222222");

        // equals
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, "string");
        assertNotEquals(dto1, dto2);
        assertEquals(dto1, dto1);

        // hashCode
        assertEquals(dto1.hashCode(), dto1.hashCode());

        // toString
        assertNotNull(dto1.toString());
    }

    @Test
    void testCanEqual() {
        CodeVerificationDTO dto = new CodeVerificationDTO();
        assertTrue(dto.canEqual(new CodeVerificationDTO()));
        assertFalse(dto.canEqual("otra clase"));
    }
}
