package com.esimedia.features.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esimedia.features.auth.services.PasswordSecurityService.PasswordSecurityCheckResult;
import com.esimedia.features.auth.services.PasswordSecurityService.PasswordSecuritySystemStatus;

@ExtendWith(MockitoExtension.class)
class PasswordSecurityServiceTest {

    @Mock
    private PasswordDictionaryService passwordDictionaryService;

    @Mock
    private LeakLookupService leakLookupService;

    @InjectMocks
    private PasswordSecurityService passwordSecurityService;

    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final String COMMON_PASSWORD = "password123";
    private static final String LEAKED_PASSWORD = "123456";

    @BeforeEach
    void setUp() {
        // Configuración base para los mocks (se puede sobrescribir en cada test)
    }

    // ========== Tests para isPasswordInDictionary() ==========

    @Test
    void testIsPasswordInDictionary_WhenPasswordIsInDictionary() {
        // Arrange
        when(passwordDictionaryService.isPasswordInDictionary(COMMON_PASSWORD)).thenReturn(true);

        // Act
        boolean result = passwordSecurityService.isPasswordInDictionary(COMMON_PASSWORD);

        // Assert
        assertTrue(result);
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(COMMON_PASSWORD);
    }

    @Test
    void testIsPasswordInDictionary_WhenPasswordIsNotInDictionary() {
        // Arrange
        when(passwordDictionaryService.isPasswordInDictionary(TEST_PASSWORD)).thenReturn(false);

        // Act
        boolean result = passwordSecurityService.isPasswordInDictionary(TEST_PASSWORD);

        // Assert
        assertFalse(result);
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(TEST_PASSWORD);
    }

    // ========== Tests para isPasswordCompromised() ==========

    @Test
    void testIsPasswordCompromised_WhenPasswordIsCompromised() {
        // Arrange
        when(leakLookupService.isPasswordCompromised(LEAKED_PASSWORD)).thenReturn(true);

        // Act
        boolean result = passwordSecurityService.isPasswordCompromised(LEAKED_PASSWORD);

        // Assert
        assertTrue(result);
        verify(leakLookupService, times(1)).isPasswordCompromised(LEAKED_PASSWORD);
    }

    @Test
    void testIsPasswordCompromised_WhenPasswordIsNotCompromised() {
        // Arrange
        when(leakLookupService.isPasswordCompromised(TEST_PASSWORD)).thenReturn(false);

        // Act
        boolean result = passwordSecurityService.isPasswordCompromised(TEST_PASSWORD);

        // Assert
        assertFalse(result);
        verify(leakLookupService, times(1)).isPasswordCompromised(TEST_PASSWORD);
    }

    // ========== Tests para isPasswordLeaked() (alias) ==========

    @Test
    void testIsPasswordLeaked_WhenPasswordIsLeaked() {
        // Arrange
        when(leakLookupService.isPasswordCompromised(LEAKED_PASSWORD)).thenReturn(true);

        // Act
        boolean result = passwordSecurityService.isPasswordLeaked(LEAKED_PASSWORD);

        // Assert
        assertTrue(result);
        verify(leakLookupService, times(1)).isPasswordCompromised(LEAKED_PASSWORD);
    }

    @Test
    void testIsPasswordLeaked_WhenPasswordIsNotLeaked() {
        // Arrange
        when(leakLookupService.isPasswordCompromised(TEST_PASSWORD)).thenReturn(false);

        // Act
        boolean result = passwordSecurityService.isPasswordLeaked(TEST_PASSWORD);

        // Assert
        assertFalse(result);
        verify(leakLookupService, times(1)).isPasswordCompromised(TEST_PASSWORD);
    }

    // ========== Tests para performSecurityCheck() - Casos exitosos ==========

    @Test
    void testPerformSecurityCheck_WhenPasswordIsSecure() {
        // Arrange - Contraseña NO está en diccionario NI comprometida
        when(passwordDictionaryService.isPasswordInDictionary(TEST_PASSWORD)).thenReturn(false);
        when(leakLookupService.isPasswordCompromised(TEST_PASSWORD)).thenReturn(false);

        // Act
        PasswordSecurityCheckResult result = passwordSecurityService.performSecurityCheck(TEST_PASSWORD);

        // Assert
        assertTrue(result.isSecure());
        assertFalse(result.isInDictionary());
        assertFalse(result.isCompromised());
        assertEquals("La contraseña es segura.", result.getMessage());
        
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(TEST_PASSWORD);
        verify(leakLookupService, times(1)).isPasswordCompromised(TEST_PASSWORD);
    }

