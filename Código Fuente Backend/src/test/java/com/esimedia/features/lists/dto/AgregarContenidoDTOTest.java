package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgregarContenidoDTOTest {

    @Test
    void testConstructorYGetters() {
        AgregarContenidoDTO dto = new AgregarContenidoDTO(
                "1", List.of("C1", "C2"), "USR"
        );

        assertEquals("1", dto.getIdLista());
        assertEquals(2, dto.getIdsContenido().size());
        assertEquals("USR", dto.getIdUsuario());
    }

    @Test
    void testSetters() {
        AgregarContenidoDTO dto = new AgregarContenidoDTO();

        dto.setIdLista("1");
        dto.setIdsContenido(List.of("A"));
        dto.setIdUsuario("USR");

        assertEquals("1", dto.getIdLista());
        assertEquals(1, dto.getIdsContenido().size());
        assertEquals("USR", dto.getIdUsuario());
    }

    @Test
    void testEqualsAndHashCode() {
        AgregarContenidoDTO dto1 = new AgregarContenidoDTO(
                "1", List.of("A"), "U"
        );
        AgregarContenidoDTO dto2 = new AgregarContenidoDTO(
                "1", List.of("A"), "U"
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEqualsDifferentValues() {
        AgregarContenidoDTO dto =
                new AgregarContenidoDTO("1", List.of("A"), "U");

        assertNotEquals(dto,
                new AgregarContenidoDTO("2", List.of("A"), "U"));
        assertNotEquals(dto,
                new AgregarContenidoDTO("1", List.of("B"), "U"));
        assertNotEquals(dto,
                new AgregarContenidoDTO("1", List.of("A"), "X"));
    }

    @Test
    void testEqualsNullAndOtherClass() {
        AgregarContenidoDTO dto = new AgregarContenidoDTO();
        assertNotEquals(dto, null);
        assertNotEquals(dto, "string");
    }

    @Test
    void testCanEqual() {
        AgregarContenidoDTO dto = new AgregarContenidoDTO();
        assertTrue(dto.canEqual(new AgregarContenidoDTO()));
        assertFalse(dto.canEqual("string"));
    }

    @Test
    void testToString() {
        AgregarContenidoDTO dto = new AgregarContenidoDTO();
        assertNotNull(dto.toString());
    }
}
