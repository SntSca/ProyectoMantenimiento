package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListaUpdateFieldsDTOTest {

    @Test
    void testConstructorAndGetters() {
        ListaUpdateFieldsDTO dto = new ListaUpdateFieldsDTO(
                "1",
                "Lista prueba",
                "Descripción"
        );

        assertEquals("1", dto.getIdLista());
        assertEquals("Lista prueba", dto.getNombre());
        assertEquals("Descripción", dto.getDescripcion());
    }

    @Test
    void testSetters() {
        ListaUpdateFieldsDTO dto = new ListaUpdateFieldsDTO();
        dto.setIdLista("2");
        dto.setNombre("Nombre");
        dto.setDescripcion("Desc");

        assertAll(
                () -> assertEquals("2", dto.getIdLista()),
                () -> assertEquals("Nombre", dto.getNombre()),
                () -> assertEquals("Desc", dto.getDescripcion())
        );
    }

    @Test
    void testEqualsAndHashCode() {
        ListaUpdateFieldsDTO a = new ListaUpdateFieldsDTO("1", "A", "B");
        ListaUpdateFieldsDTO b = new ListaUpdateFieldsDTO("1", "A", "B");
        ListaUpdateFieldsDTO c = new ListaUpdateFieldsDTO("9", "X", "Y");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void testEqualsWithDifferentObject() {
        ListaUpdateFieldsDTO dto = new ListaUpdateFieldsDTO("1", "A", "B");
        assertNotEquals(dto, new Object());
    }

    @Test
    void testToString() {
        ListaUpdateFieldsDTO dto = new ListaUpdateFieldsDTO("1", "A", "B");
        String result = dto.toString();
        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.contains("A"));
        assertTrue(result.contains("B"));
    }

    @Test
    void testCanEqual() {
        ListaUpdateFieldsDTO dto = new ListaUpdateFieldsDTO();
        ListaUpdateFieldsDTO dto2 = new ListaUpdateFieldsDTO();
        assertTrue(dto.canEqual(dto2));
    }
}
