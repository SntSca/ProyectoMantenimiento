package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListaPrivadaResponseDTOTest {

    @Test
    void testConstructorYGetters() {
        ContenidoListaResponseDTO contenido = new ContenidoListaResponseDTO();
        ListaPrivadaResponseDTO dto = new ListaPrivadaResponseDTO(
                "1", "Nombre", "Desc", "USR", true, List.of(contenido)
        );

        assertEquals("1", dto.getIdLista());
        assertEquals("Nombre", dto.getNombre());
        assertEquals("Desc", dto.getDescripcion());
        assertEquals("USR", dto.getIdCreadorUsuario());
        assertTrue(dto.getVisibilidad());
        assertEquals(1, dto.getContenidos().size());
    }

    @Test
    void testSetters() {
        ListaPrivadaResponseDTO dto = new ListaPrivadaResponseDTO();

        dto.setIdLista("1");
        dto.setNombre("A");
        dto.setDescripcion("B");
        dto.setIdCreadorUsuario("U");
        dto.setVisibilidad(false);
        dto.setContenidos(List.of());

        assertEquals("1", dto.getIdLista());
        assertEquals("A", dto.getNombre());
        assertEquals("B", dto.getDescripcion());
        assertEquals("U", dto.getIdCreadorUsuario());
        assertFalse(dto.getVisibilidad());
        assertEquals(0, dto.getContenidos().size());
    }

    @Test
    void testBuilder() {
        ListaPrivadaResponseDTO dto = ListaPrivadaResponseDTO.builder()
                .idLista("1")
                .nombre("Nombre")
                .descripcion("Desc")
                .idCreadorUsuario("USR")
                .visibilidad(true)
                .contenidos(List.of())
                .build();

        assertEquals("1", dto.getIdLista());
        assertEquals("Nombre", dto.getNombre());
    }

    @Test
    void testEqualsAndHashCode() {
        ListaPrivadaResponseDTO dto1 =
                new ListaPrivadaResponseDTO("1", "A", "B", "U", true, List.of());
        ListaPrivadaResponseDTO dto2 =
                new ListaPrivadaResponseDTO("1", "A", "B", "U", true, List.of());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEqualsDifferentValues() {
        ListaPrivadaResponseDTO dto =
                new ListaPrivadaResponseDTO("1", "A", "B", "U", true, List.of());

        assertNotEquals(dto,
                new ListaPrivadaResponseDTO("2", "A", "B", "U", true, List.of()));
        assertNotEquals(dto,
                new ListaPrivadaResponseDTO("1", "X", "B", "U", true, List.of()));
        assertNotEquals(dto,
                new ListaPrivadaResponseDTO("1", "A", "X", "U", true, List.of()));
        assertNotEquals(dto,
                new ListaPrivadaResponseDTO("1", "A", "B", "X", true, List.of()));
        assertNotEquals(dto,
                new ListaPrivadaResponseDTO("1", "A", "B", "U", false, List.of()));
    }

    @Test
    void testEqualsNullAndOtherClass() {
        ListaPrivadaResponseDTO dto = new ListaPrivadaResponseDTO();
        assertNotEquals(dto, null);
        assertNotEquals(dto, "string");
    }

    @Test
    void testCanEqual() {
        ListaPrivadaResponseDTO dto = new ListaPrivadaResponseDTO();
        assertTrue(dto.canEqual(new ListaPrivadaResponseDTO()));
        assertFalse(dto.canEqual("string"));
    }

    @Test
    void testToString() {
        ListaPrivadaResponseDTO dto = new ListaPrivadaResponseDTO();
        assertNotNull(dto.toString());
    }
}
