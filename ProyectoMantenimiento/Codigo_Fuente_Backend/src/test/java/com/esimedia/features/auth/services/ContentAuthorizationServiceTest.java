package com.esimedia.features.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.entity.ContenidosVideo;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.shared.util.JwtValidationUtil;

@ExtendWith(MockitoExtension.class)
class ContentAuthorizationServiceTest {

    @Mock
    private JwtValidationUtil jwtValidationService;
    @Mock
    private CreadorContenidoRepository creadorContenidoRepository;
    @Mock
    private ContenidosAudioRepository audioRepository;
    @Mock
    private ContenidosVideoRepository videoRepository;

    @InjectMocks
    private ContentAuthorizationService contentAuthorizationService;

    private CreadorContenido creador;
    private ContenidosAudio audio;
    private ContenidosVideo video;
    private String authHeader;

    @BeforeEach
    void setUp() {
        creador = new CreadorContenido();
        creador.setIdUsuario("creator123");
        creador.setAlias("creatoruser");
        creador.setAliasCreador("CreatorAlias");
        creador.setValidado(true);
        creador.setBloqueado(false);

        audio = new ContenidosAudio();
        audio.setId("audio123");
        audio.setTitulo("Test Audio");

        video = new ContenidosVideo();
        video.setId("video123");
        video.setTitulo("Test Video");

        authHeader = "Bearer jwt-token-123";
    }

    // ===== VALIDATE CREATOR TESTS =====

    @Test
    void testValidateCreator_Success() {
        when(jwtValidationService.validateJwtWithBusinessRules(
            eq(authHeader), 
            eq(Arrays.asList(Rol.CREADOR)), 
            isNull()
        )).thenReturn("creator123");
        
        when(creadorContenidoRepository.findById("creator123")).thenReturn(Optional.of(creador));

        CreadorContenido result = contentAuthorizationService.validateCreator(authHeader);

        assertNotNull(result);
        assertEquals("creator123", result.getIdUsuario());
        assertTrue(result.isValidado());
        assertFalse(result.isBloqueado());
        verify(jwtValidationService).validateJwtWithBusinessRules(authHeader, Arrays.asList(Rol.CREADOR), null);
    }

