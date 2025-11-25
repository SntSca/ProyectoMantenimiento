package com.esimedia.features.lists.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListaBaseTest {

    private ListaBase buildSample1() {
        return ListaBase.builder()
                .idLista("1")
                .nombre("Lista A")
                .descripcion("Descripcion A")
                .idCreadorUsuario("user1")
                .visibilidad(true)
                .build();
    }

    private ListaBase buildSample2() {
        return ListaBase.builder()
                .idLista("2")
                .nombre("Lista B")
                .descripcion("Descripcion B")
                .idCreadorUsuario("user2")
                .visibilidad(false)
                .build();
    }

    @Test
    void testConstructorAndGetters() {
        ListaBase dto = new ListaBase("1", "Lista A", "Descripcion A", "user1", true);

        assertEquals("1", dto.getIdLista());
        assertEquals("Lista A", dto.getNombre());
        assertEquals("Descripcion A", dto.getDescripcion());
        assertEquals("user1", dto.getIdCreadorUsuario());
        assertTrue(dto.getVisibilidad());
    }

    @Test
    void testSetters() {
        ListaBase dto = new ListaBase();
        dto.setIdLista("5");
        dto.setNombre("Test");
        dto.setDescripcion("Desc");
        dto.setIdCreadorUsuario("u1");
        dto.setVisibilidad(false);

        assertEquals("5", dto.getIdLista());
        assertEquals("Test", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals("u1", dto.getIdCreadorUsuario());
        assertFalse(dto.getVisibilidad());
    }

    @Test
    void testBuilder() {
        ListaBase dto = buildSample1();
        assertEquals("Lista A", dto.getNombre());
    }

    @Test
    void testEquals() {
        ListaBase a = buildSample1();
        ListaBase b = buildSample1();
        ListaBase c = buildSample2();

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    void testHashCode() {
        ListaBase a = buildSample1();
        ListaBase b = buildSample1();
        ListaBase c = buildSample2();

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void testCanEqual() {
        ListaBase a = buildSample1();
        ListaBase b = buildSample1();

        assertTrue(a.canEqual(b));
        assertFalse(a.canEqual("string"));
    }

    @Test
    void testToString() {
        ListaBase dto = buildSample1();
        String s = dto.toString();

        assertNotNull(s);
        assertTrue(s.contains("ListaBase"));
        assertTrue(s.contains("Lista A"));
        assertTrue(s.contains("Descripcion A"));
        assertTrue(s.contains("user1"));
    }
}
