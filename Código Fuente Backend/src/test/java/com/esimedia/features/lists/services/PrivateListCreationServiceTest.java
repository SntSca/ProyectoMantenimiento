package com.esimedia.features.lists.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.ListaPrivadaReproduccionDTO;
import com.esimedia.features.lists.entity.ListaContenidoPrivada;
import com.esimedia.features.lists.entity.ListaPrivada;
import com.esimedia.features.lists.repository.ListaContenidoPrivadaRepository;
import com.esimedia.features.lists.repository.ListaPrivadaRepository;
import com.esimedia.shared.util.ContentValidationUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del Servicio de Creación de Listas Privadas")
class PrivateListCreationServiceTest {

    @Mock
    private ListaPrivadaRepository listaPrivadaRepository;

    @Mock
    private ListaContenidoPrivadaRepository listaContenidoPrivadaRepository;

    @Mock
    private ContentValidationUtil contentValidationUtil;

    @InjectMocks
    private PrivateListCreationService privateListCreationService;

    private ListaPrivadaReproduccionDTO listaDTO;
    private ListaPrivada listaGuardada;

    @BeforeEach
    void setUp() {
        listaDTO = new ListaPrivadaReproduccionDTO();
        listaDTO.setNombre("Mi Lista Privada");
        listaDTO.setDescripcion("Descripción de la lista");
        listaDTO.setIdCreadorUsuario("user-123");
        listaDTO.setContenidos(Arrays.asList("contenido-1", "contenido-2"));

        listaGuardada = ListaPrivada.builder()
            .idLista("lista-123")
            .nombre("Mi Lista Privada")
            .descripcion("Descripción de la lista")
            .idCreadorUsuario("user-123")
            .build();
    }

    // ==================== Crear Lista Privada ====================