    @Test
    void testValidateCreator_NotValidated() {
        creador.setValidado(false);
        
        when(jwtValidationService.validateJwtWithBusinessRules(
            eq(authHeader), 
            eq(Arrays.asList(Rol.CREADOR)), 
            isNull()
        )).thenReturn("creator123");
        
        when(creadorContenidoRepository.findById("creator123")).thenReturn(Optional.of(creador));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateCreator(authHeader)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("validado por un administrador"));
    }

    @Test
    void testValidateCreator_Blocked() {
        creador.setBloqueado(true);
        
        when(jwtValidationService.validateJwtWithBusinessRules(
            eq(authHeader), 
            eq(Arrays.asList(Rol.CREADOR)), 
            isNull()
        )).thenReturn("creator123");
        
        when(creadorContenidoRepository.findById("creator123")).thenReturn(Optional.of(creador));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateCreator(authHeader)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("bloqueado"));
    }

    @Test
    void testValidateCreator_NotFound() {
        when(jwtValidationService.validateJwtWithBusinessRules(
            eq(authHeader), 
            eq(Arrays.asList(Rol.CREADOR)), 
            isNull()
        )).thenReturn("unknownuser");
        
        when(creadorContenidoRepository.findById("unknownuser")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateCreator(authHeader)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Creador de contenido no encontrado"));
    }

    @Test
    void testValidateCreator_EmptyRepository() {
        when(jwtValidationService.validateJwtWithBusinessRules(
            eq(authHeader), 
            eq(Arrays.asList(Rol.CREADOR)), 
            isNull()
        )).thenReturn("creator123");
        
        when(creadorContenidoRepository.findById("creator123")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateCreator(authHeader)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // ===== VALIDATE CONTENT ACCESS TESTS =====

    @Test
    void testValidateContentAccess_Audio_Success() {
        when(jwtValidationService.validateContentAccess(authHeader, TipoContenido.AUDIO))
            .thenReturn("username");

        assertDoesNotThrow(() -> 
            contentAuthorizationService.validateContentAccess(authHeader, "audio123", TipoContenido.AUDIO)
        );

        verify(jwtValidationService).validateContentAccess(authHeader, TipoContenido.AUDIO);
    }

    @Test
    void testValidateContentAccess_Video_Success() {
        when(jwtValidationService.validateContentAccess(authHeader, TipoContenido.VIDEO))
            .thenReturn("username");

        assertDoesNotThrow(() -> 
            contentAuthorizationService.validateContentAccess(authHeader, "video123", TipoContenido.VIDEO)
        );

        verify(jwtValidationService).validateContentAccess(authHeader, TipoContenido.VIDEO);
    }

    @Test
    void testValidateContentAccess_Unauthorized() {
        when(jwtValidationService.validateContentAccess(authHeader, TipoContenido.AUDIO))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateContentAccess(authHeader, "audio123", TipoContenido.AUDIO)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    // ===== GET TIPO CONTENIDO BY ID TESTS =====

    @Test
    void testGetTipoContenidoById_Audio_Success() {
        when(audioRepository.findById("audio123")).thenReturn(Optional.of(audio));

        TipoContenido result = contentAuthorizationService.getTipoContenidoById("audio123");

        assertEquals(TipoContenido.AUDIO, result);
        verify(audioRepository).findById("audio123");
        verify(videoRepository, never()).findById(anyString());
    }

    @Test
    void testGetTipoContenidoById_Video_Success() {
        when(audioRepository.findById("video123")).thenReturn(Optional.empty());
        when(videoRepository.findById("video123")).thenReturn(Optional.of(video));

        TipoContenido result = contentAuthorizationService.getTipoContenidoById("video123");

        assertEquals(TipoContenido.VIDEO, result);
        verify(audioRepository).findById("video123");
        verify(videoRepository).findById("video123");
    }

    @Test
    void testGetTipoContenidoById_NotFound() {
        when(audioRepository.findById("unknown")).thenReturn(Optional.empty());
        when(videoRepository.findById("unknown")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.getTipoContenidoById("unknown")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Contenido no encontrado"));
    }

    // ===== VALIDATE CONTENT ACCESS BY ID TESTS =====

    @Test
    void testValidateContentAccessById_Audio_Success() {
        when(audioRepository.findById("audio123")).thenReturn(Optional.of(audio));
        when(jwtValidationService.validateContentAccess(authHeader, TipoContenido.AUDIO))
            .thenReturn("username");

        assertDoesNotThrow(() -> 
            contentAuthorizationService.validateContentAccessById(authHeader, "audio123")
        );

        verify(audioRepository).findById("audio123");
        verify(jwtValidationService).validateContentAccess(authHeader, TipoContenido.AUDIO);
    }

    @Test
    void testValidateContentAccessById_Video_Success() {
        when(audioRepository.findById("video123")).thenReturn(Optional.empty());
        when(videoRepository.findById("video123")).thenReturn(Optional.of(video));
        when(jwtValidationService.validateContentAccess(authHeader, TipoContenido.VIDEO))
            .thenReturn("username");

        assertDoesNotThrow(() -> 
            contentAuthorizationService.validateContentAccessById(authHeader, "video123")
        );

        verify(audioRepository).findById("video123");
        verify(videoRepository).findById("video123");
        verify(jwtValidationService).validateContentAccess(authHeader, TipoContenido.VIDEO);
    }

    @Test
    void testValidateContentAccessById_ContentNotFound() {
        when(audioRepository.findById("unknown")).thenReturn(Optional.empty());
        when(videoRepository.findById("unknown")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateContentAccessById(authHeader, "unknown")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Contenido no encontrado"));
    }

    @Test
    void testValidateContentAccessById_UnauthorizedAfterFindingContent() {
        when(audioRepository.findById("audio123")).thenReturn(Optional.of(audio));
        when(jwtValidationService.validateContentAccess(authHeader, TipoContenido.AUDIO))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateContentAccessById(authHeader, "audio123")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    // ===== EDGE CASES AND INTEGRATION TESTS =====

    @Test
    void testValidateCreator_MultipleCreatorsInRepo() {
        CreadorContenido otherCreador = new CreadorContenido();
        otherCreador.setIdUsuario("other123");
        otherCreador.setAlias("otheruser");
        otherCreador.setValidado(true);
        otherCreador.setBloqueado(false);

        when(jwtValidationService.validateJwtWithBusinessRules(
            eq(authHeader), 
            eq(Arrays.asList(Rol.CREADOR)), 
            isNull()
        )).thenReturn("creator123");
        
        when(creadorContenidoRepository.findById("creator123")).thenReturn(Optional.of(creador));

        CreadorContenido result = contentAuthorizationService.validateCreator(authHeader);

        assertNotNull(result);
        assertEquals("creator123", result.getIdUsuario());
        assertEquals("creatoruser", result.getAlias());
    }

    @Test
    void testValidateCreator_JwtValidationFails() {
        when(jwtValidationService.validateJwtWithBusinessRules(
            eq(authHeader), 
            eq(Arrays.asList(Rol.CREADOR)), 
            isNull()
        )).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT"));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateCreator(authHeader)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(creadorContenidoRepository, never()).findAll();
    }

    @Test
    void testGetTipoContenidoById_ChecksAudioFirst() {
        // Verificar que siempre busca primero en audio
        when(audioRepository.findById("content123")).thenReturn(Optional.of(audio));

        TipoContenido result = contentAuthorizationService.getTipoContenidoById("content123");

        assertEquals(TipoContenido.AUDIO, result);
        verify(audioRepository).findById("content123");
        // No debería buscar en video si lo encuentra en audio
        verify(videoRepository, never()).findById(anyString());
    }

    @Test
    void testValidateContentAccess_NullAuthHeader() {
        when(jwtValidationService.validateContentAccess(null, TipoContenido.AUDIO))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No auth header"));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> contentAuthorizationService.validateContentAccess(null, "audio123", TipoContenido.AUDIO)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}