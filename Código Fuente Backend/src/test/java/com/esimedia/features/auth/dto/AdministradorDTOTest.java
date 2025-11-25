package com.esimedia.features.auth.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdministradorDTOTest {

    @Test
    void testSettersAndGetters() {
        AdministradorDTO dto = new AdministradorDTO();
        dto.setEmail("admin@example.com");
        dto.setDepartamento("Recursos Humanos");

        assertEquals("admin@example.com", dto.getEmail());
        assertEquals("Recursos Humanos", dto.getDepartamento());
    }

    @Test
    void testEqualsHashCodeToString() {
        AdministradorDTO dto1 = new AdministradorDTO();
        dto1.setEmail("admin@example.com");
        dto1.setDepartamento("IT");

        AdministradorDTO dto2 = new AdministradorDTO();
        dto2.setEmail("admin2@example.com");
        dto2.setDepartamento("Marketing");

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
        AdministradorDTO dto = new AdministradorDTO();
        assertTrue(dto.canEqual(new AdministradorDTO()));
        assertFalse(dto.canEqual("otra clase"));
    }
}