    @Test
    void testPerformSecurityCheck_WhenPasswordIsInDictionary() {
        // Arrange - Contraseña SÍ está en diccionario pero NO comprometida
        when(passwordDictionaryService.isPasswordInDictionary(COMMON_PASSWORD)).thenReturn(true);
        when(leakLookupService.isPasswordCompromised(COMMON_PASSWORD)).thenReturn(false);

        // Act
        PasswordSecurityCheckResult result = passwordSecurityService.performSecurityCheck(COMMON_PASSWORD);

        // Assert
        assertFalse(result.isSecure());
        assertTrue(result.isInDictionary());
        assertFalse(result.isCompromised());
        assertEquals("La contraseña es muy común. Por favor, elige una contraseña más fuerte.", result.getMessage());
        
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(COMMON_PASSWORD);
        verify(leakLookupService, times(1)).isPasswordCompromised(COMMON_PASSWORD);
    }

    @Test
    void testPerformSecurityCheck_WhenPasswordIsCompromised() {
        // Arrange - Contraseña NO está en diccionario pero SÍ comprometida
        when(passwordDictionaryService.isPasswordInDictionary(LEAKED_PASSWORD)).thenReturn(false);
        when(leakLookupService.isPasswordCompromised(LEAKED_PASSWORD)).thenReturn(true);

        // Act
        PasswordSecurityCheckResult result = passwordSecurityService.performSecurityCheck(LEAKED_PASSWORD);

        // Assert
        assertFalse(result.isSecure());
        assertFalse(result.isInDictionary());
        assertTrue(result.isCompromised());
        assertEquals("La contraseña ha sido comprometida en una brecha de datos. Elige una diferente.", result.getMessage());
        
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(LEAKED_PASSWORD);
        verify(leakLookupService, times(1)).isPasswordCompromised(LEAKED_PASSWORD);
    }

    @Test
    void testPerformSecurityCheck_WhenPasswordIsInDictionaryAndCompromised() {
        // Arrange - Contraseña SÍ está en diccionario Y comprometida (peor caso)
        String worstPassword = "123456";
        when(passwordDictionaryService.isPasswordInDictionary(worstPassword)).thenReturn(true);
        when(leakLookupService.isPasswordCompromised(worstPassword)).thenReturn(true);

        // Act
        PasswordSecurityCheckResult result = passwordSecurityService.performSecurityCheck(worstPassword);

        // Assert
        assertFalse(result.isSecure());
        assertTrue(result.isInDictionary());
        assertTrue(result.isCompromised());
        assertEquals("La contraseña es muy común y ha sido comprometida en brechas de datos.", result.getMessage());
        
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(worstPassword);
        verify(leakLookupService, times(1)).isPasswordCompromised(worstPassword);
    }

    // ========== Tests para performSecurityCheck() - Casos con excepciones ==========

    @Test
    void testPerformSecurityCheck_WhenDictionaryServiceThrowsException() {
        // Arrange - El servicio de diccionario lanza excepción
        when(passwordDictionaryService.isPasswordInDictionary(TEST_PASSWORD))
            .thenThrow(new RuntimeException("Error en diccionario"));
        when(leakLookupService.isPasswordCompromised(TEST_PASSWORD)).thenReturn(false);

        // Act
        PasswordSecurityCheckResult result = passwordSecurityService.performSecurityCheck(TEST_PASSWORD);

        // Assert - El check continúa aunque falle el diccionario
        assertTrue(result.isSecure()); // Solo se verifica leak lookup
        assertFalse(result.isInDictionary()); // inDictionary queda en false por el catch
        assertFalse(result.isCompromised());
        assertEquals("La contraseña es segura.", result.getMessage());
        
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(TEST_PASSWORD);
        verify(leakLookupService, times(1)).isPasswordCompromised(TEST_PASSWORD);
    }

    @Test
    void testPerformSecurityCheck_WhenLeakLookupServiceThrowsException() {
        // Arrange - El servicio de leak lookup lanza excepción
        when(passwordDictionaryService.isPasswordInDictionary(TEST_PASSWORD)).thenReturn(false);
        when(leakLookupService.isPasswordCompromised(TEST_PASSWORD))
            .thenThrow(new RuntimeException("Error en leak lookup"));

        // Act
        PasswordSecurityCheckResult result = passwordSecurityService.performSecurityCheck(TEST_PASSWORD);

        // Assert - El check continúa aunque falle leak lookup
        assertTrue(result.isSecure()); // Solo se verifica diccionario
        assertFalse(result.isInDictionary());
        assertFalse(result.isCompromised()); // isCompromised queda en false por el catch
        assertEquals("La contraseña es segura.", result.getMessage());
        
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(TEST_PASSWORD);
        verify(leakLookupService, times(1)).isPasswordCompromised(TEST_PASSWORD);
    }

