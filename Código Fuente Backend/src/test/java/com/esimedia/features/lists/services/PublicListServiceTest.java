package com.esimedia.features.lists.services;

import com.esimedia.features.lists.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicListServiceTest {

    @Mock
    private PublicListCreationService publicListCreationService;

    @Mock
    private PublicListContentService publicListContentService;

    @Mock
    private PublicListQueryService publicListQueryService;

    @Mock
    private PublicListUpdateService publicListUpdateService;

    @InjectMocks
    private PublicListService publicListService;

    private String idUsuario;
    private String idLista;

    @BeforeEach
    void setUp() {
        idUsuario = "user123";
        idLista = "lista123";
    }

    @Test
    void testCrearListaPublica_Success() {
        // Arrange
        ListaPublicaReproduccionDTO listaDTO = new ListaPublicaReproduccionDTO();
        String expectedResponse = "Lista creada exitosamente";
        when(publicListCreationService.crearListaPublica(any(ListaPublicaReproduccionDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = publicListService.crearListaPublica(listaDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(publicListCreationService, times(1)).crearListaPublica(any(ListaPublicaReproduccionDTO.class));
    }

    @Test
    void testAgregarContenidoListaPublica_ConIdUsuario_Success() {
        // Arrange
        AgregarContenidoDTO agregarDTO = new AgregarContenidoDTO();
        String expectedResponse = "Contenido agregado exitosamente";
        when(publicListContentService.agregarContenidoListaPublica(any(AgregarContenidoDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = publicListService.agregarContenidoListaPublica(agregarDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(publicListContentService, times(1)).agregarContenidoListaPublica(any(AgregarContenidoDTO.class));
    }

    @Test
    void testAgregarContenidoListaPublica_SinIdUsuario_Success() {
        // Arrange
        AgregarContenidoPublicoDTO agregarDTO = new AgregarContenidoPublicoDTO();
        String expectedResponse = "Contenido agregado exitosamente";
        when(publicListContentService.agregarContenidoListaPublica(any(AgregarContenidoPublicoDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = publicListService.agregarContenidoListaPublica(agregarDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(publicListContentService, times(1)).agregarContenidoListaPublica(any(AgregarContenidoPublicoDTO.class));
    }

    @Test
    void testEliminarListaPublica_Success() {
        // Arrange
        String expectedResponse = "Lista eliminada exitosamente";
        when(publicListUpdateService.eliminarListaPublica(idLista, idUsuario))
                .thenReturn(expectedResponse);

        // Act
        String result = publicListService.eliminarListaPublica(idLista, idUsuario);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(publicListUpdateService, times(1)).eliminarListaPublica(idLista, idUsuario);
    }

    @Test
    void testEliminarContenidoListaPublica_Success() {
        // Arrange
        EliminarContenidoPublicoDTO eliminarDTO = new EliminarContenidoPublicoDTO();
        String expectedResponse = "Contenido eliminado exitosamente";
        when(publicListContentService.eliminarContenidoListaPublica(any(EliminarContenidoPublicoDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = publicListService.eliminarContenidoListaPublica(eliminarDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(publicListContentService, times(1)).eliminarContenidoListaPublica(any(EliminarContenidoPublicoDTO.class));
    }

    @Test
    void testObtenerListasPublicas_Success() {
        // Arrange
        ListaResponseDTO lista1 = ListaResponseDTO.builder()
                .idLista("lista1")
                .nombre("Lista Pública 1")
                .build();

        ListaResponseDTO lista2 = ListaResponseDTO.builder()
                .idLista("lista2")
                .nombre("Lista Pública 2")
                .build();

        List<ListaResponseDTO> expectedListas = Arrays.asList(lista1, lista2);
        when(publicListQueryService.obtenerListasPublicas())
                .thenReturn(expectedListas);

        // Act
        List<ListaResponseDTO> result = publicListService.obtenerListasPublicas();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lista1", result.get(0).getIdLista());
        assertEquals("lista2", result.get(1).getIdLista());
        verify(publicListQueryService, times(1)).obtenerListasPublicas();
    }

    @Test
    void testObtenerListasPublicas_SinListas() {
        // Arrange
        when(publicListQueryService.obtenerListasPublicas())
                .thenReturn(Collections.emptyList());

        // Act
        List<ListaResponseDTO> result = publicListService.obtenerListasPublicas();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(publicListQueryService, times(1)).obtenerListasPublicas();
    }

    @Test
    void testObtenerListaPublicaConContenidos_Success() {
        // Arrange
        ListaContenidosResponseDTO expectedResponse = ListaContenidosResponseDTO.builder()
                .idLista(idLista)
                .nombre("Mi Lista")
                .descripcion("Descripción")
                .contenidos(Collections.emptyList())
                .build();
        
        when(publicListQueryService.obtenerListaPublicaConContenidos(idLista))
                .thenReturn(expectedResponse);

        // Act
        ListaContenidosResponseDTO result = publicListService.obtenerListaPublicaConContenidos(idLista);

        // Assert
        assertNotNull(result);
        assertEquals(idLista, result.getIdLista());
        assertEquals("Mi Lista", result.getNombre());
        verify(publicListQueryService, times(1)).obtenerListaPublicaConContenidos(idLista);
    }

    @Test
    void testObtenerTodasListasPublicasConContenidos_Success() {
        // Arrange
        ListaResponseDTO lista1 = ListaResponseDTO.builder()
                .idLista("lista1")
                .nombre("Lista 1")
                .build();

        ListaResponseDTO lista2 = ListaResponseDTO.builder()
                .idLista("lista2")
                .nombre("Lista 2")
                .build();

        List<ListaResponseDTO> expectedListas = Arrays.asList(lista1, lista2);
        when(publicListQueryService.obtenerTodasListasPublicasConContenidos())
                .thenReturn(expectedListas);

        // Act
        List<ListaResponseDTO> result = publicListService.obtenerTodasListasPublicasConContenidos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lista1", result.get(0).getIdLista());
        assertEquals("lista2", result.get(1).getIdLista());
        verify(publicListQueryService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

    @Test
    void testObtenerTodasListasPublicasConContenidos_SinListas() {
        // Arrange
        when(publicListQueryService.obtenerTodasListasPublicasConContenidos())
                .thenReturn(Collections.emptyList());

        // Act
        List<ListaResponseDTO> result = publicListService.obtenerTodasListasPublicasConContenidos();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(publicListQueryService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

    @Test
    void testActualizarCamposListaPublica_Success() {
        // Arrange
        ListaUpdateFieldsPublicasDTO updateDTO = new ListaUpdateFieldsPublicasDTO();
        String expectedResponse = "Campos actualizados exitosamente";
        when(publicListUpdateService.actualizarCamposListaPublica(any(ListaUpdateFieldsPublicasDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = publicListService.actualizarCamposListaPublica(updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(publicListUpdateService, times(1))
                .actualizarCamposListaPublica(any(ListaUpdateFieldsPublicasDTO.class));
    }

    @Test
    void testConstructor() {
        // Arrange & Act
        PublicListService service = new PublicListService(
            publicListCreationService,
            publicListContentService,
            publicListQueryService,
            publicListUpdateService
        );

        // Assert
        assertNotNull(service);
    }
}
