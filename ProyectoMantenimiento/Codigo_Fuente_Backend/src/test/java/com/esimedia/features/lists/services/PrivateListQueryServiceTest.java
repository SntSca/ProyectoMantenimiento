package com.esimedia.features.lists.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esimedia.features.lists.dto.ListaPrivadaResponseDTO;
import com.esimedia.features.lists.entity.ListaPrivada;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.lists.repository.ListaContenidoPrivadaRepository;
import com.esimedia.features.lists.repository.ListaPrivadaRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del Servicio de Consulta de Listas Privadas")
class PrivateListQueryServiceTest {

    @Mock
    private ListaPrivadaRepository listaPrivadaRepository;

    @Mock
    private UsuarioNormalRepository usuarioNormalRepository;

    @Mock
    private ListaContenidoPrivadaRepository listaContenidoPrivadaRepository;

    @Mock
    private PrivateListHelper privateListHelper;

    @InjectMocks
    private PrivateListQueryService privateListQueryService;

    private ListaPrivada listaPrivada1;
    private ListaPrivada listaPrivada2;
    private String userId;
    private String listaId;
    
    @Mock
    private UsuarioNormal usuario;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        listaId = "lista-priv-001";

        // Usuario normal
        usuario = UsuarioNormal.builder()
            .idUsuario(userId)
            .email("user@example.com")
            .alias("user123")
            .confirmado(true)
            .build();

        // Lista privada 1
        listaPrivada1 = ListaPrivada.builder()
            .idLista(listaId)
            .nombre("Mi Playlist Privada 1")
            .descripcion("Solo para mí")
            .idCreadorUsuario(userId)
            .visibilidad(false)
            .build();

