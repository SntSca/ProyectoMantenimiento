package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EliminarContenidoDTOTest {

    @Test
    void testConstructorAndGetters() {
        EliminarContenidoDTO dto = new EliminarContenidoDTO(
                "1",
                List.of("A", "B"),
                "USR"
        );

        assertEquals("1", dto.getIdLista());
        assertEquals(List.of("A", "B"), dto.getIdsContenido());
        assertEquals("USR", dto.getIdUsuario());
    }

    @Test
    void testSetters() {
        EliminarContenidoDTO dto = new EliminarContenidoDTO();

        dto.setIdLista("2");
        dto.setIdsContenido(List.of("X"));
        dto.setIdUsuario("U");

        assertAll(
                () -> assertEquals("2", dto.getIdLista()),
                () -> assertEquals(List.of("X"), dto.getIdsContenido()),
                () -> assertEquals("U", dto.getIdUsuario())
        );
    }

    @Test
    void testEqualsAndHashCode() {
        EliminarContenidoDTO a = new EliminarContenidoDTO("1", List.of("A"), "U");
        EliminarContenidoDTO b = new EliminarContenidoDTO("1", List.of("A"), "U");
        EliminarContenidoDTO c = new EliminarContenidoDTO("9", List.of("Z"), "X");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void testEqualsWithDifferentObject() {
        EliminarContenidoDTO dto = new EliminarContenidoDTO("1", List.of("A"), "U");
        assertNotEquals(dto, new Object());
    }

    @Test
    void testToString() {
        EliminarContenidoDTO dto = new EliminarContenidoDTO("1", List.of("A"), "U");
        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.contains("A"));
        assertTrue(result.contains("U"));
    }

    @Test
    void testCanEqual() {
        EliminarContenidoDTO dto = new EliminarContenidoDTO();
        EliminarContenidoDTO dto2 = new EliminarContenidoDTO();
        assertTrue(dto.canEqual(dto2));
    }
}
