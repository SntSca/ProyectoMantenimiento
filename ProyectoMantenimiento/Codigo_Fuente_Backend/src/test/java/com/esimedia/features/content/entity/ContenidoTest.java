package com.esimedia.features.content.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContenidoTest {

    @Test
    void testCreateContenido() {
        Contenido c = Contenido.createContenido("Titulo", "Desc", 200, "user1");
        assertNotNull(c);
        assertEquals("Titulo", c.getTitulo());
        assertEquals("Desc", c.getDescripcion());
        assertEquals(200, c.getDuracion());
        assertEquals("user1", c.getIdCreador());
        assertFalse(c.isEsVIP());
        assertNotNull(c.getFechaSubida());
    }
}
