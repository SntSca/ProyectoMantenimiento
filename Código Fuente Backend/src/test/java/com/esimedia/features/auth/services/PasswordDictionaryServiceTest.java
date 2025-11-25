package com.esimedia.features.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esimedia.features.auth.repository.CommonPasswordRepository;

@ExtendWith(MockitoExtension.class)
class PasswordDictionaryServiceTest {

    @Mock
    private CommonPasswordRepository commonPasswordRepository;

    @InjectMocks
    @Spy
    private PasswordDictionaryService passwordDictionaryService;

    // ========== isPasswordInDictionary() - Cubre 4 branches + método ==========

    @Test
    void testIsPasswordInDictionary_FoundInDictionary() {
        // Cubre: password != null, !isEmpty(), found = true, return true
        when(commonPasswordRepository.existsByHash(anyString())).thenReturn(true);
        
        assertTrue(passwordDictionaryService.isPasswordInDictionary("password123"));
        verify(commonPasswordRepository).existsByHash(anyString());
    }

    @Test
    void testIsPasswordInDictionary_NotFound() {
        // Cubre: password != null, !isEmpty(), found = false, return false
        when(commonPasswordRepository.existsByHash(anyString())).thenReturn(false);
        
        assertFalse(passwordDictionaryService.isPasswordInDictionary("SecurePass123!"));
    }

    @Test
    void testIsPasswordInDictionary_NullPassword() {
        // Cubre: password == null, return false
        assertFalse(passwordDictionaryService.isPasswordInDictionary(null));
        verify(commonPasswordRepository, never()).existsByHash(anyString());
    }

    @Test
    void testIsPasswordInDictionary_EmptyPassword() {
        // Cubre: password.isEmpty(), return false
        assertFalse(passwordDictionaryService.isPasswordInDictionary(""));
        verify(commonPasswordRepository, never()).existsByHash(anyString());
    }

    @Test
    void testIsPasswordInDictionary_HashPasswordThrowsException() throws Exception {
        // Cubre: catch (NoSuchAlgorithmException) en línea 16 -> return false
        // Usamos spy para simular que hashPassword lanza NoSuchAlgorithmException
        doThrow(new NoSuchAlgorithmException("Algorithm not found"))
            .when(passwordDictionaryService).hashPassword(anyString());

        // Act - El método debe capturar la excepción y devolver false
        boolean result = passwordDictionaryService.isPasswordInDictionary("test");

        // Assert
        assertFalse(result); // Devuelve false en caso de error para no bloquear al usuario
        verify(commonPasswordRepository, never()).existsByHash(anyString()); // No llega a buscar en BD
    }

    // ========== hashPassword() - Cubre método completo ==========

    @Test
    void testHashPassword() throws Exception {
        // Cubre: hashPassword() completo
        String hash = passwordDictionaryService.hashPassword("test");
        
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 = 64 hex chars
    }

    // ========== bytesToHex() - Método privado cubierto por hashPassword ==========
    
    @Test
    void testBytesToHex_ViaHashPassword() throws Exception {
        // Cubre: bytesToHex con byte que necesita padding (hex.length() == 1)
        // y bytes normales (hex.length() == 2)
        String hash = passwordDictionaryService.hashPassword("a");
        
        assertEquals(64, hash.length());
        assertTrue(hash.matches("^[0-9a-f]{64}$"));
    }

    // ========== getDictionarySize() - Cubre método ==========

    @Test
    void testGetDictionarySize() {
        when(commonPasswordRepository.count()).thenReturn(10000L);
        
        assertEquals(10000L, passwordDictionaryService.getDictionarySize());
        verify(commonPasswordRepository).count();
    }

    // ========== isDictionaryLoaded() - Cubre 2 branches ==========

    @Test
    void testIsDictionaryLoaded_True() {
        // Cubre: count() > 0 = true
        when(commonPasswordRepository.count()).thenReturn(1L);
        
        assertTrue(passwordDictionaryService.isDictionaryLoaded());
    }

    @Test
    void testIsDictionaryLoaded_False() {
        // Cubre: count() > 0 = false
        when(commonPasswordRepository.count()).thenReturn(0L);
        
        assertFalse(passwordDictionaryService.isDictionaryLoaded());
    }
}