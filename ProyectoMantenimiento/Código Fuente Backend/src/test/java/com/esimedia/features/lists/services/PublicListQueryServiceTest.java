package com.esimedia.features.lists.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import com.esimedia.features.lists.dto.ListaContenidosResponseDTO;
import com.esimedia.features.lists.dto.ListaResponseDTO;
import com.esimedia.features.lists.entity.ListaPublica;
import com.esimedia.features.lists.repository.ListaContenidoPublicaRepository;
import com.esimedia.features.lists.repository.ListaPublicaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del Servicio de Consulta de Listas Públicas")
class PublicListQueryServiceTest {

    @Mock
    private ListaPublicaRepository listaPublicaRepository;

    @Mock
    private ListaContenidoPublicaRepository listaContenidoPublicaRepository;

    @Mock
    private PublicListHelper publicListHelper;

    @InjectMocks
    private PublicListQueryService publicListQueryService;

    private ListaPublica listaPublica1;
    private ListaPublica listaPublica2;
    private String creadorId;
    private String listaId;

    @BeforeEach
    void setUp() {
        creadorId = "creator-123";
        listaId = "lista-pub-001";

        // Lista pública 1
        listaPublica1 = ListaPublica.builder()
            .idLista(listaId)
            .nombre("Mi Lista de Favoritos")
            .descripcion("Mis canciones favoritas")
            .idCreadorUsuario(creadorId)
            .visibilidad(true)
            .build();

        // Lista pública 2
        listaPublica2 = ListaPublica.builder()
            .idLista("lista-pub-002")
            .nombre("Éxitos del Momento")
            .descripcion("Los mejores éxitos")
            .idCreadorUsuario(creadorId)
            .visibilidad(true)
            .build();
    }

    // ==================== Consultas de Listas Públicas ====================