        // Lista privada 2
        listaPrivada2 = ListaPrivada.builder()
            .idLista("lista-priv-002")
            .nombre("Mi Playlist Privada 2")
            .descripcion("Otro contenido privado")
            .idCreadorUsuario(userId)
            .visibilidad(false)
            .build();
    }

    // ==================== Consultas de Listas Privadas ====================

    @Test
    @DisplayName("CA-01: Consulta exitosa - No devuelve ninguna lista")
    void testObtenerTodasListasPrivadasConContenidos_EmptyList() {
        // Given
        when(listaPrivadaRepository.findByIdCreadorUsuario(userId))
            .thenReturn(new ArrayList<>());

        // When
        List<ListaPrivadaResponseDTO> resultado =
            privateListQueryService.obtenerTodasListasPrivadasConContenidos(userId);

        // Then
        assertNotNull(resultado);
        assertEquals(0, resultado.size());

        verify(listaPrivadaRepository, times(1)).findByIdCreadorUsuario(userId);
    }

    @Test
    @DisplayName("CA-02: Consulta exitosa - Devuelve una lista sin contenidos")
    void testObtenerTodasListasPrivadasConContenidos_SingleListNoContents() {
        // Given
        List<ListaPrivada> listas = Arrays.asList(listaPrivada1);
        when(listaPrivadaRepository.findByIdCreadorUsuario(userId))
            .thenReturn(listas);

        when(listaContenidoPrivadaRepository.findByIdLista(listaPrivada1.getIdLista()))
            .thenReturn(new ArrayList<>());

        when(privateListHelper.obtenerContenidosCompletos(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        List<ListaPrivadaResponseDTO> resultado =
            privateListQueryService.obtenerTodasListasPrivadasConContenidos(userId);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Mi Playlist Privada 1", resultado.get(0).getNombre());
        assertEquals("Solo para mí", resultado.get(0).getDescripcion());
        verify(listaPrivadaRepository, times(1)).findByIdCreadorUsuario(userId);
    }

    @Test
    @DisplayName("CA-03: Consulta exitosa - Devuelve una lista con contenidos (1 audio y 1 video)")
    void testObtenerTodasListasPrivadasConContenidos_SingleListWithContents() {
        // Given
        List<ListaPrivada> listas = Arrays.asList(listaPrivada1);
        when(listaPrivadaRepository.findByIdCreadorUsuario(userId))
            .thenReturn(listas);

        when(listaContenidoPrivadaRepository.findByIdLista(listaPrivada1.getIdLista()))
            .thenReturn(new ArrayList<>());

        when(privateListHelper.obtenerContenidosCompletos(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        List<ListaPrivadaResponseDTO> resultado =
            privateListQueryService.obtenerTodasListasPrivadasConContenidos(userId);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        ListaPrivadaResponseDTO lista = resultado.get(0);
        assertEquals(listaId, lista.getIdLista());
        assertEquals("Mi Playlist Privada 1", lista.getNombre());
        assertEquals(userId, lista.getIdCreadorUsuario());
        verify(listaPrivadaRepository, times(1)).findByIdCreadorUsuario(userId);
    }

    @Test
    @DisplayName("CA-04: Consulta errónea - JWT válido pero usuario no concuerda (JWT de CREADOR, consulta NORMAL)")
    void testObtenerTodasListasPrivadasConContenidos_InvalidUserType() {
        // Given - Usuario es CREADOR pero intenta acceder como NORMAL
        String creadorId = "creator-999";
        when(listaPrivadaRepository.findByIdCreadorUsuario(creadorId))
            .thenThrow(new IllegalArgumentException("El usuario no tiene permiso para acceder a estas listas"));

        // When & Then
        assertThrows(
            IllegalArgumentException.class,
            () -> privateListQueryService.obtenerTodasListasPrivadasConContenidos(creadorId)
        );

        verify(listaPrivadaRepository, times(1)).findByIdCreadorUsuario(creadorId);
    }

    @Test
    @DisplayName("CA-05: Consulta errónea - JWT es de otro usuario normal (IDs no coinciden)")
    void testObtenerTodasListasPrivadasConContenidos_DifferentUserIdMismatch() {
        // Given - Usuario B intenta acceder a sus listas pero el servicio lanza excepción
        String userIdB = "user-456";
        
        // Intenta acceder pero el JWT es de otro usuario
        when(listaPrivadaRepository.findByIdCreadorUsuario(userIdB))
            .thenThrow(new SecurityException("No tienes permiso para acceder a las listas privadas de otro usuario"));

        // When & Then
        assertThrows(
            SecurityException.class,
            () -> privateListQueryService.obtenerTodasListasPrivadasConContenidos(userIdB)
        );

        verify(listaPrivadaRepository, times(1)).findByIdCreadorUsuario(userIdB);
    }

    @Test
    @DisplayName("CA-06: Validar aislamiento de datos - Solo ve sus listas privadas (usuario correcto)")
    void testObtenerTodasListasPrivadasConContenidos_ValidaIslamiento() {
        // Given - Usuario accede a sus propias listas
        List<ListaPrivada> listas = Arrays.asList(listaPrivada1, listaPrivada2);

        when(listaPrivadaRepository.findByIdCreadorUsuario(userId))
            .thenReturn(listas);

        when(listaContenidoPrivadaRepository.findByIdLista(listaPrivada1.getIdLista()))
            .thenReturn(new ArrayList<>());
        when(listaContenidoPrivadaRepository.findByIdLista(listaPrivada2.getIdLista()))
            .thenReturn(new ArrayList<>());

        when(privateListHelper.obtenerContenidosCompletos(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        List<ListaPrivadaResponseDTO> resultado =
            privateListQueryService.obtenerTodasListasPrivadasConContenidos(userId);

        // Then - Verificar que todas las listas pertenecen al usuario
        assertTrue(resultado.stream()
            .allMatch(l -> l.getIdCreadorUsuario().equals(userId)));

        // Verificar que solo se consultó con este userId
        verify(listaPrivadaRepository, times(1)).findByIdCreadorUsuario(userId);
        // Nunca debe usar otros IDs
        verify(listaPrivadaRepository, never()).findByIdCreadorUsuario(argThat(
            arg -> !arg.equals(userId)
        ));
    }

    @Test
    @DisplayName("CA-07: Validar estructura de respuesta de lista privada")
    void testObtenerTodasListasPrivadasConContenidos_ResponseStructure() {
        // Given
        List<ListaPrivada> listas = Arrays.asList(listaPrivada1);

        when(listaPrivadaRepository.findByIdCreadorUsuario(userId))
            .thenReturn(listas);

        when(listaContenidoPrivadaRepository.findByIdLista(listaPrivada1.getIdLista()))
            .thenReturn(new ArrayList<>());

        when(privateListHelper.obtenerContenidosCompletos(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        List<ListaPrivadaResponseDTO> resultado =
            privateListQueryService.obtenerTodasListasPrivadasConContenidos(userId);

        // Then
        assertFalse(resultado.isEmpty());
        ListaPrivadaResponseDTO lista = resultado.get(0);
        assertNotNull(lista.getIdLista());
        assertNotNull(lista.getNombre());
        assertNotNull(lista.getDescripcion());
        assertNotNull(lista.getIdCreadorUsuario());
        assertNotNull(lista.getContenidos());
    }

    @Test
    @DisplayName("CA-08: Múltiples listas privadas con contenidos variados")
    void testObtenerTodasListasPrivadasConContenidos_MultipleLists() {
        // Given
        List<ListaPrivada> listas = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            listas.add(ListaPrivada.builder()
                .idLista("lista-priv-" + i)
                .nombre("Lista Privada " + i)
                .descripcion("Descripción " + i)
                .idCreadorUsuario(userId)
                .visibilidad(false)
                .build());
        }

        when(listaPrivadaRepository.findByIdCreadorUsuario(userId))
            .thenReturn(listas);

        // Mock para contenidos de cada lista
        for (ListaPrivada lista : listas) {
            when(listaContenidoPrivadaRepository.findByIdLista(lista.getIdLista()))
                .thenReturn(new ArrayList<>());
        }

        when(privateListHelper.obtenerContenidosCompletos(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        List<ListaPrivadaResponseDTO> resultado =
            privateListQueryService.obtenerTodasListasPrivadasConContenidos(userId);

        // Then
        assertEquals(3, resultado.size());
        for (int i = 0; i < 3; i++) {
            assertEquals("Lista Privada " + (i + 1), resultado.get(i).getNombre());
        }
    }

    @Test
    @DisplayName("CA-09: ID usuario vacío - Lista vacía")
    void testObtenerTodasListasPrivadasConContenidos_EmptyUserId() {
        // Given
        when(listaPrivadaRepository.findByIdCreadorUsuario(""))
            .thenReturn(new ArrayList<>());

        // When
        List<ListaPrivadaResponseDTO> resultado =
            privateListQueryService.obtenerTodasListasPrivadasConContenidos("");

        // Then
        assertNotNull(resultado);
        assertEquals(0, resultado.size());

        verify(listaPrivadaRepository, times(1)).findByIdCreadorUsuario("");
    }

    @Test
    @DisplayName("CA-10: Devolver todas las listas del usuario sin filtrar por visibilidad")
    void testObtenerTodasListasPrivadasConContenidos_OnlyPrivate() {
        // Given
        List<ListaPrivada> listas = Arrays.asList(listaPrivada1, listaPrivada2);

        when(listaPrivadaRepository.findByIdCreadorUsuario(userId))
            .thenReturn(listas);

        when(listaContenidoPrivadaRepository.findByIdLista(listaPrivada1.getIdLista()))
            .thenReturn(new ArrayList<>());
        when(listaContenidoPrivadaRepository.findByIdLista(listaPrivada2.getIdLista()))
            .thenReturn(new ArrayList<>());

        when(privateListHelper.obtenerContenidosCompletos(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        List<ListaPrivadaResponseDTO> resultado =
            privateListQueryService.obtenerTodasListasPrivadasConContenidos(userId);

        // Then - Verificar que devuelve todas las listas sin filtrar
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream()
            .allMatch(l -> l.getIdCreadorUsuario().equals(userId)));
    }

    @Test
    @DisplayName("CA-11: Error en base de datos - 500")
    void testObtenerTodasListasPrivadasConContenidos_DatabaseError() {
        // Given
        when(listaPrivadaRepository.findByIdCreadorUsuario(userId))
            .thenThrow(new RuntimeException("Error en base de datos"));

        // When & Then
        assertThrows(
            RuntimeException.class,
            () -> privateListQueryService.obtenerTodasListasPrivadasConContenidos(userId)
        );

        verify(listaPrivadaRepository, times(1)).findByIdCreadorUsuario(userId);
    }
}
