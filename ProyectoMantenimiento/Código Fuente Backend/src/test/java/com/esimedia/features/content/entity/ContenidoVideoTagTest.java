package com.esimedia.features.content.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContenidoVideoTagTest {
    
    @Test
    void testConstructorAndGetters() {
        ContenidoVideoTag contenidoVideoTag = new ContenidoVideoTag();
        
        assertNull(contenidoVideoTag.getId());
        assertNull(contenidoVideoTag.getIdContenido());
        assertNull(contenidoVideoTag.getIdTag());
    }

    @Test
    void testSetters() {
        ContenidoVideoTag contenidoVideoTag = new ContenidoVideoTag();
        
        contenidoVideoTag.setId("123");
        contenidoVideoTag.setIdContenido("456");
        contenidoVideoTag.setIdTag("789");
        
        assertEquals("123", contenidoVideoTag.getId());
        assertEquals("456", contenidoVideoTag.getIdContenido());
        assertEquals("789", contenidoVideoTag.getIdTag());
    }

    @Test
    void testToString() {
        ContenidoVideoTag contenidoVideoTag = new ContenidoVideoTag();
        contenidoVideoTag.setId("1");
        contenidoVideoTag.setIdContenido("10");
        contenidoVideoTag.setIdTag("20");
        
        String str = contenidoVideoTag.toString();
        
        assertTrue(str.contains("1"));
        assertTrue(str.contains("10"));
        assertTrue(str.contains("20"));
    }

    @Test
    void testEqualsWithSameObject() {
        ContenidoVideoTag contenidoVideoTag = new ContenidoVideoTag();
        contenidoVideoTag.setId("1");
        
        assertEquals(contenidoVideoTag, contenidoVideoTag);
    }

    @Test
    void testEqualsWithEqualObjects() {
        ContenidoVideoTag contenidoVideoTag1 = new ContenidoVideoTag();
        contenidoVideoTag1.setId("1");
        contenidoVideoTag1.setIdContenido("10");
        contenidoVideoTag1.setIdTag("20");
        
        ContenidoVideoTag contenidoVideoTag2 = new ContenidoVideoTag();
        contenidoVideoTag2.setId("1");
        contenidoVideoTag2.setIdContenido("10");
        contenidoVideoTag2.setIdTag("20");
        
        assertEquals(contenidoVideoTag1, contenidoVideoTag2);
    }

    @Test
    void testEqualsWithDifferentObjects() {
        ContenidoVideoTag contenidoVideoTag1 = new ContenidoVideoTag();
        contenidoVideoTag1.setId("1");
        
        ContenidoVideoTag contenidoVideoTag2 = new ContenidoVideoTag();
        contenidoVideoTag2.setId("2");
        
        assertNotEquals(contenidoVideoTag1, contenidoVideoTag2);
    }

    @Test
    void testEqualsWithNull() {
        ContenidoVideoTag contenidoVideoTag = new ContenidoVideoTag();
        
        assertNotEquals(null, contenidoVideoTag);
    }

    @Test
    void testEqualsWithDifferentClass() {
        ContenidoVideoTag contenidoVideoTag = new ContenidoVideoTag();
        String otherObject = "test";
        
        assertNotEquals(contenidoVideoTag, otherObject);
    }

    @Test
    void testHashCodeConsistency() {
        ContenidoVideoTag contenidoVideoTag = new ContenidoVideoTag();
        contenidoVideoTag.setId("1");
        contenidoVideoTag.setIdContenido("10");
        contenidoVideoTag.setIdTag("20");
        
        int hashCode1 = contenidoVideoTag.hashCode();
        int hashCode2 = contenidoVideoTag.hashCode();
        
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeWithEqualObjects() {
        ContenidoVideoTag contenidoVideoTag1 = new ContenidoVideoTag();
        contenidoVideoTag1.setId("1");
        contenidoVideoTag1.setIdContenido("10");
        contenidoVideoTag1.setIdTag("20");
        
        ContenidoVideoTag contenidoVideoTag2 = new ContenidoVideoTag();
        contenidoVideoTag2.setId("1");
        contenidoVideoTag2.setIdContenido("10");
        contenidoVideoTag2.setIdTag("20");
        
        assertEquals(contenidoVideoTag1.hashCode(), contenidoVideoTag2.hashCode());
    }

    @Test
    void testCanEqual() {
        ContenidoVideoTag contenidoVideoTag1 = new ContenidoVideoTag();
        ContenidoVideoTag contenidoVideoTag2 = new ContenidoVideoTag();
        
        
        assertEquals(contenidoVideoTag1, contenidoVideoTag2);
    }

    @Test
    void testEqualsWithPartiallyNullFields() {
        ContenidoVideoTag contenidoVideoTag1 = new ContenidoVideoTag();
        contenidoVideoTag1.setId("1");
        
        ContenidoVideoTag contenidoVideoTag2 = new ContenidoVideoTag();
        contenidoVideoTag2.setId("1");
        contenidoVideoTag2.setIdContenido("10");
        
        assertNotEquals(contenidoVideoTag1, contenidoVideoTag2);
    }
}