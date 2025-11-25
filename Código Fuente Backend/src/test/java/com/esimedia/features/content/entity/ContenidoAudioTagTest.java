package com.esimedia.features.content.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContenidoAudioTagTest {
    
    @Test
    void testDefaultConstructorAndGetters() {
        ContenidoAudioTag contenidoAudioTag = new ContenidoAudioTag();
        
        assertNull(contenidoAudioTag.getId());
        assertNull(contenidoAudioTag.getIdContenido());
        assertNull(contenidoAudioTag.getIdTag());
    }

    @Test
    void testParameterizedConstructor() {
        ContenidoAudioTag contenidoAudioTag = new ContenidoAudioTag("456", "789");
        
        assertNull(contenidoAudioTag.getId());
        assertEquals("456", contenidoAudioTag.getIdContenido());
        assertEquals("789", contenidoAudioTag.getIdTag());
    }

    @Test
    void testSetters() {
        ContenidoAudioTag contenidoAudioTag = new ContenidoAudioTag();
        
        contenidoAudioTag.setId("123");
        contenidoAudioTag.setIdContenido("456");
        contenidoAudioTag.setIdTag("789");
        
        assertEquals("123", contenidoAudioTag.getId());
        assertEquals("456", contenidoAudioTag.getIdContenido());
        assertEquals("789", contenidoAudioTag.getIdTag());
    }

    @Test
    void testToString() {
        ContenidoAudioTag contenidoAudioTag = new ContenidoAudioTag();
        contenidoAudioTag.setId("1");
        contenidoAudioTag.setIdContenido("10");
        contenidoAudioTag.setIdTag("20");
        
        String str = contenidoAudioTag.toString();
        
        assertTrue(str.contains("1"));
        assertTrue(str.contains("10"));
        assertTrue(str.contains("20"));
    }

    @Test
    void testEqualsWithSameObject() {
        ContenidoAudioTag contenidoAudioTag = new ContenidoAudioTag();
        contenidoAudioTag.setId("1");
        
        assertEquals(contenidoAudioTag, contenidoAudioTag);
    }

    @Test
    void testEqualsWithEqualObjects() {
        ContenidoAudioTag contenidoAudioTag1 = new ContenidoAudioTag();
        contenidoAudioTag1.setId("1");
        contenidoAudioTag1.setIdContenido("10");
        contenidoAudioTag1.setIdTag("20");
        
        ContenidoAudioTag contenidoAudioTag2 = new ContenidoAudioTag();
        contenidoAudioTag2.setId("1");
        contenidoAudioTag2.setIdContenido("10");
        contenidoAudioTag2.setIdTag("20");
        
        assertEquals(contenidoAudioTag1, contenidoAudioTag2);
    }

    @Test
    void testEqualsWithDifferentObjects() {
        ContenidoAudioTag contenidoAudioTag1 = new ContenidoAudioTag();
        contenidoAudioTag1.setId("1");
        
        ContenidoAudioTag contenidoAudioTag2 = new ContenidoAudioTag();
        contenidoAudioTag2.setId("2");
        
        assertNotEquals(contenidoAudioTag1, contenidoAudioTag2);
    }

    @Test
    void testEqualsWithNull() {
        ContenidoAudioTag contenidoAudioTag = new ContenidoAudioTag();
        
        assertNotEquals(null, contenidoAudioTag);
    }

    @Test
    void testEqualsWithDifferentClass() {
        ContenidoAudioTag contenidoAudioTag = new ContenidoAudioTag();
        String otherObject = "test";
        
        assertNotEquals(contenidoAudioTag, otherObject);
    }

    @Test
    void testHashCodeConsistency() {
        ContenidoAudioTag contenidoAudioTag = new ContenidoAudioTag();
        contenidoAudioTag.setId("1");
        contenidoAudioTag.setIdContenido("10");
        contenidoAudioTag.setIdTag("20");
        
        int hashCode1 = contenidoAudioTag.hashCode();
        int hashCode2 = contenidoAudioTag.hashCode();
        
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeWithEqualObjects() {
        ContenidoAudioTag contenidoAudioTag1 = new ContenidoAudioTag();
        contenidoAudioTag1.setId("1");
        contenidoAudioTag1.setIdContenido("10");
        contenidoAudioTag1.setIdTag("20");
        
        ContenidoAudioTag contenidoAudioTag2 = new ContenidoAudioTag();
        contenidoAudioTag2.setId("1");
        contenidoAudioTag2.setIdContenido("10");
        contenidoAudioTag2.setIdTag("20");
        
        assertEquals(contenidoAudioTag1.hashCode(), contenidoAudioTag2.hashCode());
    }

    @Test
    void testCanEqual() {
        ContenidoAudioTag contenidoAudioTag1 = new ContenidoAudioTag();
        ContenidoAudioTag contenidoAudioTag2 = new ContenidoAudioTag();
        
        assertEquals(contenidoAudioTag1, contenidoAudioTag2);
    }

    @Test
    void testEqualsWithPartiallyNullFields() {
        ContenidoAudioTag contenidoAudioTag1 = new ContenidoAudioTag();
        contenidoAudioTag1.setId("1");
        
        ContenidoAudioTag contenidoAudioTag2 = new ContenidoAudioTag();
        contenidoAudioTag2.setId("1");
        contenidoAudioTag2.setIdContenido("10");
        
        assertNotEquals(contenidoAudioTag1, contenidoAudioTag2);
    }
}