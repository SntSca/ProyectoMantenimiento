package com.esimedia.features.favoritos.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.esimedia.features.favoritos.entity.ContenidoFavorito;
import com.esimedia.features.favoritos.repository.ContenidoFavoritoRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.favoritos.dto.FavoritoDTO;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private ContenidoFavoritoRepository favoritoRepository;
    @Mock
    private ContenidosVideoRepository videoRepository;
    @Mock
    private ContenidosAudioRepository audioRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    private FavoritoDTO favoritoDTO;
    private ContenidoFavorito favorito;

    @BeforeEach
    void setUp() {
        favoritoDTO = new FavoritoDTO();
        favoritoDTO.setIdContenido("content123");

        favorito = ContenidoFavorito.builder()
            .idUsuario("user123")
            .idContenido("content123")
            .build();
    }

    // =====================================================
    // =============== AGREGAR FAVORITO ====================
    // =====================================================

    @Test
    void testAgregarFavorito_Success() {
        when(videoRepository.existsById("content123")).thenReturn(true);
        when(favoritoRepository.existsByIdUsuarioAndIdContenido("user123", "content123"))
            .thenReturn(false);

        String result = favoritoService.agregarFavorito("user123", favoritoDTO);

        assertEquals("SUCCESS: Contenido agregado a favoritos", result);
        verify(favoritoRepository).save(any(ContenidoFavorito.class));
    }

    @Test
    void testAgregarFavorito_ContenidoNoExiste() {
        when(videoRepository.existsById("content123")).thenReturn(false);
        when(audioRepository.existsById("content123")).thenReturn(false);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> favoritoService.agregarFavorito("user123", favoritoDTO)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void testAgregarFavorito_YaEsFavorito() {
        when(videoRepository.existsById("content123")).thenReturn(true);
        when(favoritoRepository.existsByIdUsuarioAndIdContenido("user123", "content123"))
            .thenReturn(true);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> favoritoService.agregarFavorito("user123", favoritoDTO)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void testAgregarFavorito_UnexpectedException() {
        when(videoRepository.existsById("content123")).thenThrow(new RuntimeException("DB down"));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> favoritoService.agregarFavorito("user123", favoritoDTO)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }

    // =====================================================
    // =============== ELIMINAR FAVORITO ===================
    // =====================================================

    @Test
    void testEliminarFavorito_Success() {
        when(favoritoRepository.findByIdUsuarioAndIdContenido("user123", "content123"))
            .thenReturn(Optional.of(favorito));

        String result = favoritoService.eliminarFavorito("user123", favoritoDTO);

        assertEquals("SUCCESS: Favorito eliminado correctamente", result);
        verify(favoritoRepository).delete(favorito);
    }

    @Test
    void testEliminarFavorito_NoExiste() {
        when(favoritoRepository.findByIdUsuarioAndIdContenido("user123", "content123"))
            .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> favoritoService.eliminarFavorito("user123", favoritoDTO)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(favoritoRepository, never()).delete(any());
    }

    @Test
    void testEliminarFavorito_UnexpectedException() {
        when(favoritoRepository.findByIdUsuarioAndIdContenido("user123", "content123"))
            .thenThrow(new RuntimeException("DB error"));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> favoritoService.eliminarFavorito("user123", favoritoDTO)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }

    // =====================================================
    // ========== OBTENER FAVORITOS POR USUARIO ============
    // =====================================================

    @Test
    void testObtenerFavoritosPorUsuario_Success() {
        List<ContenidoFavorito> lista = new ArrayList<>();
        lista.add(favorito);

        when(favoritoRepository.findByIdUsuario("user123")).thenReturn(lista);

        List<String> result = favoritoService.obtenerFavoritosPorUsuario("user123");

        assertEquals(1, result.size());
        assertEquals("content123", result.get(0));
    }

    @Test
    void testObtenerFavoritosPorUsuario_Empty() {
        when(favoritoRepository.findByIdUsuario("user123")).thenReturn(new ArrayList<>());

        List<String> result = favoritoService.obtenerFavoritosPorUsuario("user123");

        assertTrue(result.isEmpty());
    }
}