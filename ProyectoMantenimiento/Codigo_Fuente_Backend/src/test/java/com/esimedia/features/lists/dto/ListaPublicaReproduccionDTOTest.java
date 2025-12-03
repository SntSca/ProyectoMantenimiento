package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ListaPublicaReproduccionDTOTest {

    private ListaPublicaReproduccionDTO sample1() {
        return new ListaPublicaReproduccionDTO(
                "1",
                "Mi Lista",
                "Descripción",
                "user123",
                Arrays.asList("c1", "c2"),
                true
        );
    }

    private ListaPublicaReproduccionDTO sample2() {
        return new ListaPublicaReproduccionDTO(
                "1",
                "Mi Lista",
                "Descripción",
                "user123",
                Arrays.asList("c1", "c2"),
                true
        );
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ListaPublicaReproduccionDTO dto = new ListaPublicaReproduccionDTO();

        dto.setIdLista("A");
        dto.setNombre("Nombre");
        dto.setDescripcion("Desc");
        dto.setIdCreadorUsuario("creator");
        dto.setContenidos(Arrays.asList("x", "y"));
        dto.setVisibilidad(false);

        assertEquals("A", dto.getIdLista());
        assertEquals("Nombre", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals("creator", dto.getIdCreadorUsuario());
        assertEquals(Arrays.asList("x", "y"), dto.getContenidos());
        assertFalse(dto.getVisibilidad());
    }

    @Test
    void testAllArgsConstructor() {
        ListaPublicaReproduccionDTO dto =
                new ListaPublicaReproduccionDTO("1", "N", "D", "creator", Arrays.asList("a"), true);

        assertEquals("1", dto.getIdLista());
        assertEquals("N", dto.getNombre());
        assertEquals("D", dto.getDescripcion());
        assertEquals("creator", dto.getIdCreadorUsuario());
        assertEquals(Arrays.asList("a"), dto.getContenidos());
        assertTrue(dto.getVisibilidad());
    }

    @Test
    void testEqualsSameObject() {
        ListaPublicaReproduccionDTO dto = sample1();
        assertEquals(dto, dto);
    }

    @Test
    void testEqualsEqualObjects() {
        assertEquals(sample1(), sample2());
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(sample1(), null);
    }

    @Test
    void testEqualsDifferentType() {
        assertNotEquals(sample1(), "string");
    }

    @Test
    void testEqualsDifferentIdLista() {
        ListaPublicaReproduccionDTO dto1 = sample1();
        ListaPublicaReproduccionDTO dto2 = sample1();
        dto2.setIdLista("XXX");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsDifferentNombre() {
        ListaPublicaReproduccionDTO dto1 = sample1();
        ListaPublicaReproduccionDTO dto2 = sample1();
        dto2.setNombre("Otro");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsDifferentDescripcion() {
        ListaPublicaReproduccionDTO dto1 = sample1();
        ListaPublicaReproduccionDTO dto2 = sample1();
        dto2.setDescripcion("AAAA");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsDifferentIdCreadorUsuario() {
        ListaPublicaReproduccionDTO dto1 = sample1();
        ListaPublicaReproduccionDTO dto2 = sample1();
        dto2.setIdCreadorUsuario("otro");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsDifferentContenidos() {
        ListaPublicaReproduccionDTO dto1 = sample1();
        ListaPublicaReproduccionDTO dto2 = sample1();
        dto2.setContenidos(Arrays.asList("ZZZ"));

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsDifferentVisibilidad() {
        ListaPublicaReproduccionDTO dto1 = sample1();
        ListaPublicaReproduccionDTO dto2 = sample1();
        dto2.setVisibilidad(false);

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testCanEqual() {
        ListaPublicaReproduccionDTO dto = sample1();
        assertTrue(dto.canEqual(sample1()));
        assertFalse(dto.canEqual(new Object()));
    }

    @Test
    void testHashCode() {
        assertEquals(sample1().hashCode(), sample2().hashCode());

        ListaPublicaReproduccionDTO dto = sample1();
        dto.setNombre("XX");

        assertNotEquals(sample1().hashCode(), dto.hashCode());
    }

    @Test
    void testToString() {
        ListaPublicaReproduccionDTO dto = sample1();
        String txt = dto.toString();

        assertNotNull(txt);
        assertTrue(txt.contains("Mi Lista"));
        assertTrue(txt.contains("Descripción"));
        assertTrue(txt.contains("user123"));
    }
}
