package com.esimedia.features.auth.services;

import com.esimedia.features.auth.entity.Sesion;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.enums.EstadoSesion;
import com.esimedia.features.auth.repository.SesionRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TokenCleanupServiceTest {

    @Mock
    private SesionRepository sesionRepository;
    
    @Mock
    private UsuarioNormalRepository usuarioNormalRepository;
    
    @Mock
    private CreadorContenidoRepository creadorContenidoRepository;

    @InjectMocks
    private TokenCleanupService tokenCleanupService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteExpiredSessionsAndUnconfirmedUsers_WithExpiredSessions() {
        // Arrange
        Sesion sesionExpirada = new Sesion();
        List<Sesion> sesionesExpiradas = Arrays.asList(sesionExpirada);

        when(sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class))).thenReturn(sesionesExpiradas);
        when(usuarioNormalRepository.findAll()).thenReturn(Collections.emptyList());
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        tokenCleanupService.deleteExpiredSessionsAndUnconfirmedUsers();

        // Assert
        verify(sesionRepository).delete(sesionExpirada);
        verify(sesionRepository).findByEstadoAndFechaUltimaActividadBefore(eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class));
    }

    @Test
    void testDeleteExpiredSessionsAndUnconfirmedUsers_WithUnvalidatedUser() {
        // Arrange
        UsuarioNormal usuarioNoValidado = mock(UsuarioNormal.class);
        when(usuarioNoValidado.getIdUsuario()).thenReturn("user1");
        when(usuarioNoValidado.getAlias()).thenReturn("testuser");
        when(usuarioNoValidado.isConfirmado()).thenReturn(false);

        when(sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(usuarioNormalRepository.findAll()).thenReturn(Arrays.asList(usuarioNoValidado));
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());
        when(sesionRepository.countByIdUsuarioAndEstado("user1", EstadoSesion.ACTIVA)).thenReturn(0L);

        // Act
        tokenCleanupService.deleteExpiredSessionsAndUnconfirmedUsers();

        // Assert
        verify(usuarioNormalRepository).delete(usuarioNoValidado);
        verify(sesionRepository).countByIdUsuarioAndEstado("user1", EstadoSesion.ACTIVA);
    }

    @Test
    void testDeleteExpiredSessionsAndUnconfirmedUsers_WithValidatedUser() {
        // Arrange
        UsuarioNormal usuarioValidado = mock(UsuarioNormal.class);
        when(usuarioValidado.getIdUsuario()).thenReturn("user1");
        when(usuarioValidado.getAlias()).thenReturn("validuser");
        when(usuarioValidado.isConfirmado()).thenReturn(true);

        when(sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(usuarioNormalRepository.findAll()).thenReturn(Arrays.asList(usuarioValidado));
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        tokenCleanupService.deleteExpiredSessionsAndUnconfirmedUsers();

        // Assert
        verify(usuarioNormalRepository, never()).delete(usuarioValidado);
        verify(sesionRepository, never()).countByIdUsuarioAndEstado(any(), any());
    }

    @Test
    void testDeleteExpiredSessionsAndUnconfirmedUsers_WithUnvalidatedUserWithActiveSessions() {
        // Arrange
        UsuarioNormal usuarioNoValidado = mock(UsuarioNormal.class);
        when(usuarioNoValidado.getIdUsuario()).thenReturn("user1");
        when(usuarioNoValidado.getAlias()).thenReturn("userWithSessions");
        when(usuarioNoValidado.isConfirmado()).thenReturn(false);

        when(sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(usuarioNormalRepository.findAll()).thenReturn(Arrays.asList(usuarioNoValidado));
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());
        when(sesionRepository.countByIdUsuarioAndEstado("user1", EstadoSesion.ACTIVA)).thenReturn(1L);

        // Act
        tokenCleanupService.deleteExpiredSessionsAndUnconfirmedUsers();

        // Assert
        verify(usuarioNormalRepository, never()).delete(usuarioNoValidado);
        verify(sesionRepository).countByIdUsuarioAndEstado("user1", EstadoSesion.ACTIVA);
    }

    @Test
    void testDeleteExpiredSessionsAndUnconfirmedUsers_WithUnvalidatedCreator() {
        // Arrange
        CreadorContenido creadorNoValidado = mock(CreadorContenido.class);
        when(creadorNoValidado.getIdUsuario()).thenReturn("creator1");
        when(creadorNoValidado.getAlias()).thenReturn("testcreator");
        when(creadorNoValidado.isValidado()).thenReturn(false);

        when(sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(usuarioNormalRepository.findAll()).thenReturn(Collections.emptyList());
        when(creadorContenidoRepository.findAll()).thenReturn(Arrays.asList(creadorNoValidado));
        when(sesionRepository.countByIdUsuarioAndEstado("creator1", EstadoSesion.ACTIVA)).thenReturn(0L);

        // Act
        tokenCleanupService.deleteExpiredSessionsAndUnconfirmedUsers();

        // Assert
        verify(creadorContenidoRepository).delete(creadorNoValidado);
        verify(sesionRepository).countByIdUsuarioAndEstado("creator1", EstadoSesion.ACTIVA);
    }

    @Test
    void testDeleteExpiredSessionsAndUnconfirmedUsers_WithValidatedCreator() {
        // Arrange
        CreadorContenido creadorValidado = mock(CreadorContenido.class);
        when(creadorValidado.getIdUsuario()).thenReturn("creator1");
        when(creadorValidado.getAlias()).thenReturn("validcreator");
        when(creadorValidado.isValidado()).thenReturn(true);

        when(sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(usuarioNormalRepository.findAll()).thenReturn(Collections.emptyList());
        when(creadorContenidoRepository.findAll()).thenReturn(Arrays.asList(creadorValidado));

        // Act
        tokenCleanupService.deleteExpiredSessionsAndUnconfirmedUsers();

        // Assert
        verify(creadorContenidoRepository, never()).delete(creadorValidado);
        verify(sesionRepository, never()).countByIdUsuarioAndEstado(any(), any());
    }

    @Test
    void testDeleteExpiredSessionsAndUnconfirmedUsers_NoSesionesNoUsuarios() {
        // Arrange
        when(sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(usuarioNormalRepository.findAll()).thenReturn(Collections.emptyList());
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        tokenCleanupService.deleteExpiredSessionsAndUnconfirmedUsers();

        // Assert
        verify(sesionRepository, never()).delete(any(Sesion.class));
        verify(usuarioNormalRepository, never()).delete(any(UsuarioNormal.class));
        verify(creadorContenidoRepository, never()).delete(any(CreadorContenido.class));
    }

    @Test
    void testDeleteExpiredSessionsAndUnconfirmedUsers_MixedScenario() {
        // Arrange
        Sesion sesionExpirada = new Sesion();

        UsuarioNormal usuarioNoValidado = mock(UsuarioNormal.class);
        when(usuarioNoValidado.getIdUsuario()).thenReturn("user1");
        when(usuarioNoValidado.getAlias()).thenReturn("userToDelete");
        when(usuarioNoValidado.isConfirmado()).thenReturn(false);

        CreadorContenido creadorValidado = mock(CreadorContenido.class);
        when(creadorValidado.getIdUsuario()).thenReturn("creator1");
        when(creadorValidado.getAlias()).thenReturn("validcreator");
        when(creadorValidado.isValidado()).thenReturn(true);

        when(sesionRepository.findByEstadoAndFechaUltimaActividadBefore(
            eq(EstadoSesion.EXPIRADA), any(LocalDateTime.class))).thenReturn(Arrays.asList(sesionExpirada));
        when(usuarioNormalRepository.findAll()).thenReturn(Arrays.asList(usuarioNoValidado));
        when(creadorContenidoRepository.findAll()).thenReturn(Arrays.asList(creadorValidado));
        when(sesionRepository.countByIdUsuarioAndEstado("user1", EstadoSesion.ACTIVA)).thenReturn(0L);

        // Act
        tokenCleanupService.deleteExpiredSessionsAndUnconfirmedUsers();

        // Assert
        verify(sesionRepository).delete(sesionExpirada);
        verify(usuarioNormalRepository).delete(usuarioNoValidado);
        verify(creadorContenidoRepository, never()).delete(creadorValidado);
    }
}