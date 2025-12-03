package com.esimedia.features.content.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContentVideoUploadDTOTest {

    @Test
    void testGettersAndSetters() {
        ContentVideoUploadDTO dto = new ContentVideoUploadDTO();
        dto.setUrlArchivo("https://video.com/test.mp4");
        dto.setResolucion("1080p");
        dto.setRestriccionEdad(18);

        assertEquals("https://video.com/test.mp4", dto.getUrlArchivo());
        assertEquals("1080p", dto.getResolucion());
        assertEquals(18, dto.getRestriccionEdad());
    }

    @Test
    void testEqualsAndHashCode() {
        ContentVideoUploadDTO dto1 = new ContentVideoUploadDTO();
        dto1.setUrlArchivo("url1");
        dto1.setResolucion("720p");
        dto1.setRestriccionEdad(12);

        ContentVideoUploadDTO dto2 = new ContentVideoUploadDTO();
        dto2.setUrlArchivo("url1");
        dto2.setResolucion("720p");
        dto2.setRestriccionEdad(12);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        dto2.setResolucion("480p");
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToStringContainsFields() {
        ContentVideoUploadDTO dto = new ContentVideoUploadDTO();
        dto.setUrlArchivo("archivo.mp4");
        dto.setResolucion("4K");
        dto.setRestriccionEdad(16);

        String toString = dto.toString();
        assertTrue(toString.contains("archivo.mp4"));
        assertTrue(toString.contains("4K"));
        assertTrue(toString.contains("16"));
    }
}
