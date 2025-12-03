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
class PrivateListServiceTest {

    @Mock
    private PrivateListCreationService privateListCreationService;

    @Mock
    private PrivateListContentService privateListContentService;

    @Mock
    private PrivateListQueryService privateListQueryService;

    @Mock
    private PrivateListUpdateService privateListUpdateService;

    @InjectMocks
    private PrivateListService privateListService;

    private String idUsuario;
    private String idLista;

    @BeforeEach
    void setUp() {
        idUsuario = "user123";
        idLista = "lista123";
    }

    @Test
    void testCrearListaPrivada_Success() {
        // Arrange
        ListaPrivadaReproduccionDTO listaDTO = new ListaPrivadaReproduccionDTO();
        String expectedResponse = "Lista creada exitosamente";
        when(privateListCreationService.crearListaPrivada(any(ListaPrivadaReproduccionDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = privateListService.crearListaPrivada(listaDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(privateListCreationService, times(1)).crearListaPrivada(any(ListaPrivadaReproduccionDTO.class));
    }

    @Test
    void testAgregarContenidoListaPrivada_Success() {
        // Arrange
        AgregarContenidoDTO agregarDTO = new AgregarContenidoDTO();
        String expectedResponse = "Contenido agregado exitosamente";
        when(privateListContentService.agregarContenidoListaPrivada(any(AgregarContenidoDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = privateListService.agregarContenidoListaPrivada(agregarDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(privateListContentService, times(1)).agregarContenidoListaPrivada(any(AgregarContenidoDTO.class));
    }

    @Test
    void testEliminarContenidoListaPrivada_Success() {
        // Arrange
        EliminarContenidoDTO eliminarDTO = new EliminarContenidoDTO();
        String expectedResponse = "Contenido eliminado exitosamente";
        when(privateListContentService.eliminarContenidoListaPrivada(any(EliminarContenidoDTO.class)))
                .thenReturn(expectedResponse);

        // Act
        String result = privateListService.eliminarContenidoListaPrivada(eliminarDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(privateListContentService, times(1)).eliminarContenidoListaPrivada(any(EliminarContenidoDTO.class));
    }

    @Test
    void testEliminarListaPrivada_Success() {
        // Arrange
        String expectedResponse = "Lista eliminada exitosamente";
        when(privateListUpdateService.eliminarListaPrivada(idLista, idUsuario))
                .thenReturn(expectedResponse);

        // Act
        String result = privateListService.eliminarListaPrivada(idLista, idUsuario);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(privateListUpdateService, times(1)).eliminarListaPrivada(idLista, idUsuario);
    }

    @Test
    void testObtenerTodasListasPrivadasConContenidos_Success() {
        // Arrange
        ListaPrivadaResponseDTO lista1 = ListaPrivadaResponseDTO.builder()
                .idLista("lista1")
                .nombre("Lista 1")
                .build();

        ListaPrivadaResponseDTO lista2 = ListaPrivadaResponseDTO.builder()
                .idLista("lista2")
                .nombre("Lista 2")
                .build();

        List<ListaPrivadaResponseDTO> expectedListas = Arrays.asList(lista1, lista2);
        when(privateListQueryService.obtenerTodasListasPrivadasConContenidos(idUsuario))
                .thenReturn(expectedListas);

        // Act
        List<ListaPrivadaResponseDTO> result = 
            privateListService.obtenerTodasListasPrivadasConContenidos(idUsuario);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lista1", result.get(0).getIdLista());
        assertEquals("lista2", result.get(1).getIdLista());
        verify(privateListQueryService, times(1)).obtenerTodasListasPrivadasConContenidos(idUsuario);
    }

    @Test
    void testObtenerTodasListasPrivadasConContenidos_UsuarioSinListas() {
        // Arrange
        when(privateListQueryService.obtenerTodasListasPrivadasConContenidos(idUsuario))
                .thenReturn(Collections.emptyList());

        // Act
        List<ListaPrivadaResponseDTO> result = 
            privateListService.obtenerTodasListasPrivadasConContenidos(idUsuario);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(privateListQueryService, times(1)).obtenerTodasListasPrivadasConContenidos(idUsuario);
    }

    @Test
    void testActualizarListaPrivada_Success() {
        // Arrange
        ListaUpdateDTO updateDTO = new ListaUpdateDTO();
        String expectedResponse = "Lista actualizada exitosamente";
        when(privateListUpdateService.actualizarListaPrivada(any(ListaUpdateDTO.class), eq(idUsuario)))
                .thenReturn(expectedResponse);

        // Act
        String result = privateListService.actualizarListaPrivada(updateDTO, idUsuario);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(privateListUpdateService, times(1)).actualizarListaPrivada(any(ListaUpdateDTO.class), eq(idUsuario));
    }

    @Test
    void testActualizarCamposListaPrivada_Success() {
        // Arrange
        ListaUpdateFieldsPrivadasDTO updateFieldsDTO = new ListaUpdateFieldsPrivadasDTO();
        String expectedResponse = "Campos actualizados exitosamente";
        when(privateListUpdateService.actualizarCamposListaPrivada(any(ListaUpdateFieldsPrivadasDTO.class), eq(idUsuario)))
                .thenReturn(expectedResponse);

        // Act
        String result = privateListService.actualizarCamposListaPrivada(updateFieldsDTO, idUsuario);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(privateListUpdateService, times(1))
                .actualizarCamposListaPrivada(any(ListaUpdateFieldsPrivadasDTO.class), eq(idUsuario));
    }

    @Test
    void testConstructor() {
        // Arrange & Act
        PrivateListService service = new PrivateListService(
            privateListCreationService,
            privateListContentService,
            privateListQueryService,
            privateListUpdateService
        );

        // Assert
        assertNotNull(service);
    }
}
