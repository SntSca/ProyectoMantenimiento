package com.esimedia.features.content.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.entity.ContenidoAudioTag;
import com.esimedia.features.content.entity.Tags;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.ContenidoAudioTagRepository;
import com.esimedia.features.content.repository.TagsRepository;
import com.esimedia.features.content.repository.ValoracionContenidoRepository;
import com.esimedia.shared.util.ContentUtil;
import com.esimedia.shared.util.JwtValidationUtil;
import com.esimedia.shared.util.ContentTagProcessor;

import com.esimedia.features.auth.services.ValidationService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AudioContentService Tests")
class AudioContentServiceTest {

    @Mock
    private JwtValidationUtil jwtValidationService;

    @Mock
    private AudioContentUploadService uploadService;

    @Mock
    private AudioContentRetrievalService retrievalService;

    @Mock
    private AudioContentManagementService managementService;

    @InjectMocks
    private AudioContentService audioContentService;

    private ContentAudioUploadDTO audioDTO;
    private ContenidosAudio contenidosAudio;
    private String authHeader;
    private String userId;
    private String audioId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authHeader = "Bearer test-token";
        userId = "user123";
        audioId = "audio123";

        // Configurar DTO de prueba
        audioDTO = new ContentAudioUploadDTO();
        audioDTO.setTitulo("Test Audio");
        audioDTO.setDescripcion("Descripción de prueba");
        audioDTO.setDuracion(225);
        audioDTO.setFichero("data:audio/wav;base64,SUQzBAA=");
        audioDTO.setFicheroExtension("audio/wav");
        audioDTO.setMiniatura("data:image/png;base64,iVBORw0KGgo=");
        audioDTO.setFormatoMiniatura("image/png");
        audioDTO.setEsVIP(true);
        audioDTO.setFechaExpiracion("2025-12-31");
        audioDTO.setTags(Arrays.asList("tag1", "tag2"));

