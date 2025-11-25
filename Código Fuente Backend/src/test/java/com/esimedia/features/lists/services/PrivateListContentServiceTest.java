package com.esimedia.features.lists.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.EliminarContenidoDTO;
import com.esimedia.features.lists.entity.ListaContenidoPrivada;
import com.esimedia.features.lists.entity.ListaPrivada;
import com.esimedia.features.lists.repository.ListaContenidoPrivadaRepository;
import com.esimedia.features.lists.repository.ListaPrivadaRepository;
import com.esimedia.shared.util.ContentValidationUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del Servicio de Contenido de Listas Privadas")
class PrivateListContentServiceTest {

    @Mock
    private ListaPrivadaRepository listaPrivadaRepository;

    @Mock
    private ListaContenidoPrivadaRepository listaContenidoPrivadaRepository;

    @Mock
    private ContentValidationUtil contentValidationUtil;

    @InjectMocks
    private PrivateListContentService privateListContentService;

    private EliminarContenidoDTO eliminarDTO;
    private ListaPrivada listaPrivada;
    private ListaContenidoPrivada contenido;

    @BeforeEach
    void setUp() {
        eliminarDTO = new EliminarContenidoDTO("lista123", Arrays.asList("contenido456"), "usuario789");
        listaPrivada = ListaPrivada.builder()
            .idLista("lista123")
            .nombre("Mi Lista")
            .descripcion("Descripción")
            .idCreadorUsuario("usuario789")
            .visibilidad(false)
            .build();
        contenido = ListaContenidoPrivada.builder()
            .idLista("lista123")
            .idContenido("contenido456")
            .build();
    }

