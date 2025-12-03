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

import com.esimedia.features.lists.dto.ListaPublicaReproduccionDTO;
import com.esimedia.features.lists.entity.ListaContenidoPublica;
import com.esimedia.features.lists.entity.ListaPublica;
import com.esimedia.features.lists.repository.ListaContenidoPublicaRepository;
import com.esimedia.features.lists.repository.ListaPublicaRepository;
import com.esimedia.shared.util.ContentValidationUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del Servicio de Creación de Listas Públicas")
class PublicListCreationServiceTest {

    @Mock
    private ListaPublicaRepository listaPublicaRepository;

    @Mock
    private ListaContenidoPublicaRepository listaContenidoPublicaRepository;

    @Mock
    private ContentValidationUtil contentValidationUtil;

    @InjectMocks
    private PublicListCreationService publicListCreationService;

    private ListaPublicaReproduccionDTO listaDTO;
    private ListaPublica listaGuardada;

    @BeforeEach
    void setUp() {
        listaDTO = new ListaPublicaReproduccionDTO();
        listaDTO.setNombre("Mi Lista Pública");
        listaDTO.setDescripcion("Descripción de la lista");
        listaDTO.setIdCreadorUsuario("user-123");
        listaDTO.setVisibilidad(true);
        listaDTO.setContenidos(Arrays.asList("contenido-1", "contenido-2"));

        listaGuardada = ListaPublica.builder()
            .idLista("lista-123")
            .nombre("Mi Lista Pública")
            .descripcion("Descripción de la lista")
            .idCreadorUsuario("user-123")
            .visibilidad(true)
            .build();
    }

    // ==================== Crear Lista Pública ====================