    @Test
    @DisplayName("CA-01: Crear lista privada - Éxito")
    void testCrearListaPrivada_Success() {
        // Given
        when(listaPrivadaRepository.save(any(ListaPrivada.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPrivadaRepository.save(any(ListaContenidoPrivada.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        String resultado = privateListCreationService.crearListaPrivada(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista privada creada exitosamente", resultado);

        // Verificar que se guardó la lista
        ArgumentCaptor<ListaPrivada> listaCaptor = ArgumentCaptor.forClass(ListaPrivada.class);
        verify(listaPrivadaRepository, times(1)).save(listaCaptor.capture());
        
        ListaPrivada listaSaved = listaCaptor.getValue();
        assertEquals("Mi Lista Privada", listaSaved.getNombre());
        assertEquals("Descripción de la lista", listaSaved.getDescripcion());
        assertEquals("user-123", listaSaved.getIdCreadorUsuario());

        // Verificar que se validaron los contenidos
        verify(contentValidationUtil, times(2)).validarContenidoExistente(anyString());
        verify(contentValidationUtil).validarContenidoExistente("contenido-1");
        verify(contentValidationUtil).validarContenidoExistente("contenido-2");

        // Verificar que se guardaron los contenidos
        verify(listaContenidoPrivadaRepository, times(2)).save(any(ListaContenidoPrivada.class));
    }

    @Test
    @DisplayName("CA-02: Crear lista privada - Contenido no existe")
    void testCrearListaPrivada_ContenidoNoExiste() {
        // Given
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenido no encontrado"))
            .when(contentValidationUtil).validarContenidoExistente("contenido-1");

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            privateListCreationService.crearListaPrivada(listaDTO));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Contenido no encontrado", exception.getReason());

        // Verificar que no se guardó la lista ni los contenidos
        verify(listaPrivadaRepository, never()).save(any(ListaPrivada.class));
        verify(listaContenidoPrivadaRepository, never()).save(any(ListaContenidoPrivada.class));
    }

    @Test
    @DisplayName("CA-03: Crear lista privada - Sin contenidos")
    void testCrearListaPrivada_SinContenidos() {
        // Given
        listaDTO.setContenidos(Collections.emptyList());
        
        when(listaPrivadaRepository.save(any(ListaPrivada.class)))
            .thenReturn(listaGuardada);

        // When
        String resultado = privateListCreationService.crearListaPrivada(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista privada creada exitosamente", resultado);

        // Verificar que se guardó la lista pero no se agregaron contenidos
        verify(listaPrivadaRepository, times(1)).save(any(ListaPrivada.class));
        verify(contentValidationUtil, never()).validarContenidoExistente(anyString());
        verify(listaContenidoPrivadaRepository, never()).save(any(ListaContenidoPrivada.class));
    }

    @Test
    @DisplayName("CA-04: Crear lista privada - Con un solo contenido")
    void testCrearListaPrivada_UnSoloContenido() {
        // Given
        listaDTO.setContenidos(Arrays.asList("contenido-1"));
        
        when(listaPrivadaRepository.save(any(ListaPrivada.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPrivadaRepository.save(any(ListaContenidoPrivada.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente("contenido-1");

        // When
        String resultado = privateListCreationService.crearListaPrivada(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista privada creada exitosamente", resultado);

        verify(contentValidationUtil, times(1)).validarContenidoExistente("contenido-1");
        verify(listaContenidoPrivadaRepository, times(1)).save(any(ListaContenidoPrivada.class));
    }

    @Test
    @DisplayName("CA-05: Crear lista privada - Con múltiples contenidos")
    void testCrearListaPrivada_MultipleContenidos() {
        // Given
        listaDTO.setContenidos(Arrays.asList("contenido-1", "contenido-2", "contenido-3", "contenido-4"));
        
        when(listaPrivadaRepository.save(any(ListaPrivada.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPrivadaRepository.save(any(ListaContenidoPrivada.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        String resultado = privateListCreationService.crearListaPrivada(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista privada creada exitosamente", resultado);

        verify(contentValidationUtil, times(4)).validarContenidoExistente(anyString());
        verify(listaContenidoPrivadaRepository, times(4)).save(any(ListaContenidoPrivada.class));
    }

    @Test
    @DisplayName("CA-06: Crear lista privada - Con nombre y descripción vacíos")
    void testCrearListaPrivada_NombreDescripcionVacios() {
        // Given
        listaDTO.setNombre("");
        listaDTO.setDescripcion("");
        
        ListaPrivada listaVacia = ListaPrivada.builder()
            .idLista("lista-123")
            .nombre("")
            .descripcion("")
            .idCreadorUsuario("user-123")
            .build();
        
        when(listaPrivadaRepository.save(any(ListaPrivada.class)))
            .thenReturn(listaVacia);
        when(listaContenidoPrivadaRepository.save(any(ListaContenidoPrivada.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        String resultado = privateListCreationService.crearListaPrivada(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista privada creada exitosamente", resultado);

        ArgumentCaptor<ListaPrivada> listaCaptor = ArgumentCaptor.forClass(ListaPrivada.class);
        verify(listaPrivadaRepository, times(1)).save(listaCaptor.capture());
        
        ListaPrivada listaSaved = listaCaptor.getValue();
        assertEquals("", listaSaved.getNombre());
        assertEquals("", listaSaved.getDescripcion());
    }

    @Test
    @DisplayName("CA-07: Crear lista privada - Segundo contenido falla validación")
    void testCrearListaPrivada_SegundoContenidoFallaValidacion() {
        // Given
        doNothing().when(contentValidationUtil).validarContenidoExistente("contenido-1");
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenido no encontrado"))
            .when(contentValidationUtil).validarContenidoExistente("contenido-2");

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            privateListCreationService.crearListaPrivada(listaDTO));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // Verificar que se validó el primer contenido pero falló en el segundo
        verify(contentValidationUtil).validarContenidoExistente("contenido-1");
        verify(contentValidationUtil).validarContenidoExistente("contenido-2");
        
        // Verificar que no se guardó la lista ni los contenidos
        verify(listaPrivadaRepository, never()).save(any(ListaPrivada.class));
        verify(listaContenidoPrivadaRepository, never()).save(any(ListaContenidoPrivada.class));
    }

    @Test
    @DisplayName("CA-08: Crear lista privada - Verifica orden de operaciones")
    void testCrearListaPrivada_OrdenOperaciones() {
        // Given
        when(listaPrivadaRepository.save(any(ListaPrivada.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPrivadaRepository.save(any(ListaContenidoPrivada.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        privateListCreationService.crearListaPrivada(listaDTO);

        // Then - Verificar orden de llamadas
        var inOrder = inOrder(contentValidationUtil, listaPrivadaRepository, listaContenidoPrivadaRepository);
        
        inOrder.verify(contentValidationUtil).validarContenidoExistente("contenido-1");
        inOrder.verify(contentValidationUtil).validarContenidoExistente("contenido-2");
        inOrder.verify(listaPrivadaRepository).save(any(ListaPrivada.class));
        inOrder.verify(listaContenidoPrivadaRepository, times(2)).save(any(ListaContenidoPrivada.class));
    }

    @Test
    @DisplayName("CA-09: Crear lista privada - Verifica los IDs de contenido guardados")
    void testCrearListaPrivada_VerificaIdsContenido() {
        // Given
        when(listaPrivadaRepository.save(any(ListaPrivada.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPrivadaRepository.save(any(ListaContenidoPrivada.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        privateListCreationService.crearListaPrivada(listaDTO);

        // Then
        ArgumentCaptor<ListaContenidoPrivada> contenidoCaptor = ArgumentCaptor.forClass(ListaContenidoPrivada.class);
        verify(listaContenidoPrivadaRepository, times(2)).save(contenidoCaptor.capture());
        
        var contenidosGuardados = contenidoCaptor.getAllValues();
        assertEquals(2, contenidosGuardados.size());
        assertEquals("lista-123", contenidosGuardados.get(0).getIdLista());
        assertEquals("contenido-1", contenidosGuardados.get(0).getIdContenido());
        assertEquals("lista-123", contenidosGuardados.get(1).getIdLista());
        assertEquals("contenido-2", contenidosGuardados.get(1).getIdContenido());
    }

    @Test
    @DisplayName("CA-10: Constructor verifica dependencias")
    void testConstructor() {
        // Arrange & Act
        PrivateListCreationService service = new PrivateListCreationService(
            listaPrivadaRepository,
            listaContenidoPrivadaRepository,
            contentValidationUtil
        );

        // Assert
        assertNotNull(service);
    }
}
