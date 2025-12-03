package com.esimedia.features.content.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.repository.*;
import com.esimedia.shared.security.ClamAVService;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class AudioContentUploadServiceTest {

    @Mock private ValidationService validationService;
    @Mock private ClamAVService clamavService;
    @Mock private ContenidosAudioRepository contenidoAudioRepository;
    @Mock private ContenidoAudioTagRepository contenidoAudioTagRepository;
    @Mock private TagsRepository tagsRepository;
    @Mock private CreadorContenidoRepository creadorContenidoRepository;

    @InjectMocks
    private AudioContentUploadService audioContentUploadService;

    private static final String USER_ID = "user123";

    private ContentAudioUploadDTO audioDTO;
    private CreadorContenido creador;
    private ContenidosAudio audio;

    @BeforeEach
    void setUp() {
        audioDTO = new ContentAudioUploadDTO();
        audioDTO.setTitulo("Test Audio");
        audioDTO.setDescripcion("Description");
        audioDTO.setDuracion(180);
        audioDTO.setFichero("base64audiodata");
        audioDTO.setFicheroExtension("mp3");
        audioDTO.setEsVIP(false);
        audioDTO.setTags(Arrays.asList("tag1"));

        creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setEspecialidad("Music");

        audio = ContenidosAudio.builder()
            .id("audio123")
            .titulo("Test")
            .build();
    }

    // ========== uploadAudioContent - Cubre branches principales ==========

    @Test
    void testUploadAudioContent_ValidationError() {
        when(validationService.validateAudioContent(audioDTO)).thenReturn("Error de validación");

        String result = audioContentUploadService.uploadAudioContent(USER_ID, audioDTO);

        assertEquals("Error de validación", result);
    }

    // ========== processAudioContent excepciones ==========


    @Test
    void testProcessAudioContent_CreadorNotFound() {
        lenient().when(validationService.validateAudioContent(audioDTO)).thenReturn(null);
        lenient().when(clamavService.scanFile(any(), anyString())).thenReturn(true);
        lenient().when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());

        String result = audioContentUploadService.uploadAudioContent(USER_ID, audioDTO);

        assertTrue(result.contains("Error interno"));
    }

    @Test
    void testGetRestriccionEdadFromValue_Invalid() {
        audioDTO.setRestriccionEdad(999);
        
        lenient().when(validationService.validateAudioContent(audioDTO)).thenReturn(null);
        lenient().when(clamavService.scanFile(any(), anyString())).thenReturn(true);
        lenient().when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));

        String result = audioContentUploadService.uploadAudioContent(USER_ID, audioDTO);

        assertTrue(result.contains("Error en los datos"));
    }

    @Test
    void testProcessAudioContent_GenericException() {
        lenient().when(validationService.validateAudioContent(audioDTO)).thenReturn(null);
        lenient().when(clamavService.scanFile(any(), anyString()))
                .thenThrow(new RuntimeException("Error"));

        String result = audioContentUploadService.uploadAudioContent(USER_ID, audioDTO);

        assertEquals("Error interno del servidor", result);
    }

}