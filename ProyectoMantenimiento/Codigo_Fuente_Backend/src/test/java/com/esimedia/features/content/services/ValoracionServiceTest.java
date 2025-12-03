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

import com.esimedia.features.content.entity.*;
import com.esimedia.features.content.repository.*;

@ExtendWith(MockitoExtension.class)
public class ValoracionServiceTest {

    @Mock private ValoracionContenidoRepository valoracionRepository;
    @Mock private ContenidosAudioRepository audioRepository;
    @Mock private ContenidosVideoRepository videoRepository;

    @InjectMocks
    private ValoracionService valoracionService;

    private static final String CONTENT_ID = "content123";
    private static final String USER_ID = "user456";

    private ContenidosAudio audio;
    private ContenidosVideo video;
    private ValoracionContenido valoracion;

    @BeforeEach
    void setUp() {
        audio = ContenidosAudio.builder()
            .id(CONTENT_ID)
            .titulo("Test Audio")
            .valoracionMedia(0.0)
            .build();

        video = ContenidosVideo.builder()
            .id(CONTENT_ID)
            .titulo("Test Video")
            .valoracionMedia(0.0)
            .build();

        valoracion = new ValoracionContenido(CONTENT_ID, USER_ID, 5.0);
    }

    // ========== valorarContenido - Cubre validación y branches ==========

    @Test
    void testValorarContenido_InvalidValoracionLow() {
        assertThrows(ResponseStatusException.class, () ->
            valoracionService.valorarContenido(CONTENT_ID, USER_ID, 0.0));
    }

    @Test
    void testValorarContenido_InvalidValoracionHigh() {
        assertThrows(ResponseStatusException.class, () ->
            valoracionService.valorarContenido(CONTENT_ID, USER_ID, 6.0));
    }