    @Test
    void testPerformSecurityCheck_WhenBothServicesThrowException() {
        // Arrange - Ambos servicios lanzan excepción
        when(passwordDictionaryService.isPasswordInDictionary(TEST_PASSWORD))
            .thenThrow(new RuntimeException("Error en diccionario"));
        when(leakLookupService.isPasswordCompromised(TEST_PASSWORD))
            .thenThrow(new RuntimeException("Error en leak lookup"));

        // Act
        PasswordSecurityCheckResult result = passwordSecurityService.performSecurityCheck(TEST_PASSWORD);

        // Assert - Ambos fallan, pero el resultado es "seguro" por defecto
        assertTrue(result.isSecure());
        assertFalse(result.isInDictionary());
        assertFalse(result.isCompromised());
        assertEquals("La contraseña es segura.", result.getMessage());
        
        verify(passwordDictionaryService, times(1)).isPasswordInDictionary(TEST_PASSWORD);
        verify(leakLookupService, times(1)).isPasswordCompromised(TEST_PASSWORD);
    }

    @Test
    void testPerformSecurityCheck_WhenDictionaryServiceThrowsException_ButPasswordIsLeaked() {
        // Arrange - Diccionario falla pero la contraseña está comprometida
        when(passwordDictionaryService.isPasswordInDictionary(LEAKED_PASSWORD))
            .thenThrow(new RuntimeException("Error en diccionario"));
        when(leakLookupService.isPasswordCompromised(LEAKED_PASSWORD)).thenReturn(true);

        // Act
        PasswordSecurityCheckResult result = passwordSecurityService.performSecurityCheck(LEAKED_PASSWORD);

        // Assert - Debería detectar que está comprometida aunque falle el diccionario
        assertFalse(result.isSecure());
        assertFalse(result.isInDictionary());
        assertTrue(result.isCompromised());
        assertEquals("La contraseña ha sido comprometida en una brecha de datos. Elige una diferente.", result.getMessage());
    }

    // ========== Tests para getSystemStatus() ==========

    @Test
    void testGetSystemStatus_WhenAllServicesAvailable() {
        // Arrange
        when(passwordDictionaryService.isDictionaryLoaded()).thenReturn(true);
        when(passwordDictionaryService.getDictionarySize()).thenReturn(10000L);

        // Act
        PasswordSecuritySystemStatus status = passwordSecurityService.getSystemStatus();

        // Assert
        assertTrue(status.isDictionaryLoaded());
        assertEquals(10000L, status.getDictionarySize());
        assertTrue(status.isLeakLookupAvailable()); // leakLookupService != null
        
        verify(passwordDictionaryService, times(1)).isDictionaryLoaded();
        verify(passwordDictionaryService, times(1)).getDictionarySize();
    }

    @Test
    void testGetSystemStatus_WhenDictionaryNotLoaded() {
        // Arrange
        when(passwordDictionaryService.isDictionaryLoaded()).thenReturn(false);
        when(passwordDictionaryService.getDictionarySize()).thenReturn(0L);

        // Act
        PasswordSecuritySystemStatus status = passwordSecurityService.getSystemStatus();

        // Assert
        assertFalse(status.isDictionaryLoaded());
        assertEquals(0L, status.getDictionarySize());
        assertTrue(status.isLeakLookupAvailable());
        
        verify(passwordDictionaryService, times(1)).isDictionaryLoaded();
        verify(passwordDictionaryService, times(1)).getDictionarySize();
    }

    @Test
    void testGetSystemStatus_WithDifferentDictionarySizes() {
        // Arrange
        when(passwordDictionaryService.isDictionaryLoaded()).thenReturn(true);
        when(passwordDictionaryService.getDictionarySize()).thenReturn(50000L);

        // Act
        PasswordSecuritySystemStatus status = passwordSecurityService.getSystemStatus();

        // Assert
        assertTrue(status.isDictionaryLoaded());
        assertEquals(50000L, status.getDictionarySize());
        assertTrue(status.isLeakLookupAvailable());
    }

    // ========== Tests para generateSecurityMessage() - Indirectamente a través de performSecurityCheck ==========

