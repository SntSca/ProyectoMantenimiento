package com.esimedia.features.lists.services;

import com.esimedia.features.lists.dto.ListaUpdateDTO;
import com.esimedia.features.lists.dto.ListaUpdateFieldsPrivadasDTO;
import com.esimedia.features.lists.entity.ListaPrivada;
import com.esimedia.features.lists.repository.ListaContenidoPrivadaRepository;
import com.esimedia.features.lists.repository.ListaPrivadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateListUpdateServiceTest {

    @Mock
    private ListaPrivadaRepository listaPrivadaRepository;

    @Mock
    private ListaContenidoPrivadaRepository listaContenidoPrivadaRepository;

    @InjectMocks
    private PrivateListUpdateService privateListUpdateService;

    private String idLista;
    private String idUsuario;
    private String otroUsuario;

    @BeforeEach
    void setUp() {
        idLista = "lista123";
        idUsuario = "usuario123";
        otroUsuario = "otroUsuario";
    }

    @Test
    void testActualizarListaPrivada_Success() {
        // Arrange
        ListaPrivada listaPrivada = mock(ListaPrivada.class);
        when(listaPrivada.getIdCreadorUsuario()).thenReturn(idUsuario);
        
        ListaUpdateDTO updateDTO = mock(ListaUpdateDTO.class);
        when(updateDTO.getIdLista()).thenReturn(idLista);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.of(listaPrivada));
        when(listaPrivadaRepository.save(any(ListaPrivada.class))).thenReturn(listaPrivada);

        // Act
        String result = privateListUpdateService.actualizarListaPrivada(updateDTO, idUsuario);

        // Assert
        assertNotNull(result);
        assertEquals("Lista privada actualizada exitosamente", result);
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaPrivadaRepository, times(1)).save(any(ListaPrivada.class));
    }

    @Test
    void testActualizarListaPrivada_ListaNoEncontrada() {
        // Arrange
        ListaUpdateDTO updateDTO = mock(ListaUpdateDTO.class);
        when(updateDTO.getIdLista()).thenReturn(idLista);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> privateListUpdateService.actualizarListaPrivada(updateDTO, idUsuario));
        
        assertEquals("Lista privada no encontrada", exception.getReason());
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaPrivadaRepository, never()).save(any());
    }

    @Test
    void testActualizarListaPrivada_UsuarioNoAutorizado() {
        // Arrange
        ListaPrivada listaPrivada = mock(ListaPrivada.class);
        when(listaPrivada.getIdCreadorUsuario()).thenReturn(idUsuario);
        
        ListaUpdateDTO updateDTO = mock(ListaUpdateDTO.class);
        when(updateDTO.getIdLista()).thenReturn(idLista);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.of(listaPrivada));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> privateListUpdateService.actualizarListaPrivada(updateDTO, otroUsuario));
        
        assertEquals("No tienes permiso para actualizar esta lista", exception.getReason());
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaPrivadaRepository, never()).save(any());
    }

    @Test
    void testActualizarCamposListaPrivada_Success() {
        // Arrange
        ListaPrivada listaPrivada = mock(ListaPrivada.class);
        when(listaPrivada.getIdCreadorUsuario()).thenReturn(idUsuario);
        
        ListaUpdateFieldsPrivadasDTO updateFieldsDTO = mock(ListaUpdateFieldsPrivadasDTO.class);
        when(updateFieldsDTO.getIdLista()).thenReturn(idLista);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.of(listaPrivada));
        when(listaPrivadaRepository.save(any(ListaPrivada.class))).thenReturn(listaPrivada);

        // Act
        String result = privateListUpdateService.actualizarCamposListaPrivada(updateFieldsDTO, idUsuario);

        // Assert
        assertNotNull(result);
        assertEquals("Lista privada actualizada exitosamente", result);
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaPrivadaRepository, times(1)).save(any(ListaPrivada.class));
    }

    @Test
    void testActualizarCamposListaPrivada_ListaNoEncontrada() {
        // Arrange
        ListaUpdateFieldsPrivadasDTO updateFieldsDTO = mock(ListaUpdateFieldsPrivadasDTO.class);
        when(updateFieldsDTO.getIdLista()).thenReturn(idLista);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> privateListUpdateService.actualizarCamposListaPrivada(updateFieldsDTO, idUsuario));
        
        assertEquals("Lista no encontrada", exception.getReason());
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaPrivadaRepository, never()).save(any());
    }

    @Test
    void testActualizarCamposListaPrivada_UsuarioNoAutorizado() {
        // Arrange
        ListaPrivada listaPrivada = mock(ListaPrivada.class);
        when(listaPrivada.getIdCreadorUsuario()).thenReturn(idUsuario);
        
        ListaUpdateFieldsPrivadasDTO updateFieldsDTO = mock(ListaUpdateFieldsPrivadasDTO.class);
        when(updateFieldsDTO.getIdLista()).thenReturn(idLista);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.of(listaPrivada));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> privateListUpdateService.actualizarCamposListaPrivada(updateFieldsDTO, otroUsuario));
        
        assertEquals("No tienes permisos para modificar esta lista privada", exception.getReason());
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaPrivadaRepository, never()).save(any());
    }

    @Test
    void testEliminarListaPrivada_Success() {
        // Arrange
        ListaPrivada listaPrivada = mock(ListaPrivada.class);
        when(listaPrivada.getIdCreadorUsuario()).thenReturn(idUsuario);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.of(listaPrivada));

        // Act
        String result = privateListUpdateService.eliminarListaPrivada(idLista, idUsuario);

        // Assert
        assertNotNull(result);
        assertEquals("Lista privada eliminada exitosamente", result);
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaContenidoPrivadaRepository, times(1)).deleteByIdLista(idLista);
        verify(listaPrivadaRepository, times(1)).delete(listaPrivada);
    }

    @Test
    void testEliminarListaPrivada_ListaNoEncontrada() {
        // Arrange
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> privateListUpdateService.eliminarListaPrivada(idLista, idUsuario));
        
        assertEquals("Lista privada no encontrada", exception.getReason());
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaContenidoPrivadaRepository, never()).deleteByIdLista(any());
        verify(listaPrivadaRepository, never()).delete(any());
    }

    @Test
    void testEliminarListaPrivada_UsuarioNoAutorizado() {
        // Arrange
        ListaPrivada listaPrivada = mock(ListaPrivada.class);
        when(listaPrivada.getIdCreadorUsuario()).thenReturn(idUsuario);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.of(listaPrivada));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> privateListUpdateService.eliminarListaPrivada(idLista, otroUsuario));
        
        assertEquals("No tienes permiso para eliminar esta lista", exception.getReason());
        verify(listaPrivadaRepository, times(1)).findById(idLista);
        verify(listaContenidoPrivadaRepository, never()).deleteByIdLista(any());
        verify(listaPrivadaRepository, never()).delete(any());
    }

    @Test
    void testEliminarListaPrivada_EliminaContenidosPrimero() {
        // Arrange
        ListaPrivada listaPrivada = mock(ListaPrivada.class);
        when(listaPrivada.getIdCreadorUsuario()).thenReturn(idUsuario);
        when(listaPrivadaRepository.findById(idLista)).thenReturn(Optional.of(listaPrivada));

        // Act
        privateListUpdateService.eliminarListaPrivada(idLista, idUsuario);

        // Assert
        // Verificar que se elimina el contenido antes que la lista
        verify(listaContenidoPrivadaRepository, times(1)).deleteByIdLista(idLista);
        verify(listaPrivadaRepository, times(1)).delete(listaPrivada);
    }

    @Test
    void testConstructor() {
        // Arrange & Act
        PrivateListUpdateService service = new PrivateListUpdateService(
            listaPrivadaRepository,
            listaContenidoPrivadaRepository
        );

        // Assert
        assertNotNull(service);
    }
}
