package com.esimedia.features.content.entity;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class TagsTest {

    @Test
    @DisplayName("Debe crear un tag con constructor vacío")
    void testConstructorVacio() {
        // When
        Tags tag = new Tags();

        // Then
        assertNotNull(tag);
        assertNull(tag.getIdTag());
        assertNull(tag.getNombre());
    }

    @Test
    @DisplayName("Debe crear un tag con constructor de un parámetro")
    void testConstructorUnParametro() {
        // Given
        String nombre = "Programación";

        // When
        Tags tag = new Tags(nombre);

        // Then
        assertNotNull(tag);
        assertNull(tag.getIdTag());
        assertEquals(nombre, tag.getNombre());
    }

    @Test
    @DisplayName("Debe crear un tag con constructor de todos los parámetros")
    void testConstructorTodosParametros() {
        // Given
        String idTag = "tag123";
        String nombre = "Java";

        // When
        Tags tag = new Tags(idTag, nombre);

        // Then
        assertNotNull(tag);
        assertEquals(idTag, tag.getIdTag());
        assertEquals(nombre, tag.getNombre());
    }

    @Test
    @DisplayName("Debe establecer y obtener idTag correctamente")
    void testSetGetIdTag() {
        // Given
        Tags tag = new Tags();
        String idTag = "tag456";

        // When
        tag.setIdTag(idTag);

        // Then
        assertEquals(idTag, tag.getIdTag());
    }

    @Test
    @DisplayName("Debe establecer y obtener nombre correctamente")
    void testSetGetNombre() {
        // Given
        Tags tag = new Tags();
        String nombre = "Spring Boot";

        // When
        tag.setNombre(nombre);

        // Then
        assertEquals(nombre, tag.getNombre());
    }

    @Test
    @DisplayName("Debe validar equals cuando los objetos son iguales")
    void testEqualsTrue() {
        // Given
        Tags tag1 = new Tags("tag1", "Desarrollo");
        Tags tag2 = new Tags("tag1", "Desarrollo");

        // When & Then
        assertEquals(tag1, tag2);
    }

    @Test
    @DisplayName("Debe validar equals cuando los objetos son diferentes")
    void testEqualsFalse() {
        // Given
        Tags tag1 = new Tags("tag1", "Desarrollo");
        Tags tag2 = new Tags("tag2", "Testing");

        // When & Then
        assertNotEquals(tag1, tag2);
    }

    @Test
    @DisplayName("Debe validar equals con el mismo objeto")
    void testEqualsMismoObjeto() {
        // Given
        Tags tag = new Tags("tag1", "MongoDB");

        // When & Then
        assertEquals(tag, tag);
    }

    @Test
    @DisplayName("Debe validar equals con null")
    void testEqualsNull() {
        // Given
        Tags tag = new Tags("tag1", "Docker");

        // When & Then
        assertNotEquals(null, tag);
    }

    @Test
    @DisplayName("Debe validar equals con objeto de diferente clase")
    void testEqualsDiferenteClase() {
        // Given
        Tags tag = new Tags("tag1", "Kubernetes");
        String otroObjeto = "No soy un Tag";

        // When & Then
        assertNotEquals(tag, otroObjeto);
    }

    @Test
    @DisplayName("Debe validar equals con diferentes idTag")
    void testEqualsDiferenteIdTag() {
        // Given
        Tags tag1 = new Tags("tag1", "Backend");
        Tags tag2 = new Tags("tag2", "Backend");

        // When & Then
        assertNotEquals(tag1, tag2);
    }

    @Test
    @DisplayName("Debe validar equals con diferentes nombres")
    void testEqualsDiferenteNombre() {
        // Given
        Tags tag1 = new Tags("tag1", "Frontend");
        Tags tag2 = new Tags("tag1", "Backend");

        // When & Then
        assertNotEquals(tag1, tag2);
    }

    @Test
    @DisplayName("Debe generar el mismo hashCode para objetos iguales")
    void testHashCodeIguales() {
        // Given
        Tags tag1 = new Tags("tag1", "API");
        Tags tag2 = new Tags("tag1", "API");

        // When & Then
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test
    @DisplayName("Debe generar hashCode diferente para objetos diferentes")
    void testHashCodeDiferentes() {
        // Given
        Tags tag1 = new Tags("tag1", "REST");
        Tags tag2 = new Tags("tag2", "GraphQL");

        // When & Then
        assertNotEquals(tag1.hashCode(), tag2.hashCode());
    }

    @Test
    @DisplayName("Debe generar toString con todos los campos")
    void testToString() {
        // Given
        Tags tag = new Tags("tag789", "Microservicios");

        // When
        String resultado = tag.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("tag789"));
        assertTrue(resultado.contains("Microservicios"));
    }
}