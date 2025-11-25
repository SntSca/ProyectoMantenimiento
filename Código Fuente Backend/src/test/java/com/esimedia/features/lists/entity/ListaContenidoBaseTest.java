package com.esimedia.features.lists.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListaContenidoBaseTest {

    private ListaContenidoBase sample1() {
        return ListaContenidoBase.builder()
                .id("1")
                .idLista("L1")
                .idContenido("C1")
                .build();
    }

    private ListaContenidoBase sample2() {
        return ListaContenidoBase.builder()
                .id("2")
                .idLista("L2")
                .idContenido("C2")
                .build();
    }

    @Test
    void testConstructorAndGetters() {
        ListaContenidoBase dto = new ListaContenidoBase("1", "L1", "C1");

        assertEquals("1", dto.getId());
        assertEquals("L1", dto.getIdLista());
        assertEquals("C1", dto.getIdContenido());
    }

    @Test
    void testSetters() {
        ListaContenidoBase dto = new ListaContenidoBase();
        dto.setId("9");
        dto.setIdLista("LX");
        dto.setIdContenido("CX");

        assertEquals("9", dto.getId());
        assertEquals("LX", dto.getIdLista());
        assertEquals("CX", dto.getIdContenido());
    }

    @Test
    void testBuilder() {
        ListaContenidoBase dto = sample1();
        assertEquals("1", dto.getId());
    }

    @Test
    void testEquals() {
        ListaContenidoBase a = sample1();
        ListaContenidoBase b = sample1();
        ListaContenidoBase c = sample2();

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    void testHashCode() {
        ListaContenidoBase a = sample1();
        ListaContenidoBase b = sample1();
        ListaContenidoBase c = sample2();

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void testCanEqual() {
        ListaContenidoBase a = sample1();
        ListaContenidoBase b = sample1();

        assertTrue(a.canEqual(b));
        assertFalse(a.canEqual("string"));
    }

    @Test
    void testToString() {
        ListaContenidoBase dto = sample1();
        String s = dto.toString();

        assertNotNull(s);
        assertTrue(s.contains("ListaContenidoBase"));
        assertTrue(s.contains("1"));
        assertTrue(s.contains("L1"));
        assertTrue(s.contains("C1"));
    }
}
