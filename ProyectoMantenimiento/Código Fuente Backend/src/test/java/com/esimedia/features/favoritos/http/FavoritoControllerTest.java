package com.esimedia.features.favoritos.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.esimedia.features.favoritos.dto.FavoritoDTO;
import com.esimedia.features.favoritos.services.FavoritoService;
import com.esimedia.shared.util.JwtValidationUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FavoritoControllerTest {

    @Mock
    private FavoritoService favoritoService;

    @Mock
    private JwtValidationUtil jwtValidationService;

    @InjectMocks
    private FavoritoController favoritoController;

    private FavoritoDTO favoritoDTO;

    @BeforeEach
    void setUp() {
        favoritoDTO = new FavoritoDTO();
        favoritoDTO.setIdContenido("content123");
    }

    // ==========================================================
    // ================ AGREGAR FAVORITO =========================
    // ==========================================================

    @Test
    void testAgregarFavorito_Success() {
        when(jwtValidationService.validarGetUsuario("token123"))
            .thenReturn("user123");
        when(favoritoService.agregarFavorito("user123", favoritoDTO))
            .thenReturn("SUCCESS: Contenido agregado a favoritos");

        ResponseEntity<String> response =
                favoritoController.agregarFavorito("token123", favoritoDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SUCCESS: Contenido agregado a favoritos", response.getBody());
    }

    @Test
    void testAgregarFavorito_InternalError() {
        when(jwtValidationService.validarGetUsuario("token123"))
            .thenReturn("user123");
        when(favoritoService.agregarFavorito("user123", favoritoDTO))
            .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<String> response =
                favoritoController.agregarFavorito("token123", favoritoDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error interno del servidor", response.getBody());
    }

    // ==========================================================
    // ================ ELIMINAR FAVORITO ========================
    // ==========================================================

    @Test
    void testEliminarFavorito_Success() {
        when(jwtValidationService.validarGetUsuario("token123"))
            .thenReturn("user123");
        when(favoritoService.eliminarFavorito("user123", favoritoDTO))
            .thenReturn("SUCCESS: Favorito eliminado correctamente");

        ResponseEntity<String> response =
                favoritoController.eliminarFavorito("token123", favoritoDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SUCCESS: Favorito eliminado correctamente", response.getBody());
    }

    @Test
    void testEliminarFavorito_InternalError() {
        when(jwtValidationService.validarGetUsuario("token123"))
            .thenReturn("user123");
        when(favoritoService.eliminarFavorito("user123", favoritoDTO))
            .thenThrow(new RuntimeException("Unexpected"));

        ResponseEntity<String> response =
                favoritoController.eliminarFavorito("token123", favoritoDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error interno del servidor", response.getBody());
    }

    // ==========================================================
    // ================== OBTENER FAVORITOS ======================
    // ==========================================================

    @Test
    void testObtenerFavoritos_Success() {
        when(jwtValidationService.validarGetUsuario("token123"))
            .thenReturn("user123");
        when(favoritoService.obtenerFavoritosPorUsuario("user123"))
            .thenReturn(List.of("content1", "content2"));

        ResponseEntity<List<String>> response =
                favoritoController.obtenerFavoritos("token123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("content1", response.getBody().get(0));
    }

    @Test
    void testObtenerFavoritos_InternalError() {
        when(jwtValidationService.validarGetUsuario("token123"))
            .thenReturn("user123");
        when(favoritoService.obtenerFavoritosPorUsuario("user123"))
            .thenThrow(new RuntimeException("Error"));

        ResponseEntity<List<String>> response =
                favoritoController.obtenerFavoritos("token123");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}