    @Test
    @DisplayName("CA-01: Consulta exitosa - No devuelve ninguna lista")
    void testObtenerListasPublicas_EmptyList() {
        // Given
        when(listaPublicaRepository.findAll())
            .thenReturn(new ArrayList<>());

        // When
        List<ListaResponseDTO> resultado = publicListQueryService.obtenerListasPublicas();

        // Then
        assertNotNull(resultado);
        assertEquals(0, resultado.size());
        verify(listaPublicaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CA-02: Consulta exitosa - Devuelve una lista sin contenidos")
    void testObtenerListasPublicas_SingleListNoContents() {
        // Given
        List<ListaPublica> listas = Arrays.asList(listaPublica1);
        when(listaPublicaRepository.findAll())
            .thenReturn(listas);

        // When
        List<ListaResponseDTO> resultado = publicListQueryService.obtenerListasPublicas();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Mi Lista de Favoritos", resultado.get(0).getNombre());
        assertEquals("Mis canciones favoritas", resultado.get(0).getDescripcion());
        assertTrue(resultado.get(0).getVisibilidad());
        verify(listaPublicaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CA-03: Consulta exitosa - Devuelve una lista con contenidos (1 audio y 1 video)")
    void testObtenerListasPublicas_SingleListWithContents() {
        // Given
        List<ListaPublica> listas = Arrays.asList(listaPublica1);
        when(listaPublicaRepository.findAll())
            .thenReturn(listas);

        // When
        List<ListaResponseDTO> resultado = publicListQueryService.obtenerListasPublicas();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        ListaResponseDTO lista = resultado.get(0);
        assertEquals(listaId, lista.getIdLista());
        assertEquals("Mi Lista de Favoritos", lista.getNombre());
        assertEquals(creadorId, lista.getIdCreadorUsuario());
        assertTrue(lista.getVisibilidad());
        verify(listaPublicaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CA-04: Consulta exitosa - Devuelve dos listas con contenidos (1 audio y 1 video)")
    void testObtenerListasPublicas_TwoListsWithContents() {
        // Given
        List<ListaPublica> listas = Arrays.asList(listaPublica1, listaPublica2);
        when(listaPublicaRepository.findAll())
            .thenReturn(listas);

        // When
        List<ListaResponseDTO> resultado = publicListQueryService.obtenerListasPublicas();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Mi Lista de Favoritos", resultado.get(0).getNombre());
        assertEquals("Éxitos del Momento", resultado.get(1).getNombre());
        assertTrue(resultado.stream().allMatch(l -> l.getVisibilidad() == true));
        verify(listaPublicaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CA-05: Consulta errónea - No está el usuario autenticado (no proporciona JWT)")
    void testObtenerListasPublicas_UnauthorizedNoAuth() {
        // Given
        when(listaPublicaRepository.findAll())
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> publicListQueryService.obtenerListasPublicas()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(listaPublicaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CA-06: Obtener lista pública por ID - Éxito")
    void testObtenerListaPublicaConContenidos_Success() {
        // Given
        when(listaPublicaRepository.findById(listaId))
            .thenReturn(Optional.of(listaPublica1));

        when(listaContenidoPublicaRepository.findByIdLista(listaId))
            .thenReturn(new ArrayList<>());

        when(publicListHelper.obtenerContenidosCompletos(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        ListaContenidosResponseDTO resultado = 
            publicListQueryService.obtenerListaPublicaConContenidos(listaId);

        // Then
        assertNotNull(resultado);
        assertEquals(listaId, resultado.getIdLista());
        assertEquals("Mi Lista de Favoritos", resultado.getNombre());
        assertEquals("Mis canciones favoritas", resultado.getDescripcion());
        assertEquals(creadorId, resultado.getIdCreadorUsuario());
        assertTrue(resultado.getVisibilidad());
        assertNotNull(resultado.getContenidos());

        verify(listaPublicaRepository, times(1)).findById(listaId);
    }

    @Test
    @DisplayName("CA-07: Obtener lista pública por ID - ID no encontrado (404)")
    void testObtenerListaPublicaConContenidos_NotFound() {
        // Given
        when(listaPublicaRepository.findById(listaId))
            .thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> publicListQueryService.obtenerListaPublicaConContenidos(listaId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(listaPublicaRepository, times(1)).findById(listaId);
    }

    @Test
    @DisplayName("CA-08: Obtener lista pública por ID - ID vacío (validación)")
    void testObtenerListaPublicaConContenidos_EmptyId() {
        // When & Then
        assertThrows(
            ResponseStatusException.class,
            () -> publicListQueryService.obtenerListaPublicaConContenidos("")
        );

        verify(listaPublicaRepository, times(1)).findById("");
    }

    @Test
    @DisplayName("CA-09: Obtener lista pública por ID - Múltiples contenidos")
    void testObtenerListaPublicaConContenidos_WithMultipleContents() {
        // Given
        when(listaPublicaRepository.findById(listaId))
            .thenReturn(Optional.of(listaPublica1));

        when(listaContenidoPublicaRepository.findByIdLista(listaId))
            .thenReturn(new ArrayList<>());

        when(publicListHelper.obtenerContenidosCompletos(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        ListaContenidosResponseDTO resultado = 
            publicListQueryService.obtenerListaPublicaConContenidos(listaId);

        // Then
        assertNotNull(resultado);
        assertEquals(listaId, resultado.getIdLista());
        verify(listaPublicaRepository, times(1)).findById(listaId);
    }

    @Test
    @DisplayName("CA-10: Validar que solo se devuelven listas públicas")
    void testObtenerListasPublicas_OnlyPublic() {
        // Given
        List<ListaPublica> listas = Arrays.asList(listaPublica1, listaPublica2);
        when(listaPublicaRepository.findAll())
            .thenReturn(listas);

        // When
        List<ListaResponseDTO> resultado = publicListQueryService.obtenerListasPublicas();

        // Then
        assertTrue(resultado.stream().allMatch(l -> l.getVisibilidad() == true));
        verify(listaPublicaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CA-11: Error en base de datos - 500")
    void testObtenerListasPublicas_DatabaseError() {
        // Given
        when(listaPublicaRepository.findAll())
            .thenThrow(new RuntimeException("Error en base de datos"));

        // When & Then
        assertThrows(
            RuntimeException.class,
            () -> publicListQueryService.obtenerListasPublicas()
        );

        verify(listaPublicaRepository, times(1)).findAll();
    }
}
