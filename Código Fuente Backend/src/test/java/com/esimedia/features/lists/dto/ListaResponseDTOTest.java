package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListaResponseDTOTest {

    @Test
    void testConstructorYGetters() {
        ContenidoListaResponseDTO contenido = new ContenidoListaResponseDTO();
        ListaResponseDTO dto = new ListaResponseDTO(
                "1", "Lista A", "Desc", "USR1", true, List.of(contenido)
        );

        assertEquals("1", dto.getIdLista());
        assertEquals("Lista A", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals("USR1", dto.getIdCreadorUsuario());
        assertTrue(dto.getVisibilidad());
        assertEquals(1, dto.getContenidos().size());
    }

    @Test
    void testSetters() {
        ListaResponseDTO dto = new ListaResponseDTO();

        dto.setIdLista("1");
        dto.setNombre("Nombre");
        dto.setDescripcion("Desc");
        dto.setIdCreadorUsuario("USR");
        dto.setVisibilidad(false);
        dto.setContenidos(List.of());

        assertEquals("1", dto.getIdLista());
        assertEquals("Nombre", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals("USR", dto.getIdCreadorUsuario());
        assertFalse(dto.getVisibilidad());
    }

    @Test
    void testBuilder() {
        ListaResponseDTO dto = ListaResponseDTO.builder()
                .idLista("1")
                .nombre("Lista X")
                .descripcion("Desc X")
                .idCreadorUsuario("USR")
                .visibilidad(true)
                .contenidos(List.of())
                .build();

        assertEquals("Lista X", dto.getNombre());
    }

    @Test
    void testEqualsAndHashCode() {
        ListaResponseDTO dto1 = new ListaResponseDTO("1", "A", "B", "U", true, List.of());
        ListaResponseDTO dto2 = new ListaResponseDTO("1", "A", "B", "U", true, List.of());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEqualsDifferentValues() {
        ListaResponseDTO dto = new ListaResponseDTO("1", "A", "B", "U", true, List.of());

        assertNotEquals(dto, new ListaResponseDTO("2", "A", "B", "U", true, List.of()));
        assertNotEquals(dto, new ListaResponseDTO("1", "X", "B", "U", true, List.of()));
        assertNotEquals(dto, new ListaResponseDTO("1", "A", "X", "U", true, List.of()));
        assertNotEquals(dto, new ListaResponseDTO("1", "A", "B", "X", true, List.of()));
        assertNotEquals(dto, new ListaResponseDTO("1", "A", "B", "U", false, List.of()));
    }

    @Test
    void testEqualsWithNullAndDifferentClass() {
        ListaResponseDTO dto = new ListaResponseDTO();
        assertNotEquals(dto, null);
        assertNotEquals(dto, "string");
    }

    @Test
    void testCanEqual() {
        ListaResponseDTO dto = new ListaResponseDTO();
        assertTrue(dto.canEqual(new ListaResponseDTO()));
        assertFalse(dto.canEqual("string"));
    }

    @Test
    void testToString() {
        ListaResponseDTO dto = new ListaResponseDTO();
        assertNotNull(dto.toString());
    }
}