    @Test
    void testValorarContenido_ContentNotFound() {
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());
        when(videoRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            valoracionService.valorarContenido(CONTENT_ID, USER_ID, 5.0));
    }

    @Test
    void testValorarContenido_NewValoracion_Audio() {
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.of(audio));
        when(valoracionRepository.findByIdContenidoAndIdUsuario(CONTENT_ID, USER_ID)).thenReturn(null);
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Arrays.asList(valoracion));

        valoracionService.valorarContenido(CONTENT_ID, USER_ID, 5.0);

        verify(valoracionRepository, atLeastOnce()).save(any(ValoracionContenido.class));
        verify(audioRepository).save(any(ContenidosAudio.class));
    }

    @Test
    void testValorarContenido_NewValoracion_Video() {
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());
        when(videoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(valoracionRepository.findByIdContenidoAndIdUsuario(CONTENT_ID, USER_ID)).thenReturn(null);
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Arrays.asList(valoracion));

        valoracionService.valorarContenido(CONTENT_ID, USER_ID, 4.0);

        verify(valoracionRepository, atLeastOnce()).save(any(ValoracionContenido.class));
        verify(videoRepository).save(any(ContenidosVideo.class));
    }

    @Test
    void testValorarContenido_UpdateExisting() {
        ValoracionContenido existing = new ValoracionContenido(CONTENT_ID, USER_ID, 3.0);
        
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.of(audio));
        when(valoracionRepository.findByIdContenidoAndIdUsuario(CONTENT_ID, USER_ID)).thenReturn(existing);
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Arrays.asList(existing));

        valoracionService.valorarContenido(CONTENT_ID, USER_ID, 5.0);

        verify(valoracionRepository, atLeastOnce()).save(any(ValoracionContenido.class));
        assertEquals(5, existing.getValoracion());
    }

    // ========== actualizarValoracionMedia - Cubre branches audio/video ==========

    @Test
    void testActualizarValoracionMedia_Audio() {
        ValoracionContenido val1 = new ValoracionContenido(CONTENT_ID, "user1", 5.0);
        ValoracionContenido val2 = new ValoracionContenido(CONTENT_ID, "user2", 3.0);
        
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.of(audio));
        when(valoracionRepository.findByIdContenidoAndIdUsuario(CONTENT_ID, USER_ID)).thenReturn(null);
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Arrays.asList(val1, val2));

        valoracionService.valorarContenido(CONTENT_ID, USER_ID, 4.0);

        verify(audioRepository).save(argThat(a -> a.getValoracionMedia() == 4.0));
    }

    @Test
    void testActualizarValoracionMedia_Video() {
        ValoracionContenido val1 = new ValoracionContenido(CONTENT_ID, "user1", 4.0);
        
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());
        when(videoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(valoracionRepository.findByIdContenidoAndIdUsuario(CONTENT_ID, USER_ID)).thenReturn(null);
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Arrays.asList(val1));

        valoracionService.valorarContenido(CONTENT_ID, USER_ID, 5.0);

        verify(videoRepository).save(argThat(v -> v.getValoracionMedia() > 0.0));
    }

    @Test
    void testActualizarValoracionMedia_NoValoraciones() {
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.of(audio));
        when(valoracionRepository.findByIdContenidoAndIdUsuario(CONTENT_ID, USER_ID)).thenReturn(null);
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Collections.emptyList());

        valoracionService.valorarContenido(CONTENT_ID, USER_ID, 5.0);

        verify(audioRepository).save(argThat(a -> a.getValoracionMedia() == 0.0));
    }

    @Test
    void testActualizarValoracionMedia_ContentNotFoundInUpdate() {
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.of(audio));
        when(valoracionRepository.findByIdContenidoAndIdUsuario(CONTENT_ID, USER_ID)).thenReturn(null);
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Arrays.asList(valoracion));
        
        // En actualizarValoracionMedia, tanto audio como video retornan empty
        when(audioRepository.findById(CONTENT_ID))
            .thenReturn(Optional.of(audio))
            .thenReturn(Optional.empty());
        when(videoRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());

        valoracionService.valorarContenido(CONTENT_ID, USER_ID, 5.0);

        // Debe ejecutarse sin lanzar excepción
        verify(valoracionRepository, atLeastOnce()).save(any());
    }

    // ========== getValoracionMedia - Cubre branches ==========

    @Test
    void testGetValoracionMedia_Audio() {
        audio.setValoracionMedia(4.5);
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.of(audio));

        double result = valoracionService.getValoracionMedia(CONTENT_ID);

        assertEquals(4.5, result);
    }

    @Test
    void testGetValoracionMedia_Video() {
        video.setValoracionMedia(3.8);
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());
        when(videoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));

        double result = valoracionService.getValoracionMedia(CONTENT_ID);

        assertEquals(3.8, result);
    }

    @Test
    void testGetValoracionMedia_NotFound() {
        when(audioRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());
        when(videoRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());

        double result = valoracionService.getValoracionMedia(CONTENT_ID);

        assertEquals(0.0, result);
    }

    // ========== eliminarValoracionesDeUsuario ==========

    @Test
    void testEliminarValoracionesDeUsuario_Audio() {
        ValoracionContenido val1 = new ValoracionContenido("content1", USER_ID, 5.0);
        ValoracionContenido val2 = new ValoracionContenido("content2", USER_ID, 4.0);
        
        when(valoracionRepository.findByIdUsuario(USER_ID)).thenReturn(Arrays.asList(val1, val2));
        when(valoracionRepository.findByIdContenido("content1")).thenReturn(Collections.emptyList());
        when(valoracionRepository.findByIdContenido("content2")).thenReturn(Collections.emptyList());
        when(audioRepository.findById("content1")).thenReturn(Optional.of(audio));
        when(audioRepository.findById("content2")).thenReturn(Optional.of(audio));

        valoracionService.eliminarValoracionesDeUsuario(USER_ID);

        verify(valoracionRepository).deleteByIdUsuario(USER_ID);
        verify(audioRepository, times(2)).save(any());
    }

    @Test
    void testEliminarValoracionesDeUsuario_Video() {
        ValoracionContenido val = new ValoracionContenido("content1", USER_ID, 5.0);
        
        when(valoracionRepository.findByIdUsuario(USER_ID)).thenReturn(Arrays.asList(val));
        when(valoracionRepository.findByIdContenido("content1")).thenReturn(Collections.emptyList());
        when(audioRepository.findById("content1")).thenReturn(Optional.empty());
        when(videoRepository.findById("content1")).thenReturn(Optional.of(video));

        valoracionService.eliminarValoracionesDeUsuario(USER_ID);

        verify(valoracionRepository).deleteByIdUsuario(USER_ID);
        verify(videoRepository).save(any());
    }

    @Test
    void testEliminarValoracionesDeUsuario_Empty() {
        when(valoracionRepository.findByIdUsuario(USER_ID)).thenReturn(Collections.emptyList());

        valoracionService.eliminarValoracionesDeUsuario(USER_ID);

        verify(valoracionRepository).deleteByIdUsuario(USER_ID);
        verify(audioRepository, never()).save(any());
        verify(videoRepository, never()).save(any());
    }
}