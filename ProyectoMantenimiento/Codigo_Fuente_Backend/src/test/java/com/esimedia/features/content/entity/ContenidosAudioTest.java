package com.esimedia.features.content.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContenidosAudioTest {
    
    @Test
    void testDefaultConstructor() {
        ContenidosAudio contenido = new ContenidosAudio();
        
        assertNull(contenido.getFichero());
        assertNull(contenido.getFicheroExtension());
    }

    @Test
    void testSetters() {
        ContenidosAudio contenido = new ContenidosAudio();
        byte[] fichero = new byte[]{10, 20, 30};
        
        contenido.setFichero(fichero);
        contenido.setFicheroExtension("audio/wav");
        
        assertArrayEquals(fichero, contenido.getFichero());
        assertEquals("audio/wav", contenido.getFicheroExtension());
    }

    @Test
    void testEqualsWithNull() {
        ContenidosAudio contenido = new ContenidosAudio();
        
        assertNotEquals(null, contenido);
    }

    @Test
    void testEqualsWithDifferentClass() {
        ContenidosAudio contenido = new ContenidosAudio();
        String otherObject = "test";
        
        assertNotEquals(contenido, otherObject);
    }

    @Test
    void testCanEqual() {
        ContenidosAudio contenido1 = new ContenidosAudio();
        ContenidosAudio contenido2 = new ContenidosAudio();
        
        assertEquals(contenido1, contenido2);
    }

    @Test
    void testGetters() {
        byte[] fichero = new byte[]{11, 22, 33};
        ContenidosAudio contenido = new ContenidosAudio();
        
        contenido.setFichero(fichero);
        contenido.setFicheroExtension("audio/ogg");
        
        assertArrayEquals(fichero, contenido.getFichero());
        assertEquals("audio/ogg", contenido.getFicheroExtension());
    }

    @Test
    void testHashCodeWithNullValues() {
        ContenidosAudio contenido = new ContenidosAudio();
        
        assertDoesNotThrow(contenido::hashCode);
    }

    @Test
    void testToStringWithNullValues() {
        ContenidosAudio contenido = new ContenidosAudio();
        
        assertDoesNotThrow(contenido::toString);
    }
}