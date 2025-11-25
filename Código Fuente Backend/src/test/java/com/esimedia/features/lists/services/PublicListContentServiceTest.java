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

import com.esimedia.features.lists.dto.EliminarContenidoPublicoDTO;
import com.esimedia.features.lists.entity.ListaContenidoPublica;
import com.esimedia.features.lists.entity.ListaPublica;
import com.esimedia.features.lists.repository.ListaContenidoPublicaRepository;
import com.esimedia.features.lists.repository.ListaPublicaRepository;
import com.esimedia.shared.util.ContentValidationUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del Servicio de Contenido de Listas Públicas")
class PublicListContentServiceTest {

    @Mock
    private ListaPublicaRepository listaPublicaRepository;

    @Mock
    private ListaContenidoPublicaRepository listaContenidoPublicaRepository;

    @Mock
    private ContentValidationUtil contentValidationUtil;

    @InjectMocks
    private PublicListContentService publicListContentService;

    private EliminarContenidoPublicoDTO eliminarDTO;
    private ListaPublica listaPublica;
    private ListaContenidoPublica contenido;

    @BeforeEach
    void setUp() {
        eliminarDTO = new EliminarContenidoPublicoDTO("lista123", Arrays.asList("contenido456"));
        listaPublica = ListaPublica.builder()
            .idLista("lista123")
            .nombre("Mi Lista Pública")
            .descripcion("Descripción")
            .idCreadorUsuario("usuario789")
            .visibilidad(true)
            .build();
        contenido = ListaContenidoPublica.builder()
            .idLista("lista123")
            .idContenido("contenido456")
            .build();
    }

    @Test
    @DisplayName("Debe eliminar contenido de lista pública exitosamente")
    void testEliminarContenidoListaPublicaExitoso() {
        // Given
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        String result = publicListContentService.eliminarContenidoListaPublica(eliminarDTO);

        // Then
        assertEquals("Contenidos eliminados de la lista pública exitosamente", result);
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");
    }

    @Test
    @DisplayName("Debe lanzar NOT_FOUND si la lista pública no existe")
    void testEliminarContenidoListaPublicaListaNoEncontrada() {
        // Given
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            publicListContentService.eliminarContenidoListaPublica(eliminarDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Lista pública no encontrada", exception.getReason());
    }


    @Test
    @DisplayName("Debe ignorar si el contenido no está en la lista")
    void testEliminarContenidoListaPublicaContenidoNoEncontrado() {
        // Given
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(false);

        // When
        String result = publicListContentService.eliminarContenidoListaPublica(eliminarDTO);

        // Then
        assertEquals("Contenidos eliminados de la lista pública exitosamente", result);
        verify(listaContenidoPublicaRepository, never()).deleteByIdListaAndIdContenido(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe eliminar múltiples contenidos exitosamente")
    void testEliminarContenidoListaPublicaMultiplesContenidos() {
        // Given
        EliminarContenidoPublicoDTO dtoMulti = new EliminarContenidoPublicoDTO();
        dtoMulti.setIdLista("lista123");
        dtoMulti.setIdsContenido(Arrays.asList("contenido1", "contenido2", "contenido3"));
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido1")).thenReturn(true);
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido2")).thenReturn(true);
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido3")).thenReturn(true);

        // When
        String result = publicListContentService.eliminarContenidoListaPublica(dtoMulti);

        // Then
        assertEquals("Contenidos eliminados de la lista pública exitosamente", result);
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "contenido1");
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "contenido2");
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "contenido3");
    }

