package com.esimedia.features.lists.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListaContenidoPublicaTest {

    private ListaContenidoPublica sample1() {
        return ListaContenidoPublica.builder()
                .id("1")
                .idLista("LX")
                .idContenido("CX")
                .build();
    }

    private ListaContenidoPublica sample2() {
        return ListaContenidoPublica.builder()
                .id("2")
                .idLista("LY")
                .idContenido("CY")
                .build();
    }

    @Test
    void testNoArgsConstructor() {
        ListaContenidoPublica dto = new ListaContenidoPublica();
        assertNotNull(dto);
    }

    @Test
    void testBuilder() {
        ListaContenidoPublica dto = sample1();
        assertEquals("1", dto.getId());
    }

    @Test
    void testEquals() {
        ListaContenidoPublica a = sample1();
        ListaContenidoPublica b = sample1();
        ListaContenidoPublica c = sample2();

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    void testHashCode() {
        ListaContenidoPublica a = sample1();
        ListaContenidoPublica b = sample1();
        ListaContenidoPublica c = sample2();

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void testCanEqual() {
        ListaContenidoPublica a = sample1();
        ListaContenidoPublica b = sample1();

        assertTrue(a.canEqual(b));
        assertFalse(a.canEqual("string"));
    }

    @Test
    void testToString() {
        ListaContenidoPublica dto = sample1();
        String s = dto.toString();

        assertNotNull(s);
        assertTrue(s.contains("ListaContenidoPublica"));
    }
}
