package com.esimedia.features.lists.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ContenidoListaResponseDTOTest {

    private ContenidoListaResponseDTO buildSample1() {
        return ContenidoListaResponseDTO.builder()
                .id("1")
                .titulo("Titulo")
                .descripcion("Desc")
                .duracion(10)
                .especialidad("Yoga")
                .visibilidad(true)
                .tags(Arrays.asList("tag1", "tag2"))
                .esVIP(true)
                .miniatura("min.png")
                .formatoMiniatura("png")
                .fechaSubida("2025-01-01")
                .fechaExpiracion("2025-12-31")
                .valoracionMedia(4.5)
                .restriccionEdad(18)
                .urlArchivo("video.mp4")
                .resolucion("1080p")
                .fichero("audio.mp3")
                .ficheroExtension("mp3")
                .build();
    }

    private ContenidoListaResponseDTO buildSample2() {
        return ContenidoListaResponseDTO.builder()
                .id("1")
                .titulo("Titulo")
                .descripcion("Desc")
                .duracion(10)
                .especialidad("Yoga")
                .visibilidad(true)
                .tags(Arrays.asList("tag1", "tag2"))
                .esVIP(true)
                .miniatura("min.png")
                .formatoMiniatura("png")
                .fechaSubida("2025-01-01")
                .fechaExpiracion("2025-12-31")
                .valoracionMedia(4.5)
                .restriccionEdad(18)
                .urlArchivo("video.mp4")
                .resolucion("1080p")
                .fichero("audio.mp3")
                .ficheroExtension("mp3")
                .build();
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ContenidoListaResponseDTO dto = new ContenidoListaResponseDTO();

        dto.setId("X");
        dto.setTitulo("T");
        dto.setDescripcion("D");
        dto.setDuracion(20);
        dto.setEspecialidad("Cardio");
        dto.setVisibilidad(false);
        dto.setTags(Arrays.asList("x", "y"));
        dto.setEsVIP(false);
        dto.setMiniatura("img.jpg");
        dto.setFormatoMiniatura("jpg");
        dto.setFechaSubida("2025-02-01");
        dto.setFechaExpiracion("2025-12-01");
        dto.setValoracionMedia(3.2);
        dto.setRestriccionEdad(16);
        dto.setUrlArchivo("file.mp4");
        dto.setResolucion("4K");
        dto.setFichero("audio.wav");
        dto.setFicheroExtension("wav");

        assertEquals("X", dto.getId());
        assertEquals("T", dto.getTitulo());
        assertEquals("D", dto.getDescripcion());
        assertEquals(20, dto.getDuracion());
        assertEquals("Cardio", dto.getEspecialidad());
        assertFalse(dto.isVisibilidad());
        assertEquals(Arrays.asList("x", "y"), dto.getTags());
        assertFalse(dto.isEsVIP());
        assertEquals("img.jpg", dto.getMiniatura());
        assertEquals("jpg", dto.getFormatoMiniatura());
        assertEquals("2025-02-01", dto.getFechaSubida());
        assertEquals("2025-12-01", dto.getFechaExpiracion());
        assertEquals(3.2, dto.getValoracionMedia());
        assertEquals(16, dto.getRestriccionEdad());
        assertEquals("file.mp4", dto.getUrlArchivo());
        assertEquals("4K", dto.getResolucion());
        assertEquals("audio.wav", dto.getFichero());
        assertEquals("wav", dto.getFicheroExtension());
    }

    @Test
    void testAllArgsConstructor() {
        String f1 = "2025-03-01";
        String f2 = "2025-11-30";

        ContenidoListaResponseDTO dto = new ContenidoListaResponseDTO(
                "1", "T", "D", 10, "Yoga", true,
                Arrays.asList("a", "b"),
                true, "img", "png", f1, f2, 4.5, 18,
                "file", "1080p", "aud", "mp3"
        );

        assertEquals("1", dto.getId());
        assertEquals("T", dto.getTitulo());
        assertEquals("D", dto.getDescripcion());
        assertEquals(10, dto.getDuracion());
        assertEquals("Yoga", dto.getEspecialidad());
        assertTrue(dto.isVisibilidad());
        assertEquals(Arrays.asList("a", "b"), dto.getTags());
        assertTrue(dto.isEsVIP());
        assertEquals("img", dto.getMiniatura());
        assertEquals("png", dto.getFormatoMiniatura());
        assertEquals(f1, dto.getFechaSubida());
        assertEquals(f2, dto.getFechaExpiracion());
        assertEquals(4.5, dto.getValoracionMedia());
        assertEquals(18, dto.getRestriccionEdad());
        assertEquals("file", dto.getUrlArchivo());
        assertEquals("1080p", dto.getResolucion());
        assertEquals("aud", dto.getFichero());
        assertEquals("mp3", dto.getFicheroExtension());
    }

    @Test
    void testEqualsSameObject() {
        ContenidoListaResponseDTO dto = buildSample1();
        assertEquals(dto, dto);
    }

    @Test
    void testEqualsEqualObjects() {
        assertEquals(buildSample1(), buildSample2());
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
    void testEqualsDifferentField() {
        ContenidoListaResponseDTO dto1 = buildSample1();
        ContenidoListaResponseDTO dto2 = buildSample1();
        dto2.setTitulo("Otro");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testCanEqual() {
        ContenidoListaResponseDTO dto = buildSample1();
        assertTrue(dto.canEqual(buildSample1()));
        assertFalse(dto.canEqual(new Object()));
    }

    @Test
    void testHashCode() {
        assertEquals(buildSample1().hashCode(), buildSample2().hashCode());

        ContenidoListaResponseDTO dto = buildSample1();
        dto.setTitulo("XXX");

        assertNotEquals(buildSample1().hashCode(), dto.hashCode());
    }

    @Test
    void testToString() {
        ContenidoListaResponseDTO dto = buildSample1();
        String str = dto.toString();

        assertNotNull(str);
        assertTrue(str.contains("Titulo"));
        assertTrue(str.contains("Yoga"));
        assertTrue(str.contains("1080p"));
    }

    @Test
    void testBuilder() {
        ContenidoListaResponseDTO dto = ContenidoListaResponseDTO.builder()
                .id("10")
                .titulo("Hello")
                .build();

        assertEquals("10", dto.getId());
        assertEquals("Hello", dto.getTitulo());
    }

    @Test
    void testEqualsWithNullFields() {
        ContenidoListaResponseDTO dto1 = new ContenidoListaResponseDTO();
        ContenidoListaResponseDTO dto2 = new ContenidoListaResponseDTO();

        // Ambos nulos → deben ser iguales
        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);

        // dto1 tiene campo nulo, dto2 no → deben ser distintos
        dto2.setId("X");
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto2, dto1);

        // Ahora igualamos para cubrir la otra rama
        dto1.setId("X");
        assertEquals(dto1, dto2);

        // Campo null vs no null en otro atributo
        dto2.setTitulo("T");
        assertNotEquals(dto1, dto2);

        dto1.setTitulo("T");
        assertEquals(dto1, dto2);

        // Campo no null vs null (rama inversa)
        dto1.setDescripcion("A");
        dto2.setDescripcion(null);
        assertNotEquals(dto1, dto2);

        dto2.setDescripcion("A");
        assertEquals(dto1, dto2);
    }
}
