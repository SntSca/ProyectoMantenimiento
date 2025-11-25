package com.esimedia.features.auth.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseDTOTest {
    
    @Test
    void testNoArgsConstructor() {
        // Act
        LoginResponseDTO dto = new LoginResponseDTO();
        
        // Assert
        assertNotNull(dto);
        assertNull(dto.getToken());
        assertNull(dto.getEmail());
        assertNull(dto.getAlias());
        assertNull(dto.getRol());
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String email = "user@example.com";
        String alias = "usuario1";
        String rol = "ADMIN";
        
        // Act
        LoginResponseDTO dto = new LoginResponseDTO(token, email, alias, rol);
        
        // Assert
        assertNotNull(dto);
        assertEquals(token, dto.getToken());
        assertEquals(email, dto.getEmail());
        assertEquals(alias, dto.getAlias());
        assertEquals(rol, dto.getRol());
    }
    
    @Test
    void testGettersAndSetters() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO();
        String token = "token123";
        String email = "test@test.com";
        String alias = "testalias";
        String rol = "USER";
        
        // Act
        dto.setToken(token);
        dto.setEmail(email);
        dto.setAlias(alias);
        dto.setRol(rol);
        
        // Assert
        assertEquals(token, dto.getToken());
        assertEquals(email, dto.getEmail());
        assertEquals(alias, dto.getAlias());
        assertEquals(rol, dto.getRol());
    }
    
    @Test
    void testEquals_SameObject() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO("token", "email@test.com", "alias", "USER");
        
        // Act & Assert
        assertEquals(dto, dto);
    }
    
    @Test
    void testEquals_EqualObjects() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token123", "user@test.com", "myalias", "ADMIN");
        LoginResponseDTO dto2 = new LoginResponseDTO("token123", "user@test.com", "myalias", "ADMIN");
        
        // Act & Assert
        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);
    }
    
    @Test
    void testEquals_DifferentToken() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token1", "email@test.com", "alias", "USER");
        LoginResponseDTO dto2 = new LoginResponseDTO("token2", "email@test.com", "alias", "USER");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_DifferentEmail() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token", "email1@test.com", "alias", "USER");
        LoginResponseDTO dto2 = new LoginResponseDTO("token", "email2@test.com", "alias", "USER");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_DifferentAlias() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token", "email@test.com", "alias1", "USER");
        LoginResponseDTO dto2 = new LoginResponseDTO("token", "email@test.com", "alias2", "USER");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_DifferentRol() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token", "email@test.com", "alias", "USER");
        LoginResponseDTO dto2 = new LoginResponseDTO("token", "email@test.com", "alias", "ADMIN");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_NullObject() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO("token", "email@test.com", "alias", "USER");
        
        // Act & Assert
        assertNotEquals(null, dto);
    }
    
    @Test
    void testEquals_DifferentClass() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO("token", "email@test.com", "alias", "USER");
        String otherObject = "Not a DTO";
        
        // Act & Assert
        assertNotEquals(dto, otherObject);
    }
    
    @Test
    void testEquals_WithNullFields() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO();
        LoginResponseDTO dto2 = new LoginResponseDTO();
        
        // Act & Assert
        assertEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_OneNullField() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token", null, null, null);
        LoginResponseDTO dto2 = new LoginResponseDTO("token", "email@test.com", null, null);
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testEquals_PartialNullFields() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token", "email@test.com", null, "USER");
        LoginResponseDTO dto2 = new LoginResponseDTO("token", "email@test.com", "alias", "USER");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testHashCode_EqualObjects() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token123", "user@test.com", "myalias", "ADMIN");
        LoginResponseDTO dto2 = new LoginResponseDTO("token123", "user@test.com", "myalias", "ADMIN");
        
        // Act & Assert
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void testHashCode_DifferentObjects() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token1", "user@test.com", "alias", "USER");
        LoginResponseDTO dto2 = new LoginResponseDTO("token2", "user@test.com", "alias", "USER");
        
        // Act & Assert
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void testHashCode_NullFields() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO();
        
        // Act & Assert
        assertNotNull(dto.hashCode());
    }
    
    @Test
    void testHashCode_Consistency() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO("token", "email@test.com", "alias", "USER");
        
        // Act
        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();
        
        // Assert
        assertEquals(hashCode1, hashCode2);
    }
    
    @Test
    void testHashCode_DifferentFieldCombinations() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token", "email1@test.com", "alias", "USER");
        LoginResponseDTO dto2 = new LoginResponseDTO("token", "email2@test.com", "alias", "USER");
        LoginResponseDTO dto3 = new LoginResponseDTO("token", "email1@test.com", "alias2", "USER");
        LoginResponseDTO dto4 = new LoginResponseDTO("token", "email1@test.com", "alias", "ADMIN");
        
        // Act & Assert
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        assertNotEquals(dto1.hashCode(), dto4.hashCode());
    }
    
    @Test
    void testToString_AllFields() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO("myToken123", "user@example.com", "useralias", "ADMIN");
        
        // Act
        String result = dto.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("myToken123"));
        assertTrue(result.contains("user@example.com"));
        assertTrue(result.contains("useralias"));
        assertTrue(result.contains("ADMIN"));
    }
    
    @Test
    void testToString_NullFields() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO();
        
        // Act
        String result = dto.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("LoginResponseDTO"));
    }
    
    @Test
    void testToString_PartialFields() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO("token", null, "alias", null);
        
        // Act
        String result = dto.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("token"));
        assertTrue(result.contains("alias"));
    }
    
    @Test
    void testCanEqual_SameClass() {
        // Arrange
        LoginResponseDTO dto1 = new LoginResponseDTO("token", "email@test.com", "alias", "USER");
        LoginResponseDTO dto2 = new LoginResponseDTO("token2", "email2@test.com", "alias2", "ADMIN");
        
        // Act & Assert
        assertEquals(dto1, dto1);
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testSetters_UpdateValues() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO("oldToken", "old@email.com", "oldAlias", "USER");
        
        // Act
        dto.setToken("newToken");
        dto.setEmail("new@email.com");
        dto.setAlias("newAlias");
        dto.setRol("ADMIN");
        
        // Assert
        assertEquals("newToken", dto.getToken());
        assertEquals("new@email.com", dto.getEmail());
        assertEquals("newAlias", dto.getAlias());
        assertEquals("ADMIN", dto.getRol());
    }
    
    @Test
    void testSetters_NullValues() {
        // Arrange
        LoginResponseDTO dto = new LoginResponseDTO("token", "email@test.com", "alias", "USER");
        
        // Act
        dto.setToken(null);
        dto.setEmail(null);
        dto.setAlias(null);
        dto.setRol(null);
        
        // Assert
        assertNull(dto.getToken());
        assertNull(dto.getEmail());
        assertNull(dto.getAlias());
        assertNull(dto.getRol());
    }
}