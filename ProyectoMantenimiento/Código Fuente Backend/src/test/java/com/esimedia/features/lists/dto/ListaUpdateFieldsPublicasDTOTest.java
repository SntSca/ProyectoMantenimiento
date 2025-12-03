package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListaUpdateFieldsPublicasDTOTest {

    @Test
    void testConstructorAndGetters() {
        ListaUpdateFieldsPublicasDTO dto = new ListaUpdateFieldsPublicasDTO(true);
        assertTrue(dto.getVisibilidad());
    }

    @Test
    void testSetters() {
        ListaUpdateFieldsPublicasDTO dto = new ListaUpdateFieldsPublicasDTO();
        dto.setVisibilidad(false);
        assertFalse(dto.getVisibilidad());
    }

    @Test
    void testEqualsAndHashCode() {
        ListaUpdateFieldsPublicasDTO a = new ListaUpdateFieldsPublicasDTO(true);
        ListaUpdateFieldsPublicasDTO b = new ListaUpdateFieldsPublicasDTO(true);
        ListaUpdateFieldsPublicasDTO c = new ListaUpdateFieldsPublicasDTO(false);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void testEqualsWithDifferentObject() {
        ListaUpdateFieldsPublicasDTO dto = new ListaUpdateFieldsPublicasDTO(true);
        assertNotEquals(dto, new Object());
    }

    @Test
    void testToString() {
        ListaUpdateFieldsPublicasDTO dto = new ListaUpdateFieldsPublicasDTO(true);
        String result = dto.toString();
        assertNotNull(result);
        assertTrue(result.contains("true"));
    }

    @Test
    void testCanEqual() {
        ListaUpdateFieldsPublicasDTO dto = new ListaUpdateFieldsPublicasDTO();
        ListaUpdateFieldsPublicasDTO dto2 = new ListaUpdateFieldsPublicasDTO();
        assertTrue(dto.canEqual(dto2));
    }
}