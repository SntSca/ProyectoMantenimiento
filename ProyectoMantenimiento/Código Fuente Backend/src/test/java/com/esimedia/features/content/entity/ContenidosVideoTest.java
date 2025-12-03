package com.esimedia.features.content.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.esimedia.features.content.enums.Resolucion;

class ContenidosVideoTest {
    
    @Test
    void testDefaultConstructor() {
        ContenidosVideo contenido = new ContenidosVideo();
        
        assertNull(contenido.getId());
        assertNull(contenido.getTitulo());
        assertNull(contenido.getDescripcion());
        assertNull(contenido.getUrlArchivo());
        assertNull(contenido.getResolucion());
    }

    @Test
    void testBuilderWithMinimalFields() {
        ContenidosVideo contenido = ContenidosVideo.builder()
            .titulo("Video Simple")
            .urlArchivo("https://ejemplo.com/simple.mp4")
            .build();
        
        assertEquals("Video Simple", contenido.getTitulo());
        assertEquals("https://ejemplo.com/simple.mp4", contenido.getUrlArchivo());
        assertNull(contenido.getDescripcion());
        assertNull(contenido.getResolucion());
    }

    @Test
    void testSetters() {
        ContenidosVideo contenido = new ContenidosVideo();
        
        contenido.setUrlArchivo("https://ejemplo.com/nuevo.mp4");
        contenido.setResolucion(Resolucion.HD_720);
        
        assertEquals("https://ejemplo.com/nuevo.mp4", contenido.getUrlArchivo());
        assertEquals(Resolucion.HD_720, contenido.getResolucion());
    }

    @Test
    void testEqualsWithSameObject() {
        ContenidosVideo contenido = ContenidosVideo.builder()
            .titulo("Video")
            .build();
        
        assertEquals(contenido, contenido);
    }

    @Test
    void testEqualsWithNull() {
        ContenidosVideo contenido = new ContenidosVideo();
        
        assertNotEquals(null, contenido);
    }

    @Test
    void testEqualsWithDifferentClass() {
        ContenidosVideo contenido = new ContenidosVideo();
        String otherObject = "test";
        
        assertNotEquals(contenido, otherObject);
    }

    @Test
    void testCanEqual() {
        ContenidosVideo contenido1 = new ContenidosVideo();
        ContenidosVideo contenido2 = new ContenidosVideo();
        
        assertEquals(contenido1, contenido2);
    }

}