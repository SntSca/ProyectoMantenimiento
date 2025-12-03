package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListaUpdateDTOTest {

    @Test
    void testConstructorYGetters() {
        ListaUpdateDTO dto = new ListaUpdateDTO(
                "1", "Nombre", "Desc", true
        );

        assertEquals("1", dto.getIdLista());
        assertEquals("Nombre", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
        assertTrue(dto.getVisibilidad());
    }

    @Test
    void testSetters() {
        ListaUpdateDTO dto = new ListaUpdateDTO();

        dto.setIdLista("1");
        dto.setNombre("A");
        dto.setDescripcion("B");
        dto.setVisibilidad(false);

        assertEquals("1", dto.getIdLista());
        assertEquals("A", dto.getNombre());
        assertEquals("B", dto.getDescripcion());
        assertFalse(dto.getVisibilidad());
    }

    @Test
    void testEqualsAndHashCode() {
        ListaUpdateDTO dto1 = new ListaUpdateDTO("1", "A", "B", true);
        ListaUpdateDTO dto2 = new ListaUpdateDTO("1", "A", "B", true);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEqualsDifferentValues() {
        ListaUpdateDTO dto = new ListaUpdateDTO("1", "A", "B", true);

        assertNotEquals(dto, new ListaUpdateDTO("2", "A", "B", true));
        assertNotEquals(dto, new ListaUpdateDTO("1", "X", "B", true));
        assertNotEquals(dto, new ListaUpdateDTO("1", "A", "X", true));
        assertNotEquals(dto, new ListaUpdateDTO("1", "A", "B", false));
    }

    @Test
    void testEqualsNullAndOtherClass() {
        ListaUpdateDTO dto = new ListaUpdateDTO();
        assertNotEquals(dto, null);
        assertNotEquals(dto, "string");
    }

    @Test
    void testCanEqual() {
        ListaUpdateDTO dto = new ListaUpdateDTO();
        assertTrue(dto.canEqual(new ListaUpdateDTO()));
        assertFalse(dto.canEqual("string"));
    }

    @Test
    void testToString() {
        ListaUpdateDTO dto = new ListaUpdateDTO();
        assertNotNull(dto.toString());
    }
}
