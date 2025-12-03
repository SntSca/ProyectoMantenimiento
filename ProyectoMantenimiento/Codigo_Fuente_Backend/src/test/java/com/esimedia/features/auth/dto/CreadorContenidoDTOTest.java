package com.esimedia.features.auth.dto;

import com.esimedia.features.auth.enums.TipoContenido;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreadorContenidoDTOTest {

    @Test
    void testGettersAndSetters() {
        CreadorContenidoDTO dto = new CreadorContenidoDTO();
        dto.setAliasCreador("alias123");
        dto.setDescripcion("Descripción de prueba");
        dto.setTipoContenido(TipoContenido.VIDEO);
        dto.setEspecialidad("Java");

        assertEquals("alias123", dto.getAliasCreador());
        assertEquals("Descripción de prueba", dto.getDescripcion());
        assertEquals(TipoContenido.VIDEO, dto.getTipoContenido());
        assertEquals("Java", dto.getEspecialidad());
    }

    @Test
    void testEqualsAndHashCode() {
        CreadorContenidoDTO dto1 = new CreadorContenidoDTO();
        dto1.setAliasCreador("alias1");
        dto1.setDescripcion("desc");
        dto1.setTipoContenido(TipoContenido.VIDEO);
        dto1.setEspecialidad("Tech");

        CreadorContenidoDTO dto2 = new CreadorContenidoDTO();
        dto2.setAliasCreador("alias1");
        dto2.setDescripcion("desc");
        dto2.setTipoContenido(TipoContenido.VIDEO);
        dto2.setEspecialidad("Tech");

        // Mismo contenido → equals y hashCode deben coincidir
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        // Diferente contenido → no deben ser iguales
        dto2.setAliasCreador("otroAlias");
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToString() {
        CreadorContenidoDTO dto = new CreadorContenidoDTO();
        dto.setAliasCreador("aliasTest");
        dto.setDescripcion("descripcionTest");
        dto.setTipoContenido(TipoContenido.VIDEO);
        dto.setEspecialidad("Comunicación");

        String toString = dto.toString();
        assertTrue(toString.contains("aliasTest"));
        assertTrue(toString.contains("descripcionTest"));
        assertTrue(toString.contains("VIDEO"));
    }
}
