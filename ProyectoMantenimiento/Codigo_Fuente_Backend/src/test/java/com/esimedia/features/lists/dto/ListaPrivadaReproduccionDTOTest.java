package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListaPrivadaReproduccionDTOTest {

    @Test
    void testConstructorYGetters() {
        ListaPrivadaReproduccionDTO dto =
                new ListaPrivadaReproduccionDTO("1", "Nombre", "Desc", "USR", List.of("C1"));

        assertEquals("1", dto.getIdLista());
        assertEquals("Nombre", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals("USR", dto.getIdCreadorUsuario());
        assertEquals(1, dto.getContenidos().size());
    }

    @Test
    void testSetters() {
        ListaPrivadaReproduccionDTO dto = new ListaPrivadaReproduccionDTO();

        dto.setIdLista("1");
        dto.setNombre("Nombre");
        dto.setDescripcion("Desc");
        dto.setIdCreadorUsuario("USR");
        dto.setContenidos(List.of("X"));

        assertEquals("1", dto.getIdLista());
        assertEquals("Nombre", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals("USR", dto.getIdCreadorUsuario());
        assertEquals(1, dto.getContenidos().size());
    }

    @Test
    void testEqualsAndHashCode() {
        ListaPrivadaReproduccionDTO dto1 =
                new ListaPrivadaReproduccionDTO("1", "A", "B", "U", List.of("X"));
        ListaPrivadaReproduccionDTO dto2 =
                new ListaPrivadaReproduccionDTO("1", "A", "B", "U", List.of("X"));

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEqualsDifferentValues() {
        ListaPrivadaReproduccionDTO dto =
                new ListaPrivadaReproduccionDTO("1", "A", "B", "U", List.of("X"));

        assertNotEquals(dto, new ListaPrivadaReproduccionDTO("2", "A", "B", "U", List.of("X")));
        assertNotEquals(dto, new ListaPrivadaReproduccionDTO("1", "X", "B", "U", List.of("X")));
        assertNotEquals(dto, new ListaPrivadaReproduccionDTO("1", "A", "X", "U", List.of("X")));
        assertNotEquals(dto, new ListaPrivadaReproduccionDTO("1", "A", "B", "X", List.of("X")));
        assertNotEquals(dto, new ListaPrivadaReproduccionDTO("1", "A", "B", "U", List.of("Y")));
    }

    @Test
    void testEqualsWithNullAndDifferentClass() {
        ListaPrivadaReproduccionDTO dto = new ListaPrivadaReproduccionDTO();
        assertNotEquals(dto, null);
        assertNotEquals(dto, "string");
    }

    @Test
    void testCanEqual() {
        ListaPrivadaReproduccionDTO dto = new ListaPrivadaReproduccionDTO();
        assertTrue(dto.canEqual(new ListaPrivadaReproduccionDTO()));
        assertFalse(dto.canEqual("string"));
    }

    @Test
    void testToString() {
        ListaPrivadaReproduccionDTO dto = new ListaPrivadaReproduccionDTO();
        assertNotNull(dto.toString());
    }
}
