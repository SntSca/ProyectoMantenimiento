package com.esimedia.features.content.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ContentUploadDTOTest {

    @Test
    void testNoArgsConstructor() {
        ContentUploadDTO dto = new ContentUploadDTO();
        
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getTitulo());
        assertNull(dto.getDescripcion());
        assertNull(dto.getTags());
        assertNull(dto.getDuracion());
        assertNull(dto.getFechaSubida());
        assertNull(dto.getFechaExpiracion());
        assertNull(dto.getEsVIP());
        assertNull(dto.getMiniatura());
        assertNull(dto.getFormatoMiniatura());
    }

    @Test
    void testEqualsSameObject() {
        ContentUploadDTO dto = new ContentUploadDTO();
        dto.setId("same-id");
        
        assertEquals(dto, dto);
    }

    @Test
    void testEqualsNull() {
        ContentUploadDTO dto = new ContentUploadDTO();
        dto.setId("id-test");
        
        assertNotEquals(null, dto);
    }

    @Test
    void testEqualsWithDifferentId() {
        ContentUploadDTO dto1 = new ContentUploadDTO();
        dto1.setId("id-1");
        dto1.setTitulo("Same Title");

        ContentUploadDTO dto2 = new ContentUploadDTO();
        dto2.setId("id-2");
        dto2.setTitulo("Same Title");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsWithDifferentTitulo() {
        ContentUploadDTO dto1 = new ContentUploadDTO();
        dto1.setId("id-1");
        dto1.setTitulo("Title 1");

        ContentUploadDTO dto2 = new ContentUploadDTO();
        dto2.setId("id-1");
        dto2.setTitulo("Title 2");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsWithDifferentTags() {
        ContentUploadDTO dto1 = new ContentUploadDTO();
        dto1.setId("id-1");
        dto1.setTags(Arrays.asList("tag1", "tag2"));

        ContentUploadDTO dto2 = new ContentUploadDTO();
        dto2.setId("id-1");
        dto2.setTags(Arrays.asList("tag3", "tag4"));

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsWithDifferentDuracion() {
        ContentUploadDTO dto1 = new ContentUploadDTO();
        dto1.setId("id-1");
        dto1.setDuracion(1800);

        ContentUploadDTO dto2 = new ContentUploadDTO();
        dto2.setId("id-1");
        dto2.setDuracion(3600);

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEqualsWithDifferentEsVIP() {
        ContentUploadDTO dto1 = new ContentUploadDTO();
        dto1.setId("id-1");
        dto1.setEsVIP(true);

        ContentUploadDTO dto2 = new ContentUploadDTO();
        dto2.setId("id-1");
        dto2.setEsVIP(false);

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testSettersReturnVoid() {
        ContentUploadDTO dto = new ContentUploadDTO();
        
        // Verificar que los setters funcionan correctamente
        dto.setId("test");
        assertEquals("test", dto.getId());
        
        dto.setTitulo("test title");
        assertEquals("test title", dto.getTitulo());
        
        dto.setDescripcion("test description");
        assertEquals("test description", dto.getDescripcion());
    }

    @Test
    void testWithEmptyTags() {
        ContentUploadDTO dto = new ContentUploadDTO();
        dto.setTags(Arrays.asList());
        
        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());
    }

    @Test
    void testWithZeroDuration() {
        ContentUploadDTO dto = new ContentUploadDTO();
        dto.setDuracion(0);
        
        assertEquals(0, dto.getDuracion());
    }

    @Test
    void testWithNegativeDuration() {
        ContentUploadDTO dto = new ContentUploadDTO();
        dto.setDuracion(-100);
        
        assertEquals(-100, dto.getDuracion());
    }
}