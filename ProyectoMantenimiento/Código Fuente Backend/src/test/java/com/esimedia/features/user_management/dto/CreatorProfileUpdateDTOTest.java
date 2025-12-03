package com.esimedia.features.user_management.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreatorProfileUpdateDTOTest {

    @Test
    void constructor_CreatesEmptyInstance() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        
        assertNotNull(dto);
        assertNull(dto.getNombre());
        assertNull(dto.getApellidos());
        assertNull(dto.getAlias());
        assertNull(dto.getFotoPerfil());
        assertNull(dto.getAliasCreador());
        assertNull(dto.getDescripcion());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        
        dto.setNombre("Juan");
        dto.setApellidos("Pérez García");
        dto.setAlias("juanito");
        dto.setFotoPerfil("base64encodedimage");
        dto.setAliasCreador("juanCreator");
        dto.setDescripcion("Creador de contenido educativo");
        
        assertEquals("Juan", dto.getNombre());
        assertEquals("Pérez García", dto.getApellidos());
        assertEquals("juanito", dto.getAlias());
        assertEquals("base64encodedimage", dto.getFotoPerfil());
        assertEquals("juanCreator", dto.getAliasCreador());
        assertEquals("Creador de contenido educativo", dto.getDescripcion());
    }

    @Test
    void setNombre_WithDifferentValues_SetsCorrectly() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        
        dto.setNombre("María");
        assertEquals("María", dto.getNombre());
        
        dto.setNombre("Carlos");
        assertEquals("Carlos", dto.getNombre());
        
        dto.setNombre(null);
        assertNull(dto.getNombre());
    }

    @Test
    void setApellidos_WithDifferentValues_SetsCorrectly() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        
        dto.setApellidos("García López");
        assertEquals("García López", dto.getApellidos());
        
        dto.setApellidos(null);
        assertNull(dto.getApellidos());
    }

    @Test
    void setAlias_WithDifferentValues_SetsCorrectly() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        
        dto.setAlias("user123");
        assertEquals("user123", dto.getAlias());
        
        dto.setAlias(null);
        assertNull(dto.getAlias());
    }

    @Test
    void setFotoPerfil_WithDifferentValues_SetsCorrectly() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        
        dto.setFotoPerfil("imagedata");
        assertEquals("imagedata", dto.getFotoPerfil());
        
        dto.setFotoPerfil(null);
        assertNull(dto.getFotoPerfil());
    }

    @Test
    void setAliasCreador_WithDifferentValues_SetsCorrectly() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        
        dto.setAliasCreador("creatorAlias");
        assertEquals("creatorAlias", dto.getAliasCreador());
        
        dto.setAliasCreador(null);
        assertNull(dto.getAliasCreador());
    }

    @Test
    void setDescripcion_WithDifferentValues_SetsCorrectly() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        
        dto.setDescripcion("Mi descripción");
        assertEquals("Mi descripción", dto.getDescripcion());
        
        dto.setDescripcion(null);
        assertNull(dto.getDescripcion());
    }

    @Test
    void equals_WithSameValues_ReturnsTrue() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setNombre("Juan");
        dto1.setApellidos("Pérez");
        dto1.setAlias("juanito");
        dto1.setFotoPerfil("photo");
        dto1.setAliasCreador("juanCreator");
        dto1.setDescripcion("Description");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setNombre("Juan");
        dto2.setApellidos("Pérez");
        dto2.setAlias("juanito");
        dto2.setFotoPerfil("photo");
        dto2.setAliasCreador("juanCreator");
        dto2.setDescripcion("Description");

        assertEquals(dto1, dto2);
    }

    @Test
    void equals_WithDifferentNombre_ReturnsFalse() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setNombre("Juan");
        dto1.setApellidos("Pérez");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setNombre("María");
        dto2.setApellidos("Pérez");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_WithDifferentApellidos_ReturnsFalse() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setNombre("Juan");
        dto1.setApellidos("Pérez");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setNombre("Juan");
        dto2.setApellidos("García");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_WithDifferentAlias_ReturnsFalse() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setAlias("alias1");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setAlias("alias2");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_WithDifferentFotoPerfil_ReturnsFalse() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setFotoPerfil("photo1");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setFotoPerfil("photo2");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_WithDifferentAliasCreador_ReturnsFalse() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setAliasCreador("creator1");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setAliasCreador("creator2");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_WithDifferentDescripcion_ReturnsFalse() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setDescripcion("desc1");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setDescripcion("desc2");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void equals_WithNull_ReturnsFalse() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setNombre("Juan");

        assertNotEquals(null, dto);
    }

    @Test
    void equals_WithDifferentClass_ReturnsFalse() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setNombre("Juan");

        assertNotEquals("Not a DTO", dto);
    }

    @Test
    void equals_WithSameInstance_ReturnsTrue() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setNombre("Juan");

        assertEquals(dto, dto);
    }

    @Test
    void equals_WithNullFields_WorksCorrectly() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();

        assertEquals(dto1, dto2);
    }

    @Test
    void equals_WithOneNullField_ReturnsFalse() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setNombre("Juan");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setNombre(null);

        assertNotEquals(dto1, dto2);
    }

    @Test
    void hashCode_WithSameValues_ReturnsSameHashCode() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setNombre("Juan");
        dto1.setApellidos("Pérez");
        dto1.setAlias("juanito");
        dto1.setFotoPerfil("photo");
        dto1.setAliasCreador("juanCreator");
        dto1.setDescripcion("Description");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setNombre("Juan");
        dto2.setApellidos("Pérez");
        dto2.setAlias("juanito");
        dto2.setFotoPerfil("photo");
        dto2.setAliasCreador("juanCreator");
        dto2.setDescripcion("Description");

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void hashCode_WithDifferentValues_ReturnsDifferentHashCode() {
        CreatorProfileUpdateDTO dto1 = new CreatorProfileUpdateDTO();
        dto1.setNombre("Juan");

        CreatorProfileUpdateDTO dto2 = new CreatorProfileUpdateDTO();
        dto2.setNombre("María");

        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void hashCode_CalledMultipleTimes_ReturnsSameValue() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setNombre("Juan");
        dto.setApellidos("Pérez");

        int hash1 = dto.hashCode();
        int hash2 = dto.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void toString_ContainsAllFields() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setNombre("Juan");
        dto.setApellidos("Pérez");
        dto.setAlias("juanito");
        dto.setFotoPerfil("photo");
        dto.setAliasCreador("juanCreator");
        dto.setDescripcion("Description");

        String toString = dto.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Juan"));
        assertTrue(toString.contains("Pérez"));
        assertTrue(toString.contains("juanito"));
        assertTrue(toString.contains("photo"));
        assertTrue(toString.contains("juanCreator"));
        assertTrue(toString.contains("Description"));
    }

    @Test
    void toString_WithNullFields_WorksCorrectly() {
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();

        String toString = dto.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("CreatorProfileUpdateDTO"));
    }
}