package com.esimedia.features.favoritos.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContenidoFavoritoTest {

    @Test
    void testConstructorAndGetters() {
        ContenidoFavorito c = new ContenidoFavorito("1", "u1", "v1");
        assertEquals("1", c.getId());
        assertEquals("u1", c.getIdUsuario());
        assertEquals("v1", c.getIdContenido());
    }

    @Test
    void testSetters() {
        ContenidoFavorito c = new ContenidoFavorito();
        c.setId("10");
        c.setIdUsuario("user");
        c.setIdContenido("cont");

        assertEquals("10", c.getId());
        assertEquals("user", c.getIdUsuario());
        assertEquals("cont", c.getIdContenido());
    }

    @Test
    void testBuilder() {
        ContenidoFavorito c = ContenidoFavorito.builder()
                .id("1")
                .idUsuario("u1")
                .idContenido("c1")
                .build();

        assertEquals("1", c.getId());
        assertEquals("u1", c.getIdUsuario());
        assertEquals("c1", c.getIdContenido());
    }

    @Test
    void testEqualsSameObject() {
        ContenidoFavorito c = new ContenidoFavorito("1", "u1", "c1");
        assertEquals(c, c);
    }

    @Test
    void testEqualsDifferentObjectSameValues() {
        ContenidoFavorito c1 = new ContenidoFavorito("1", "u1", "c1");
        ContenidoFavorito c2 = new ContenidoFavorito("1", "u1", "c1");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testEqualsDifferentId() {
        ContenidoFavorito c1 = new ContenidoFavorito("1", "u1", "c1");
        ContenidoFavorito c2 = new ContenidoFavorito("2", "u1", "c1");

        assertNotEquals(c1, c2);
    }

    @Test
    void testEqualsDifferentIdUsuario() {
        ContenidoFavorito c1 = new ContenidoFavorito("1", "u1", "c1");
        ContenidoFavorito c2 = new ContenidoFavorito("1", "u2", "c1");

        assertNotEquals(c1, c2);
    }

    @Test
    void testEqualsDifferentIdContenido() {
        ContenidoFavorito c1 = new ContenidoFavorito("1", "u1", "c1");
        ContenidoFavorito c2 = new ContenidoFavorito("1", "u1", "c2");

        assertNotEquals(c1, c2);
    }

    @Test
    void testEqualsNull() {
        ContenidoFavorito c = new ContenidoFavorito("1", "u1", "c1");
        assertNotEquals(c, null);
    }

    @Test
    void testEqualsDifferentType() {
        ContenidoFavorito c = new ContenidoFavorito("1", "u1", "c1");
        assertNotEquals(c, "un string");
    }

    @Test
    void testHashCodeDifferentValues() {
        ContenidoFavorito c1 = new ContenidoFavorito("1", "u1", "c1");
        ContenidoFavorito c2 = new ContenidoFavorito("2", "u1", "c1");

        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testToStringContainsFields() {
        ContenidoFavorito c = new ContenidoFavorito("1", "u1", "c1");

        String s = c.toString();
        assertNotNull(s);
        assertTrue(s.contains("1"));
        assertTrue(s.contains("u1"));
        assertTrue(s.contains("c1"));
    }
}