    @Test
    @DisplayName("Debe eliminar contenido de lista privada exitosamente")
    void testEliminarContenidoListaPrivadaExitoso() {
        // Given
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));
        when(listaContenidoPrivadaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        String result = privateListContentService.eliminarContenidoListaPrivada(eliminarDTO);

        // Then
        assertEquals("Contenidos eliminados de la lista privada exitosamente", result);
        verify(listaContenidoPrivadaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");
    }

    @Test
    @DisplayName("Debe lanzar NOT_FOUND si la lista privada no existe")
    void testEliminarContenidoListaPrivadaListaNoEncontrada() {
        // Given
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            privateListContentService.eliminarContenidoListaPrivada(eliminarDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Lista privada no encontrada", exception.getReason());
    }

    @Test
    @DisplayName("Debe lanzar FORBIDDEN si el usuario no es el propietario")
    void testEliminarContenidoListaPrivadaUsuarioNoPropietario() {
        // Given
        listaPrivada.setIdCreadorUsuario("otroUsuario");
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            privateListContentService.eliminarContenidoListaPrivada(eliminarDTO));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("No tienes permiso para modificar esta lista", exception.getReason());
    }

    @Test
    @DisplayName("Debe lanzar NOT_FOUND si el contenido no está en la lista")
    void testEliminarContenidoListaPrivadaContenidoNoEncontrado() {
        // Given
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));
        when(listaContenidoPrivadaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(false);

        // When
        String result = privateListContentService.eliminarContenidoListaPrivada(eliminarDTO);

        // Then
        assertEquals("Contenidos eliminados de la lista privada exitosamente", result);
        verify(listaContenidoPrivadaRepository, never()).deleteByIdListaAndIdContenido("lista123", "contenido456");
    }

    @Test
    @DisplayName("Debe lanzar BAD_REQUEST si DTO es nulo")
    void testEliminarContenidoListaPrivadaDTONulo() {
        // When & Then
        assertThrows(NullPointerException.class, () ->
            privateListContentService.eliminarContenidoListaPrivada(null));
    }

    @Test
    @DisplayName("Debe lanzar NOT_FOUND si idLista es nulo")
    void testEliminarContenidoListaPrivadaIdListaNulo() {
        // Given
        EliminarContenidoDTO dtoNulo = new EliminarContenidoDTO();
        dtoNulo.setIdLista(null);
        dtoNulo.setIdsContenido(Arrays.asList("contenido456"));
        dtoNulo.setIdUsuario("usuario789");

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            privateListContentService.eliminarContenidoListaPrivada(dtoNulo));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si idsContenido es nulo")
    void testEliminarContenidoListaPrivadaIdContenidoNulo() {
        // Given
        EliminarContenidoDTO dtoNulo = new EliminarContenidoDTO();
        dtoNulo.setIdLista("lista123");
        dtoNulo.setIdsContenido(null);
        dtoNulo.setIdUsuario("usuario789");
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));

        // When & Then
        assertThrows(NullPointerException.class, () ->
            privateListContentService.eliminarContenidoListaPrivada(dtoNulo));
    }

    @Test
    @DisplayName("Debe manejar excepción del repository al eliminar")
    void testEliminarContenidoListaPrivadaExcepcionDelete() {
        // Given
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));
        when(listaContenidoPrivadaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(listaContenidoPrivadaRepository)
            .deleteByIdListaAndIdContenido("lista123", "contenido456");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            privateListContentService.eliminarContenidoListaPrivada(eliminarDTO));
        assertEquals("DB error", exception.getMessage());
    }

    @Test
    @DisplayName("Debe eliminar contenido cuando la lista tiene múltiples contenidos")
    void testEliminarContenidoListaPrivadaMultipleContenidos() {
        // Given
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));
        when(listaContenidoPrivadaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        String result = privateListContentService.eliminarContenidoListaPrivada(eliminarDTO);

        // Then
        assertEquals("Contenidos eliminados de la lista privada exitosamente", result);
        verify(listaContenidoPrivadaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");
    }

    @Test
    @DisplayName("Debe eliminar contenido cuando es el único en la lista")
    void testEliminarContenidoListaPrivadaUnicoContenido() {
        // Similar al exitoso, pero podemos verificar que no afecta otros
        // Given
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));
        when(listaContenidoPrivadaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        String result = privateListContentService.eliminarContenidoListaPrivada(eliminarDTO);

        // Then
        assertEquals("Contenidos eliminados de la lista privada exitosamente", result);
        verify(listaContenidoPrivadaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");
        verifyNoMoreInteractions(listaContenidoPrivadaRepository);
    }

    @Test
    @DisplayName("Debe verificar permisos correctamente con usuario diferente")
    void testEliminarContenidoListaPrivadaPermisos() {
        // Given
        ListaPrivada listaOtroUsuario = ListaPrivada.builder()
            .idLista("lista123")
            .idCreadorUsuario("otroUsuario")
            .build();
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaOtroUsuario));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            privateListContentService.eliminarContenidoListaPrivada(eliminarDTO));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    @DisplayName("Debe manejar lista con idCreadorUsuario nulo")
    void testEliminarContenidoListaPrivadaCreadorNulo() {
        // Given
        ListaPrivada listaSinCreador = ListaPrivada.builder()
            .idLista("lista123")
            .idCreadorUsuario(null)
            .build();
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaSinCreador));

        // When & Then
        assertThrows(NullPointerException.class, () ->
            privateListContentService.eliminarContenidoListaPrivada(eliminarDTO));
    }

    @Test
    @DisplayName("Debe no eliminar si existsBy devuelve false")
    void testEliminarContenidoListaPrivadaExistsFalse() {
        // Given
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));
        when(listaContenidoPrivadaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(false);

        // When
        String result = privateListContentService.eliminarContenidoListaPrivada(eliminarDTO);

        // Then
        assertEquals("Contenidos eliminados de la lista privada exitosamente", result);
        verify(listaContenidoPrivadaRepository, never()).deleteByIdListaAndIdContenido(any(), any());
    }

    @Test
    @DisplayName("Debe llamar a existsBy con parámetros correctos")
    void testEliminarContenidoListaPrivadaVerificarLlamadas() {
        // Given
        when(listaPrivadaRepository.findById("lista123")).thenReturn(Optional.of(listaPrivada));
        when(listaContenidoPrivadaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        privateListContentService.eliminarContenidoListaPrivada(eliminarDTO);

        // Then
        verify(listaPrivadaRepository).findById("lista123");
        verify(listaContenidoPrivadaRepository).existsByIdListaAndIdContenido("lista123", "contenido456");
        verify(listaContenidoPrivadaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");
    }
}