package com.esimedia.features.content.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContentAudioUploadDTOTest {

    @Test
    void testGettersAndSetters() {
        ContentAudioUploadDTO dto = new ContentAudioUploadDTO();
        dto.setFichero("audio.mp3");
        dto.setFicheroExtension("audio/mpeg");

        assertEquals("audio.mp3", dto.getFichero());
        assertEquals("audio/mpeg", dto.getFicheroExtension());
    }

    @Test
    void testEqualsAndHashCode() {
        ContentAudioUploadDTO dto1 = new ContentAudioUploadDTO();
        dto1.setFichero("song.wav");
        dto1.setFicheroExtension("audio/wav");

        ContentAudioUploadDTO dto2 = new ContentAudioUploadDTO();
        dto2.setFichero("song.wav");
        dto2.setFicheroExtension("audio/wav");

        // Mismo contenido → deben ser iguales
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        // Distinto contenido → deben ser distintos
        dto2.setFichero("track.mp3");
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToStringContainsFields() {
        ContentAudioUploadDTO dto = new ContentAudioUploadDTO();
        dto.setFichero("archivo.ogg");
        dto.setFicheroExtension("audio/ogg");

        String result = dto.toString();
        assertTrue(result.contains("archivo.ogg"));
        assertTrue(result.contains("audio/ogg"));
    }
}