    @Test
    @DisplayName("Debe manejar mezcla de contenidos existentes y no existentes")
    void testEliminarContenidoListaPublicaMezclaExistentesNoExistentes() {
        // Given
        EliminarContenidoPublicoDTO dtoMezcla = new EliminarContenidoPublicoDTO();
        dtoMezcla.setIdLista("lista123");
        dtoMezcla.setIdsContenido(Arrays.asList("existente", "noexistente", "existente2"));
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "existente")).thenReturn(true);
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "noexistente")).thenReturn(false);
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "existente2")).thenReturn(true);

        // When
        String result = publicListContentService.eliminarContenidoListaPublica(dtoMezcla);

        // Then
        assertEquals("Contenidos eliminados de la lista pública exitosamente", result);
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "existente");
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "existente2");
        verify(listaContenidoPublicaRepository, never()).deleteByIdListaAndIdContenido("lista123", "noexistente");
    }

    @Test
    @DisplayName("Debe manejar idsContenido con duplicados")
    void testEliminarContenidoListaPublicaDuplicados() {
        // Given
        EliminarContenidoPublicoDTO dtoDuplicados = new EliminarContenidoPublicoDTO();
        dtoDuplicados.setIdLista("lista123");
        dtoDuplicados.setIdsContenido(Arrays.asList("contenido456", "contenido456"));
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        String result = publicListContentService.eliminarContenidoListaPublica(dtoDuplicados);

        // Then
        assertEquals("Contenidos eliminados de la lista pública exitosamente", result);
        verify(listaContenidoPublicaRepository, times(2)).deleteByIdListaAndIdContenido("lista123", "contenido456");
    }

    @Test
    @DisplayName("Debe lanzar NOT_FOUND si idLista es vacío")
    void testEliminarContenidoListaPublicaIdListaVacio() {
        // Given
        EliminarContenidoPublicoDTO dtoVacio = new EliminarContenidoPublicoDTO();
        dtoVacio.setIdLista("");
        dtoVacio.setIdsContenido(Arrays.asList("contenido456"));
        when(listaPublicaRepository.findById("")).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            publicListContentService.eliminarContenidoListaPublica(dtoVacio));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Debe manejar idsContenido con strings vacíos")
    void testEliminarContenidoListaPublicaStringsVacios() {
        // Given
        EliminarContenidoPublicoDTO dtoVacios = new EliminarContenidoPublicoDTO();
        dtoVacios.setIdLista("lista123");
        dtoVacios.setIdsContenido(Arrays.asList("", "contenido456"));
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "")).thenReturn(false);
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        String result = publicListContentService.eliminarContenidoListaPublica(dtoVacios);

        // Then
        assertEquals("Contenidos eliminados de la lista pública exitosamente", result);
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");
        verify(listaContenidoPublicaRepository, never()).deleteByIdListaAndIdContenido("lista123", "");
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si idsContenido es nulo")
    void testEliminarContenidoListaPublicaIdsContenidoNulo() {
        // Given
        EliminarContenidoPublicoDTO dtoNulo = new EliminarContenidoPublicoDTO();
        dtoNulo.setIdLista("lista123");
        dtoNulo.setIdsContenido(null);
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));

        // When & Then
        assertThrows(NullPointerException.class, () ->
            publicListContentService.eliminarContenidoListaPublica(dtoNulo));
    }

    @Test
    @DisplayName("Debe manejar lista con idLista nulo")
    void testEliminarContenidoListaPublicaIdListaNulo() {
        // Given
        EliminarContenidoPublicoDTO dtoNuloLista = new EliminarContenidoPublicoDTO();
        dtoNuloLista.setIdLista(null);
        dtoNuloLista.setIdsContenido(Arrays.asList("contenido456"));
        when(listaPublicaRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            publicListContentService.eliminarContenidoListaPublica(dtoNuloLista));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Debe verificar llamadas a existsBy con parámetros correctos")
    void testEliminarContenidoListaPublicaVerificarLlamadasExists() {
        // Given
        EliminarContenidoPublicoDTO dtoVerificar = new EliminarContenidoPublicoDTO();
        dtoVerificar.setIdLista("lista123");
        dtoVerificar.setIdsContenido(Arrays.asList("contenido456"));
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        publicListContentService.eliminarContenidoListaPublica(dtoVerificar);

        // Then
        verify(listaContenidoPublicaRepository).existsByIdListaAndIdContenido("lista123", "contenido456");
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");
        verifyNoMoreInteractions(listaContenidoPublicaRepository);
    }

    @Test
    @DisplayName("Debe manejar excepción en deleteByIdListaAndIdContenido")
    void testEliminarContenidoListaPublicaExcepcionDelete() {
        // Given
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaPublica));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);
        doThrow(new RuntimeException("Error en delete")).when(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");

        // When & Then
        assertThrows(RuntimeException.class, () ->
            publicListContentService.eliminarContenidoListaPublica(eliminarDTO));
    }

    @Test
    @DisplayName("Debe eliminar contenido cuando lista tiene creador nulo")
    void testEliminarContenidoListaPublicaCreadorNulo() {
        // Given
        ListaPublica listaSinCreador = new ListaPublica();
        listaSinCreador.setIdLista("lista123");
        listaSinCreador.setIdCreadorUsuario(null);
        when(listaPublicaRepository.findById("lista123")).thenReturn(Optional.of(listaSinCreador));
        when(listaContenidoPublicaRepository.existsByIdListaAndIdContenido("lista123", "contenido456")).thenReturn(true);

        // When
        String result = publicListContentService.eliminarContenidoListaPublica(eliminarDTO);

        // Then
        assertEquals("Contenidos eliminados de la lista pública exitosamente", result);
        verify(listaContenidoPublicaRepository).deleteByIdListaAndIdContenido("lista123", "contenido456");
    }
}