    @Test
    @DisplayName("CA-01: Crear lista pública - Éxito")
    void testCrearListaPublica_Success() {
        // Given
        when(listaPublicaRepository.existsByNombre("Mi Lista Pública"))
            .thenReturn(false);
        when(listaPublicaRepository.save(any(ListaPublica.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPublicaRepository.save(any(ListaContenidoPublica.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        String resultado = publicListCreationService.crearListaPublica(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista pública creada exitosamente", resultado);

        // Verificar que se guardó la lista
        ArgumentCaptor<ListaPublica> listaCaptor = ArgumentCaptor.forClass(ListaPublica.class);
        verify(listaPublicaRepository, times(1)).save(listaCaptor.capture());
        
        ListaPublica listaSaved = listaCaptor.getValue();
        assertEquals("Mi Lista Pública", listaSaved.getNombre());
        assertEquals("Descripción de la lista", listaSaved.getDescripcion());
        assertEquals("user-123", listaSaved.getIdCreadorUsuario());
        assertTrue(listaSaved.getVisibilidad());

        // Verificar que se validaron los contenidos
        verify(contentValidationUtil, times(2)).validarContenidoExistente(anyString());
        verify(contentValidationUtil).validarContenidoExistente("contenido-1");
        verify(contentValidationUtil).validarContenidoExistente("contenido-2");

        // Verificar que se guardaron los contenidos
        verify(listaContenidoPublicaRepository, times(2)).save(any(ListaContenidoPublica.class));
    }

    @Test
    @DisplayName("CA-02: Crear lista pública - Lista con nombre duplicado")
    void testCrearListaPublica_NombreDuplicado() {
        // Given
        when(listaPublicaRepository.existsByNombre("Mi Lista Pública"))
            .thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            publicListCreationService.crearListaPublica(listaDTO));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Ya existe una lista pública con el nombre: Mi Lista Pública", exception.getReason());

        // Verificar que no se guardó nada
        verify(listaPublicaRepository, never()).save(any(ListaPublica.class));
        verify(listaContenidoPublicaRepository, never()).save(any(ListaContenidoPublica.class));
        verify(contentValidationUtil, never()).validarContenidoExistente(anyString());
    }

    @Test
    @DisplayName("CA-03: Crear lista pública - Contenido no existe")
    void testCrearListaPublica_ContenidoNoExiste() {
        // Given
        when(listaPublicaRepository.existsByNombre("Mi Lista Pública"))
            .thenReturn(false);
        
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenido no encontrado"))
            .when(contentValidationUtil).validarContenidoExistente("contenido-1");

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            publicListCreationService.crearListaPublica(listaDTO));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Contenido no encontrado", exception.getReason());

        // Verificar que no se guardó la lista ni los contenidos
        verify(listaPublicaRepository, never()).save(any(ListaPublica.class));
        verify(listaContenidoPublicaRepository, never()).save(any(ListaContenidoPublica.class));
    }

    @Test
    @DisplayName("CA-04: Crear lista pública - Sin contenidos")
    void testCrearListaPublica_SinContenidos() {
        // Given
        listaDTO.setContenidos(Collections.emptyList());
        
        when(listaPublicaRepository.existsByNombre("Mi Lista Pública"))
            .thenReturn(false);
        when(listaPublicaRepository.save(any(ListaPublica.class)))
            .thenReturn(listaGuardada);

        // When
        String resultado = publicListCreationService.crearListaPublica(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista pública creada exitosamente", resultado);

        // Verificar que se guardó la lista pero no se agregaron contenidos
        verify(listaPublicaRepository, times(1)).save(any(ListaPublica.class));
        verify(contentValidationUtil, never()).validarContenidoExistente(anyString());
        verify(listaContenidoPublicaRepository, never()).save(any(ListaContenidoPublica.class));
    }

    @Test
    @DisplayName("CA-05: Crear lista pública - Con un solo contenido")
    void testCrearListaPublica_UnSoloContenido() {
        // Given
        listaDTO.setContenidos(Arrays.asList("contenido-1"));
        
        when(listaPublicaRepository.existsByNombre("Mi Lista Pública"))
            .thenReturn(false);
        when(listaPublicaRepository.save(any(ListaPublica.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPublicaRepository.save(any(ListaContenidoPublica.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente("contenido-1");

        // When
        String resultado = publicListCreationService.crearListaPublica(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista pública creada exitosamente", resultado);

        verify(contentValidationUtil, times(1)).validarContenidoExistente("contenido-1");
        verify(listaContenidoPublicaRepository, times(1)).save(any(ListaContenidoPublica.class));
    }

    @Test
    @DisplayName("CA-06: Crear lista pública - Con múltiples contenidos")
    void testCrearListaPublica_MultipleContenidos() {
        // Given
        listaDTO.setContenidos(Arrays.asList("contenido-1", "contenido-2", "contenido-3", "contenido-4"));
        
        when(listaPublicaRepository.existsByNombre("Mi Lista Pública"))
            .thenReturn(false);
        when(listaPublicaRepository.save(any(ListaPublica.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPublicaRepository.save(any(ListaContenidoPublica.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        String resultado = publicListCreationService.crearListaPublica(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista pública creada exitosamente", resultado);

        verify(contentValidationUtil, times(4)).validarContenidoExistente(anyString());
        verify(listaContenidoPublicaRepository, times(4)).save(any(ListaContenidoPublica.class));
    }

    @Test
    @DisplayName("CA-07: Crear lista pública - Visibilidad false")
    void testCrearListaPublica_VisibilidadFalse() {
        // Given
        listaDTO.setVisibilidad(false);
        
        ListaPublica listaNoVisible = ListaPublica.builder()
            .idLista("lista-123")
            .nombre("Mi Lista Pública")
            .descripcion("Descripción de la lista")
            .idCreadorUsuario("user-123")
            .visibilidad(false)
            .build();
        
        when(listaPublicaRepository.existsByNombre("Mi Lista Pública"))
            .thenReturn(false);
        when(listaPublicaRepository.save(any(ListaPublica.class)))
            .thenReturn(listaNoVisible);
        when(listaContenidoPublicaRepository.save(any(ListaContenidoPublica.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        String resultado = publicListCreationService.crearListaPublica(listaDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Lista pública creada exitosamente", resultado);

        ArgumentCaptor<ListaPublica> listaCaptor = ArgumentCaptor.forClass(ListaPublica.class);
        verify(listaPublicaRepository, times(1)).save(listaCaptor.capture());
        
        ListaPublica listaSaved = listaCaptor.getValue();
        assertFalse(listaSaved.getVisibilidad());
    }

    @Test
    @DisplayName("CA-08: Crear lista pública - Verifica orden de operaciones")
    void testCrearListaPublica_OrdenOperaciones() {
        // Given
        when(listaPublicaRepository.existsByNombre("Mi Lista Pública"))
            .thenReturn(false);
        when(listaPublicaRepository.save(any(ListaPublica.class)))
            .thenReturn(listaGuardada);
        when(listaContenidoPublicaRepository.save(any(ListaContenidoPublica.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        doNothing().when(contentValidationUtil).validarContenidoExistente(anyString());

        // When
        publicListCreationService.crearListaPublica(listaDTO);

        // Then - Verificar orden de llamadas
        var inOrder = inOrder(listaPublicaRepository, contentValidationUtil, listaContenidoPublicaRepository);
        
        inOrder.verify(listaPublicaRepository).existsByNombre("Mi Lista Pública");
        inOrder.verify(contentValidationUtil).validarContenidoExistente("contenido-1");
        inOrder.verify(contentValidationUtil).validarContenidoExistente("contenido-2");
        inOrder.verify(listaPublicaRepository).save(any(ListaPublica.class));
        inOrder.verify(listaContenidoPublicaRepository, times(2)).save(any(ListaContenidoPublica.class));
    }

    @Test
    @DisplayName("CA-09: Constructor verifica dependencias")
    void testConstructor() {
        // Arrange & Act
        PublicListCreationService service = new PublicListCreationService(
            listaPublicaRepository,
            listaContenidoPublicaRepository,
            contentValidationUtil
        );

        // Assert
        assertNotNull(service);
    }
}
