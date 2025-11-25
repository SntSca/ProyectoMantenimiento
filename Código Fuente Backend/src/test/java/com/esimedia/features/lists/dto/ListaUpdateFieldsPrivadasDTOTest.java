package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListaUpdateFieldsPrivadasDTOTest {

    private ListaUpdateFieldsPrivadasDTO buildSample1() {
        ListaUpdateFieldsPrivadasDTO dto = new ListaUpdateFieldsPrivadasDTO();
        dto.setNombre("Lista A");
        dto.setDescripcion("Descripcion A");
        return dto;
    }

    private ListaUpdateFieldsPrivadasDTO buildSample2() {
        ListaUpdateFieldsPrivadasDTO dto = new ListaUpdateFieldsPrivadasDTO();
        dto.setNombre("Lista A");
        dto.setDescripcion("Descripcion A");
        return dto;
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ListaUpdateFieldsPrivadasDTO dto = new ListaUpdateFieldsPrivadasDTO();
        dto.setNombre("Test");
        dto.setDescripcion("Desc");

        assertEquals("Test", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
    }

    @Test
    void testEqualsSameObject() {
        ListaUpdateFieldsPrivadasDTO dto = buildSample1();
        assertEquals(dto, dto);
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(buildSample1(), null);
    }

    @Test
    void testEqualsDifferentType() {
        assertNotEquals(buildSample1(), "string");
    }

    @Test
    void testEqualsEqualObjects() {
        assertEquals(buildSample1(), buildSample2());
        assertEquals(buildSample2(), buildSample1());
    }

    @Test
    void testEqualsDifferentField() {
        ListaUpdateFieldsPrivadasDTO dto1 = buildSample1();
        ListaUpdateFieldsPrivadasDTO dto2 = buildSample1();
        dto2.setNombre("Distinto");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsWithSuperclassDifferentType() {
        // Objeto de la clase base NO debe ser igual
        ListaUpdateFieldsDTO base = new ListaUpdateFieldsDTO();
        base.setNombre("Lista A");
        base.setDescripcion("Descripcion A");

        assertNotEquals(buildSample1(), base);
        assertNotEquals(base, buildSample1());
    }

    @Test
    void testCanEqual() {
        ListaUpdateFieldsPrivadasDTO dto = buildSample1();
        assertTrue(dto.canEqual(buildSample1()));
        assertFalse(dto.canEqual(new ListaUpdateFieldsDTO()));
        assertFalse(dto.canEqual(new Object()));
    }

    @Test
    void testHashCode() {
        assertEquals(buildSample1().hashCode(), buildSample2().hashCode());

        ListaUpdateFieldsPrivadasDTO dto = buildSample1();
        dto.setNombre("Otro");

        assertNotEquals(buildSample1().hashCode(), dto.hashCode());
    }
    
    @Test
    void testToString() {
        ListaUpdateFieldsPrivadasDTO dto = buildSample1();
        String str = dto.toString();

        assertNotNull(str);
        assertTrue(str.contains("ListaUpdateFieldsPrivadasDTO("));
    }
}
