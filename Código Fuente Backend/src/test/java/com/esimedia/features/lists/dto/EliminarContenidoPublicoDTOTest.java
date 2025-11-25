package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class EliminarContenidoPublicoDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        EliminarContenidoPublicoDTO dto = new EliminarContenidoPublicoDTO();

        dto.setIdLista("LISTA1");
        dto.setIdsContenido(Arrays.asList("C1", "C2"));

        assertEquals("LISTA1", dto.getIdLista());
        assertEquals(Arrays.asList("C1", "C2"), dto.getIdsContenido());
    }

    @Test
    void testAllArgsConstructor() {
        EliminarContenidoPublicoDTO dto =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A", "B"));

        assertEquals("L1", dto.getIdLista());
        assertEquals(Arrays.asList("A", "B"), dto.getIdsContenido());
    }

    @Test
    void testEqualsSameObject() {
        EliminarContenidoPublicoDTO dto =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"));
        assertEquals(dto, dto);
    }

    @Test
    void testEqualsNull() {
        EliminarContenidoPublicoDTO dto =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"));
        assertNotEquals(dto, null);
    }

    @Test
    void testEqualsDifferentType() {
        EliminarContenidoPublicoDTO dto =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"));
        assertNotEquals(dto, "string");
    }

    @Test
    void testEqualsEqualObjects() {
        EliminarContenidoPublicoDTO dto1 =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A", "B"));
        EliminarContenidoPublicoDTO dto2 =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A", "B"));

        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }

    @Test
    void testEqualsDifferentFieldIdLista() {
        EliminarContenidoPublicoDTO dto1 =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"));
        EliminarContenidoPublicoDTO dto2 =
                new EliminarContenidoPublicoDTO("L2", Arrays.asList("A"));

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsDifferentFieldIdsContenido() {
        EliminarContenidoPublicoDTO dto1 =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"));
        EliminarContenidoPublicoDTO dto2 =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("B"));

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsWithNullFields() {
        EliminarContenidoPublicoDTO dto1 = new EliminarContenidoPublicoDTO();
        EliminarContenidoPublicoDTO dto2 = new EliminarContenidoPublicoDTO();

        // ambos nulos → iguales
        assertEquals(dto1, dto2);

        // dto2 tiene idLista → distintos
        dto2.setIdLista("X");
        assertNotEquals(dto1, dto2);

        // ahora igualamos
        dto1.setIdLista("X");
        assertEquals(dto1, dto2);

        // idsContenido null vs no null
        dto1.setIdsContenido(null);
        dto2.setIdsContenido(Collections.singletonList("A"));
        assertNotEquals(dto1, dto2);

        dto1.setIdsContenido(Collections.singletonList("A"));
        assertEquals(dto1, dto2);
    }

    @Test
    void testCanEqual() {
        EliminarContenidoPublicoDTO dto = new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"));
        assertTrue(dto.canEqual(new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"))));
        assertFalse(dto.canEqual(new Object()));
    }

    @Test
    void testHashCode() {
        EliminarContenidoPublicoDTO dto1 =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"));
        EliminarContenidoPublicoDTO dto2 =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A"));

        assertEquals(dto1.hashCode(), dto2.hashCode());

        dto2.setIdLista("XXX");
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        EliminarContenidoPublicoDTO dto =
                new EliminarContenidoPublicoDTO("L1", Arrays.asList("A", "B"));

        String txt = dto.toString();

        assertNotNull(txt);
        assertTrue(txt.contains("L1"));
        assertTrue(txt.contains("A"));
    }
}
