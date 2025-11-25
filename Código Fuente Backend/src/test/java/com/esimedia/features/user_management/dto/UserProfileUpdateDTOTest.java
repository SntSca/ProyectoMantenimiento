package com.esimedia.features.user_management.dto;

import org.junit.jupiter.api.Test;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class UserProfileUpdateDTOTest {
    
    @Test
    void testGettersAndSetters() {
        // Arrange
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        String nombre = "Juan";
        String apellidos = "García López";
        String alias = "juangar";
        Date fecha = new Date();
        Boolean flagVIP = true;
        
        // Act
        dto.setNombre(nombre);
        dto.setApellidos(apellidos);
        dto.setAlias(alias);
        dto.setFechaNacimiento(fecha);
        dto.setFlagVIP(flagVIP);
        
        // Assert
        assertEquals(nombre, dto.getNombre());
        assertEquals(apellidos, dto.getApellidos());
        assertEquals(alias, dto.getAlias());
        assertEquals(fecha, dto.getFechaNacimiento());
        assertEquals(flagVIP, dto.getFlagVIP());
    }
    
    @Test
    void testConstructor() {
        // Act
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        
        // Assert
        assertNotNull(dto);
        assertNull(dto.getNombre());
        assertNull(dto.getApellidos());
        assertNull(dto.getAlias());
        assertNull(dto.getFechaNacimiento());
        assertNull(dto.getFlagVIP());
    }
    
    @Test
    void testEquals_SameObject() {
        // Arrange
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("Juan");
        
        // Act & Assert
        assertEquals(dto, dto);
    }
    
    @Test
    void testEquals_EqualObjects() {
        // Arrange
        Date fecha = new Date();
        
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setNombre("Juan");
        dto1.setApellidos("García");
        dto1.setAlias("juangar");
        dto1.setFechaNacimiento(fecha);
        dto1.setFlagVIP(true);
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        dto2.setNombre("Juan");
        dto2.setApellidos("García");
        dto2.setAlias("juangar");
        dto2.setFechaNacimiento(fecha);
        dto2.setFlagVIP(true);
        
        // Act & Assert
        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }
    
    @Test
    void testEquals_DifferentNombre() {
        // Arrange
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setNombre("Juan");
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        dto2.setNombre("Pedro");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_DifferentApellidos() {
        // Arrange
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setApellidos("García");
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        dto2.setApellidos("López");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_DifferentAlias() {
        // Arrange
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setAlias("alias1");
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        dto2.setAlias("alias2");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_DifferentFechaNacimiento() {
        // Arrange
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setFechaNacimiento(new Date(1000000));
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        dto2.setFechaNacimiento(new Date(2000000));
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_DifferentFlagVIP() {
        // Arrange
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setFlagVIP(true);
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        dto2.setFlagVIP(false);
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_NullObject() {
        // Arrange
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("Juan");
        
        // Act & Assert
        assertNotEquals(null, dto);
    }
    
    @Test
    void testEquals_DifferentClass() {
        // Arrange
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("Juan");
        String otherObject = "Not a DTO";
        
        // Act & Assert
        assertNotEquals(dto, otherObject);
    }
    
    @Test
    void testEquals_WithNullFields() {
        // Arrange
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        
        // Act & Assert
        assertEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_OneNullField() {
        // Arrange
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setNombre("Juan");
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testHashCode_EqualObjects() {
        // Arrange
        Date fecha = new Date();
        
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setNombre("Juan");
        dto1.setApellidos("García");
        dto1.setAlias("juangar");
        dto1.setFechaNacimiento(fecha);
        dto1.setFlagVIP(true);
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        dto2.setNombre("Juan");
        dto2.setApellidos("García");
        dto2.setAlias("juangar");
        dto2.setFechaNacimiento(fecha);
        dto2.setFlagVIP(true);
        
        // Act & Assert
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void testHashCode_DifferentObjects() {
        // Arrange
        UserProfileUpdateDTO dto1 = new UserProfileUpdateDTO();
        dto1.setNombre("Juan");
        
        UserProfileUpdateDTO dto2 = new UserProfileUpdateDTO();
        dto2.setNombre("Pedro");
        
        // Act & Assert
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void testHashCode_NullFields() {
        // Arrange
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        
        // Act & Assert
        assertDoesNotThrow(() -> dto.hashCode());
    }
    
    @Test
    void testHashCode_Consistency() {
        // Arrange
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("Juan");
        dto.setApellidos("García");
        
        // Act
        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();
        
        // Assert
        assertEquals(hashCode1, hashCode2);
    }
    
    @Test
    void testToString_AllFields() {
        // Arrange
        Date fecha = new Date();
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("Juan");
        dto.setApellidos("García López");
        dto.setAlias("juangar");
        dto.setFechaNacimiento(fecha);
        dto.setFlagVIP(true);
        
        // Act
        String result = dto.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Juan"));
        assertTrue(result.contains("García López"));
        assertTrue(result.contains("juangar"));
        assertTrue(result.contains("true"));
    }
    
    @Test
    void testToString_NullFields() {
        // Arrange
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        
        // Act
        String result = dto.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("UserProfileUpdateDTO"));
    }
}