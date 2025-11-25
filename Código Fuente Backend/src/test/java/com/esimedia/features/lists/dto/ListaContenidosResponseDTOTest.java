package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListaContenidosResponseDTOTest {

    @Test
    void testConstructorYGetters() {
        ContenidoListaResponseDTO contenido = new ContenidoListaResponseDTO();
        ListaContenidosResponseDTO dto = new ListaContenidosResponseDTO(
                "1", "Lista A", "Descripción", "USR1", true, List.of(contenido)
        );

        assertEquals("1", dto.getIdLista());
        assertEquals("Lista A", dto.getNombre());
        assertEquals("Descripción", dto.getDescripcion());
        assertEquals("USR1", dto.getIdCreadorUsuario());
        assertTrue(dto.getVisibilidad());
        assertEquals(1, dto.getContenidos().size());
    }

    @Test
    void testSetters() {
        ListaContenidosResponseDTO dto = new ListaContenidosResponseDTO();

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
        assertEquals(0, dto.getContenidos().size());
    }

    @Test
    void testBuilder() {
        ListaContenidosResponseDTO dto = ListaContenidosResponseDTO.builder()
                .idLista("1")
                .nombre("Lista X")
                .descripcion("Desc X")
                .idCreadorUsuario("USR")
                .visibilidad(true)
                .contenidos(List.of())
                .build();

        assertEquals("1", dto.getIdLista());
        assertEquals("Lista X", dto.getNombre());
    }

    @Test
    void testEqualsAndHashCode() {
        ListaContenidosResponseDTO dto1 = new ListaContenidosResponseDTO("1", "A", "B", "U", true, List.of());
        ListaContenidosResponseDTO dto2 = new ListaContenidosResponseDTO("1", "A", "B", "U", true, List.of());

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEqualsDifferentValues() {
        ListaContenidosResponseDTO dto = new ListaContenidosResponseDTO("1", "A", "B", "U", true, List.of());

        assertNotEquals(dto, new ListaContenidosResponseDTO("2", "A", "B", "U", true, List.of()));
        assertNotEquals(dto, new ListaContenidosResponseDTO("1", "X", "B", "U", true, List.of()));
        assertNotEquals(dto, new ListaContenidosResponseDTO("1", "A", "X", "U", true, List.of()));
        assertNotEquals(dto, new ListaContenidosResponseDTO("1", "A", "B", "X", true, List.of()));
        assertNotEquals(dto, new ListaContenidosResponseDTO("1", "A", "B", "U", false, List.of()));
    }

    @Test
    void testEqualsWithNullAndDifferentClass() {
        ListaContenidosResponseDTO dto = new ListaContenidosResponseDTO();
        assertNotEquals(dto, null);
        assertNotEquals(dto, "string");
    }

    @Test
    void testCanEqual() {
        ListaContenidosResponseDTO dto = new ListaContenidosResponseDTO();
        assertTrue(dto.canEqual(new ListaContenidosResponseDTO()));
        assertFalse(dto.canEqual("string"));
    }

    @Test
    void testToString() {
        ListaContenidosResponseDTO dto = new ListaContenidosResponseDTO();
        assertNotNull(dto.toString());
    }
}