        // Configurar entidad de prueba - sin usar Builder
        contenidosAudio = mock(ContenidosAudio.class);
        when(contenidosAudio.getId()).thenReturn(audioId);
        when(contenidosAudio.getTitulo()).thenReturn("Test Audio");
        when(contenidosAudio.getDescripcion()).thenReturn("Descripción de prueba");
        when(contenidosAudio.getDuracion()).thenReturn(225);
        when(contenidosAudio.getIdCreador()).thenReturn(userId);
        when(contenidosAudio.getFichero()).thenReturn("test-audio-bytes".getBytes());
        when(contenidosAudio.getFicheroExtension()).thenReturn("audio/wav");
        when(contenidosAudio.isEsVIP()).thenReturn(true);
        when(contenidosAudio.getMiniatura()).thenReturn("test-image-bytes".getBytes());
        when(contenidosAudio.getFormatoMiniatura()).thenReturn("image/png");
        when(contenidosAudio.getFechaDisponibleHasta()).thenReturn(new Date());
        when(contenidosAudio.getFechaSubida()).thenReturn(new Date());
    }

    // ==================== uploadAudioContent ====================

    @Test
    @DisplayName("uploadAudioContent - Validación fallida")
    void testUploadAudioContentValidationFails() {
        when(jwtValidationService.validateContentUpload(authHeader, TipoContenido.AUDIO))
            .thenReturn(userId);
        when(uploadService.uploadAudioContent(userId, audioDTO))
            .thenReturn("Error de validación");

        String result = audioContentService.uploadAudioContent(authHeader, audioDTO);

        assertEquals("Error de validación", result);
        verify(jwtValidationService).validateContentUpload(authHeader, TipoContenido.AUDIO);
        verify(uploadService).uploadAudioContent(userId, audioDTO);
    }

    @Test
    @DisplayName("uploadAudioContent - Excepción en JWT")
    void testUploadAudioContentJwtValidationError() {
        when(jwtValidationService.validateContentUpload(authHeader, TipoContenido.AUDIO))
            .thenThrow(new RuntimeException("Token inválido"));

        String result = audioContentService.uploadAudioContent(authHeader, audioDTO);

        assertEquals("Error interno del servidor", result);
    }

    // ==================== getAllAudiosAsDTO ====================

    @Test
    @DisplayName("getAllAudiosAsDTO - Éxito con múltiples audios")
    void testGetAllAudiosAsDTOSuccess() {
        ContentAudioUploadDTO expectedDTO = new ContentAudioUploadDTO();
        expectedDTO.setTitulo("Test Audio");
        expectedDTO.setTags(Arrays.asList("tag1", "tag2"));
        List<ContentAudioUploadDTO> expected = Arrays.asList(expectedDTO);

        when(retrievalService.getAllAudiosAsDTO(authHeader)).thenReturn(expected);

        List<ContentAudioUploadDTO> result = audioContentService.getAllAudiosAsDTO(authHeader);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Audio", result.get(0).getTitulo());
        assertEquals(2, result.get(0).getTags().size());
    }

    @Test
    @DisplayName("getAllAudiosAsDTO - Lista vacía")
    void testGetAllAudiosAsDTOEmpty() {
        when(retrievalService.getAllAudiosAsDTO(authHeader)).thenReturn(Collections.emptyList());

        List<ContentAudioUploadDTO> result = audioContentService.getAllAudiosAsDTO(authHeader);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("getAllAudiosAsDTO - Sin fecha de expiración")
    void testGetAllAudiosAsDTONoExpirationDate() {
        ContentAudioUploadDTO dto = new ContentAudioUploadDTO();
        dto.setFechaExpiracion(null);
        List<ContentAudioUploadDTO> expected = Arrays.asList(dto);

        when(retrievalService.getAllAudiosAsDTO(authHeader)).thenReturn(expected);

        List<ContentAudioUploadDTO> result = audioContentService.getAllAudiosAsDTO(authHeader);

        assertEquals(1, result.size());
        assertNull(result.get(0).getFechaExpiracion());
    }

    // ==================== getAudioByIdAsDTO ====================

    @Test
    @DisplayName("getAudioByIdAsDTO - Éxito")
    void testGetAudioByIdAsDTOSuccess() {
        ContentAudioUploadDTO expected = new ContentAudioUploadDTO();
        expected.setTitulo("Test Audio");
        expected.setId(audioId);

        when(retrievalService.getAudioByIdAsDTO(authHeader, audioId)).thenReturn(expected);

        ContentAudioUploadDTO result = audioContentService.getAudioByIdAsDTO(authHeader, audioId);

        assertNotNull(result);
        assertEquals("Test Audio", result.getTitulo());
        assertEquals(audioId, result.getId());
    }

    @Test
    @DisplayName("getAudioByIdAsDTO - Audio no encontrado")
    void testGetAudioByIdAsDTONotFound() {
        when(retrievalService.getAudioByIdAsDTO(authHeader, audioId)).thenReturn(null);

        ContentAudioUploadDTO result = audioContentService.getAudioByIdAsDTO(authHeader, audioId);

        assertNull(result);
    }

    @Test
    @DisplayName("getAudioByIdAsDTO - Sin tags")
    void testGetAudioByIdAsDTONoTags() {
        ContentAudioUploadDTO expected = new ContentAudioUploadDTO();
        expected.setTags(Collections.emptyList());

        when(retrievalService.getAudioByIdAsDTO(authHeader, audioId)).thenReturn(expected);

        ContentAudioUploadDTO result = audioContentService.getAudioByIdAsDTO(authHeader, audioId);

        assertNotNull(result);
        assertEquals(0, result.getTags().size());
    }

    // ==================== getAudioById ====================

    @Test
    @DisplayName("getAudioById - Encontrado")
    void testGetAudioByIdFound() {
        when(retrievalService.getAudioById(audioId)).thenReturn(contenidosAudio);

        ContenidosAudio result = audioContentService.getAudioById(audioId);

        assertNotNull(result);
        assertEquals(audioId, result.getId());
    }

    @Test
    @DisplayName("getAudioById - No encontrado")
    void testGetAudioByIdNotFound() {
        when(retrievalService.getAudioById(audioId)).thenReturn(null);

        ContenidosAudio result = audioContentService.getAudioById(audioId);

        assertNull(result);
    }
}