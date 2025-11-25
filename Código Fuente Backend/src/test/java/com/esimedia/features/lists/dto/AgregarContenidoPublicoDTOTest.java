package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgregarContenidoPublicoDTOTest {

    @Test
    void testConstructorAndGetters() {
        AgregarContenidoPublicoDTO dto =
                new AgregarContenidoPublicoDTO("1", List.of("A", "B"));

        assertEquals("1", dto.getIdLista());
        assertEquals(List.of("A", "B"), dto.getIdsContenido());
    }

    @Test
    void testSetters() {
        AgregarContenidoPublicoDTO dto = new AgregarContenidoPublicoDTO();

        dto.setIdLista("9");
        dto.setIdsContenido(List.of("X"));

        assertEquals("9", dto.getIdLista());
        assertEquals(List.of("X"), dto.getIdsContenido());
    }

    @Test
    void testEqualsAndHashCode() {
        AgregarContenidoPublicoDTO a =
                new AgregarContenidoPublicoDTO("1", List.of("A"));
        AgregarContenidoPublicoDTO b =
                new AgregarContenidoPublicoDTO("1", List.of("A"));
        AgregarContenidoPublicoDTO c =
                new AgregarContenidoPublicoDTO("3", List.of("Z"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void testEqualsWithDifferentObject() {
        AgregarContenidoPublicoDTO dto =
                new AgregarContenidoPublicoDTO("1", List.of("A"));
        assertNotEquals(dto, new Object());
    }

    @Test
    void testToString() {
        AgregarContenidoPublicoDTO dto =
                new AgregarContenidoPublicoDTO("1", List.of("A"));

        String result = dto.toString();
        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.contains("A"));
    }

    @Test
    void testCanEqual() {
        AgregarContenidoPublicoDTO dto = new AgregarContenidoPublicoDTO();
        AgregarContenidoPublicoDTO dto2 = new AgregarContenidoPublicoDTO();
        assertTrue(dto.canEqual(dto2));
    }
}