    @Test
    void testGenerateSecurityMessage_AllCombinations() {
        // Ya están cubiertos en los tests anteriores de performSecurityCheck:
        // - isSecure() cuando ambos son false
        // - inDictionary solo
        // - isCompromised solo
        // - ambos true
        
        // Este test es un recordatorio de que generateSecurityMessage() ya está cubierto
        // al 100% por los tests de performSecurityCheck()
        
        // Verificación adicional: Cada combinación produce un mensaje único
        when(passwordDictionaryService.isPasswordInDictionary(anyString())).thenReturn(false, true, false, true);
        when(leakLookupService.isPasswordCompromised(anyString())).thenReturn(false, false, true, true);
        
        PasswordSecurityCheckResult result1 = passwordSecurityService.performSecurityCheck("test1");
        PasswordSecurityCheckResult result2 = passwordSecurityService.performSecurityCheck("test2");
        PasswordSecurityCheckResult result3 = passwordSecurityService.performSecurityCheck("test3");
        PasswordSecurityCheckResult result4 = passwordSecurityService.performSecurityCheck("test4");
        
        // Verificar que cada mensaje es diferente
        assertNotEquals(result1.getMessage(), result2.getMessage());
        assertNotEquals(result1.getMessage(), result3.getMessage());
        assertNotEquals(result1.getMessage(), result4.getMessage());
        assertNotEquals(result2.getMessage(), result3.getMessage());
    }

    // ========== Tests para clases internas (PasswordSecurityCheckResult) ==========

    @Test
    void testPasswordSecurityCheckResult_Getters() {
        // Arrange & Act
        PasswordSecurityCheckResult result = new PasswordSecurityCheckResult(
            true, 
            false, 
            false, 
            "Test message"
        );

        // Assert
        assertTrue(result.isSecure());
        assertFalse(result.isInDictionary());
        assertFalse(result.isCompromised());
        assertEquals("Test message", result.getMessage());
    }

    @Test
    void testPasswordSecurityCheckResult_AllScenarios() {
        // Test con todos los valores en true
        PasswordSecurityCheckResult result1 = new PasswordSecurityCheckResult(true, true, true, "Msg1");
        assertTrue(result1.isSecure());
        assertTrue(result1.isInDictionary());
        assertTrue(result1.isCompromised());
        assertEquals("Msg1", result1.getMessage());

        // Test con todos los valores en false
        PasswordSecurityCheckResult result2 = new PasswordSecurityCheckResult(false, false, false, "Msg2");
        assertFalse(result2.isSecure());
        assertFalse(result2.isInDictionary());
        assertFalse(result2.isCompromised());
        assertEquals("Msg2", result2.getMessage());
    }

    // ========== Tests para clases internas (PasswordSecuritySystemStatus) ==========

    @Test
    void testPasswordSecuritySystemStatus_Getters() {
        // Arrange & Act
        PasswordSecuritySystemStatus status = new PasswordSecuritySystemStatus(true, 5000L, true);

        // Assert
        assertTrue(status.isDictionaryLoaded());
        assertEquals(5000L, status.getDictionarySize());
        assertTrue(status.isLeakLookupAvailable());
    }

    @Test
    void testPasswordSecuritySystemStatus_AllScenarios() {
        // Test con servicios disponibles
        PasswordSecuritySystemStatus status1 = new PasswordSecuritySystemStatus(true, 100000L, true);
        assertTrue(status1.isDictionaryLoaded());
        assertEquals(100000L, status1.getDictionarySize());
        assertTrue(status1.isLeakLookupAvailable());

        // Test con servicios no disponibles
        PasswordSecuritySystemStatus status2 = new PasswordSecuritySystemStatus(false, 0L, false);
        assertFalse(status2.isDictionaryLoaded());
        assertEquals(0L, status2.getDictionarySize());
        assertFalse(status2.isLeakLookupAvailable());
    }

    // ========== Test del constructor ==========

    @Test
    void testConstructor() {
        // Arrange
        PasswordDictionaryService mockDict = mock(PasswordDictionaryService.class);
        LeakLookupService mockLeak = mock(LeakLookupService.class);

        // Act
        PasswordSecurityService service = new PasswordSecurityService(mockDict, mockLeak);

        // Assert
        assertNotNull(service);
        // Verificar que el servicio funciona correctamente
        when(mockDict.isPasswordInDictionary("test")).thenReturn(true);
        assertTrue(service.isPasswordInDictionary("test"));
    }
}