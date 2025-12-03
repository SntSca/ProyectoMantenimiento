package com.esimedia.features.content.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.services.ContentAuthorizationService;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.dto.ContentUpdateDTO;
import com.esimedia.features.content.entity.*;
import com.esimedia.features.content.repository.*;
import com.esimedia.shared.util.JwtValidationUtil;

@ExtendWith(MockitoExtension.class)
public class AudioContentManagementServiceTest {

    @Mock private JwtValidationUtil jwtValidationService;
    @Mock private CreadorContenidoRepository creadorContenidoRepository;
    @Mock private ValidationService validationService;
    @Mock private ContenidosAudioRepository contenidoAudioRepository;
    @Mock private ContenidoAudioTagRepository contenidoAudioTagRepository;
    @Mock private TagsRepository tagsRepository;
    @Mock private ValoracionContenidoRepository valoracionRepository;
    @Mock private ContentAuthorizationService contentAuthorizationService;

    @InjectMocks
    private AudioContentManagementService audioContentManagementService;

    private static final String AUTH_HEADER = "Bearer token";
    private static final String USER_ID = "user123";
    private static final String AUDIO_ID = "audio456";

    private ContenidosAudio audio;
    private CreadorContenido creador;

    @BeforeEach
    void setUp() {
        audio = ContenidosAudio.builder()
            .id(AUDIO_ID)
            .titulo("Test Audio")
            .descripcion("Description")
            .duracion(180)
            .idCreador(USER_ID)
            .especialidad("Music")
            .fichero(new byte[]{1, 2, 3})
            .ficheroExtension("mp3")
            .esVIP(false)
            .visualizaciones(0)
            .fechaSubida(new Date())
            .visibilidad(true)
            .build();

        creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setEspecialidad("Music");
    }

    // ========== updateAudioContent - Cubre branches ==========

    @Test
    void testUpdateAudioContent_Success() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        updateDTO.setTitulo("New Title");
        updateDTO.setDescripcion("New Desc");
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));
        when(validationService.validateContentUpdate(updateDTO)).thenReturn(null);
        when(contenidoAudioRepository.save(any())).thenReturn(audio);

        String result = audioContentManagementService.updateAudioContent(AUTH_HEADER, AUDIO_ID, updateDTO);

        assertTrue(result.contains("SUCCESS"));
    }

    @Test
    void testUpdateAudioContent_ValidationError() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));
        when(validationService.validateContentUpdate(updateDTO)).thenReturn("Error");

        String result = audioContentManagementService.updateAudioContent(AUTH_HEADER, AUDIO_ID, updateDTO);

        assertEquals("Error", result);
    }

    @Test
    void testUpdateAudioContent_CreadorNotFound() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            audioContentManagementService.updateAudioContent(AUTH_HEADER, AUDIO_ID, updateDTO));
    }

    @Test
    void testUpdateAudioContent_AudioNotFound() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            audioContentManagementService.updateAudioContent(AUTH_HEADER, AUDIO_ID, updateDTO));
    }

    @Test
    void testUpdateAudioContent_Exception() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.AUDIO))
            .thenThrow(new RuntimeException("Error"));

        String result = audioContentManagementService.updateAudioContent(AUTH_HEADER, AUDIO_ID, updateDTO);

        assertEquals("Error interno del servidor", result);
    }

    // ========== getRestriccionEdadFromValue ==========

    @Test
    void testGetRestriccionEdadFromValue_Invalid() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        updateDTO.setRestriccionEdad(999);
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));
        when(validationService.validateContentUpdate(updateDTO)).thenReturn(null);

        String result = audioContentManagementService.updateAudioContent(AUTH_HEADER, AUDIO_ID, updateDTO);

        assertTrue(result.contains("Error interno"));
    }

    // ========== deleteAudioContent ==========

    @Test
    void testDeleteAudioContent_Success() {
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));
        when(valoracionRepository.findByIdContenido(AUDIO_ID)).thenReturn(Collections.emptyList());

        audioContentManagementService.deleteAudioContent(AUTH_HEADER, AUDIO_ID);

        verify(contenidoAudioTagRepository).deleteByIdContenido(AUDIO_ID);
        verify(contenidoAudioRepository).deleteById(AUDIO_ID);
    }

    @Test
    void testDeleteAudioContent_NotFound() {
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            audioContentManagementService.deleteAudioContent(AUTH_HEADER, AUDIO_ID));
    }

    @Test
    void testDeleteAudioContent_WithValoraciones() {
        ValoracionContenido val = new ValoracionContenido();
        
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));
        when(valoracionRepository.findByIdContenido(AUDIO_ID)).thenReturn(Arrays.asList(val));

        audioContentManagementService.deleteAudioContent(AUTH_HEADER, AUDIO_ID);

        verify(valoracionRepository).deleteAll(any());
    }

    @Test
    void testDeleteAudioContent_Exception() {
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenThrow(new RuntimeException("Error"));

        assertThrows(ResponseStatusException.class, () ->
            audioContentManagementService.deleteAudioContent(AUTH_HEADER, AUDIO_ID));
    }

    // ========== incrementarVisualizaciones ==========

    @Test
    void testIncrementarVisualizaciones_Success() {
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));
        when(contenidoAudioRepository.save(any())).thenReturn(audio);

        audioContentManagementService.incrementarVisualizaciones(AUDIO_ID);

        verify(contenidoAudioRepository).save(argThat(a -> a.getVisualizaciones() == 1));
    }

    @Test
    void testIncrementarVisualizaciones_NotFound() {
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            audioContentManagementService.incrementarVisualizaciones(AUDIO_ID));
    }

    @Test
    void testIncrementarVisualizaciones_Exception() {
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));
        when(contenidoAudioRepository.save(any())).thenThrow(new RuntimeException("Error"));

        assertThrows(ResponseStatusException.class, () ->
            audioContentManagementService.incrementarVisualizaciones(AUDIO_ID));
    }
}