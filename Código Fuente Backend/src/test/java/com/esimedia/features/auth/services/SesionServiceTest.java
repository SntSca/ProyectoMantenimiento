package com.esimedia.features.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import com.esimedia.features.auth.entity.Sesion;
import com.esimedia.features.auth.enums.EstadoSesion;
import com.esimedia.features.auth.repository.SesionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SesionServiceTest {

    private SesionRepository repo;
    private SesionService service;

    @BeforeEach
    void setup() {
        repo = mock(SesionRepository.class);
        service = new SesionService(repo);
    }

    private Sesion buildSesion(String id, String user, String ip, String jwt, EstadoSesion estado) {
        Sesion s = new Sesion();
        s.setIdSesion(id);
        s.setIdUsuario(user);
        s.setIpCliente(ip);
        s.setJwtTokenId(jwt);
        s.setEstado(estado);
        s.setFechaInicio(LocalDateTime.now().minusHours(1));
        s.setFechaUltimaActividad(LocalDateTime.now().minusHours(1));
        return s;
    }

    // ========================= crearSesion =========================

    @Test
    void testCrearSesion_ok() {
        when(repo.findByIdUsuarioAndEstado("u1", EstadoSesion.ACTIVA))
            .thenReturn(new ArrayList<>());

        service.crearSesion("u1", "1.1.1.1", "jwt");

        verify(repo, times(1)).save(any(Sesion.class));
    }

    @Test
    void testCrearSesion_expiraSesionAntigua() {
        Sesion antigua = buildSesion("old", "u1", "1.1.1.1", "jwtOld", EstadoSesion.ACTIVA);
        antigua.setFechaInicio(LocalDateTime.now().minusDays(1));

        List<Sesion> activas = Arrays.asList(antigua, antigua, antigua, antigua, antigua);
        when(repo.findByIdUsuarioAndEstado("u1", EstadoSesion.ACTIVA)).thenReturn(activas);

        service.crearSesion("u1", "1.1.1.1", "jwt");

        verify(repo, times(1)).delete(antigua);
        verify(repo, times(1)).save(any(Sesion.class));
    }

    // ========================= validarSesion =========================

    @Test
    void testValidarSesion_noExiste() {
        when(repo.findByJwtTokenId("jwt")).thenReturn(Optional.empty());
        assertFalse(service.validarSesion("jwt", "1.1.1.1"));
    }

    @Test
    void testValidarSesion_inactiva() {
        Sesion s = buildSesion("s1", "u1", "1.1.1.1", "jwt", EstadoSesion.EXPIRADA);
        when(repo.findByJwtTokenId("jwt")).thenReturn(Optional.of(s));
        assertFalse(service.validarSesion("jwt", "1.1.1.1"));
    }

    @Test
    void testValidarSesion_valida() {
        Sesion s = buildSesion("s1", "u1", "1.1.1.1", "jwt", EstadoSesion.ACTIVA);
        when(repo.findByJwtTokenId("jwt")).thenReturn(Optional.of(s));

        assertTrue(service.validarSesion("jwt", "1.1.1.1"));
    }

    // ========================= eliminarSesionPorJwt =========================

    @Test
    void testEliminarSesionPorJwt_ok() {
        Sesion s = buildSesion("s1", "u1", "1.1.1.1", "jwt", EstadoSesion.ACTIVA);
        when(repo.findByJwtTokenId("jwt")).thenReturn(Optional.of(s));

        assertTrue(service.eliminarSesionPorJwt("jwt"));
        verify(repo).delete(s);
    }

    @Test
    void testEliminarSesionPorJwt_noExiste() {
        when(repo.findByJwtTokenId("jwt")).thenReturn(Optional.empty());
        assertFalse(service.eliminarSesionPorJwt("jwt"));
    }

    // ========================= eliminarSesionPorId =========================

    @Test
    void testEliminarSesionPorId_ok() {
        Sesion s = buildSesion("s1", "u1", "1.1.1.1", "jwt", EstadoSesion.ACTIVA);
        when(repo.findById("s1")).thenReturn(Optional.of(s));

        assertTrue(service.eliminarSesionPorId("s1"));
        verify(repo).delete(s);
    }

    @Test
    void testEliminarSesionPorId_noExiste() {
        when(repo.findById("x")).thenReturn(Optional.empty());
        assertFalse(service.eliminarSesionPorId("x"));
    }

    // ========================= expirarTodasSesionesActivas =========================

    @Test
    void testExpirarTodasSesionesActivas() {
        Sesion s1 = buildSesion("s1", "u1", "1.1.1.1", "jwt1", EstadoSesion.ACTIVA);
        Sesion s2 = buildSesion("s2", "u1", "1.1.1.1", "jwt2", EstadoSesion.ACTIVA);

        when(repo.findByEstado(EstadoSesion.ACTIVA)).thenReturn(Arrays.asList(s1, s2));

        service.expirarTodasSesionesActivas();

        verify(repo, times(2)).save(any(Sesion.class));
    }

    // ========================= rotarSesionPorFixation =========================

    @Test
    void testRotarSesionPorFixation() {
        when(repo.findByIdUsuarioAndEstado("u1", EstadoSesion.ACTIVA)).thenReturn(new ArrayList<>());

        service.rotarSesionPorFixation("u1", "1.1.1.1", "jwt");

        verify(repo).deleteByIdUsuario("u1");
        verify(repo).save(any(Sesion.class));
    }

    // ========================= limpiarSesionesExpiradasEInvalidas =========================

    @Test
    void testLimpiarSesionesExpiradasEInvalidas() {

        Sesion nullJwt = buildSesion("n1", "u1", "1.1.1.1", null, EstadoSesion.ACTIVA);
        Sesion expEstado = buildSesion("e1", "u1", "1.1.1.1", "jwt", EstadoSesion.EXPIRADA);
        Sesion expTiempo = buildSesion("t1", "u1", "1.1.1.1", "jwt", EstadoSesion.ACTIVA);
        expTiempo.setFechaUltimaActividad(LocalDateTime.now().minusHours(30));

        when(repo.findAll()).thenReturn(Arrays.asList(nullJwt, expEstado, expTiempo));
        when(repo.findByEstado(EstadoSesion.EXPIRADA)).thenReturn(new ArrayList<>(Arrays.asList(expEstado)));
        when(repo.findByEstado(EstadoSesion.BLOQUEADA)).thenReturn(new ArrayList<>());
        when(repo.findByFechaUltimaActividadBefore(any())).thenReturn(Arrays.asList(expTiempo));

        service.limpiarSesionesExpiradasEInvalidas();

        verify(repo).deleteAll(anyList());
    }

    // ========================= findById =========================

    @Test
    void testFindById() {
        Sesion s = buildSesion("s1", "u1", "1.1.1.1", "jwt", EstadoSesion.ACTIVA);
        when(repo.findById("s1")).thenReturn(Optional.of(s));

        assertTrue(service.findById("s1").isPresent());
    }

    // ========================= getSesionesActivasUsuario =========================

    @Test
    void testGetSesionesActivasUsuario() {
        when(repo.findByIdUsuarioAndEstado("u1", EstadoSesion.ACTIVA)).thenReturn(new ArrayList<>());
        assertNotNull(service.getSesionesActivasUsuario("u1"));
    }

    @Test
    void testLimpiarTodasLasSesiones() {
        // Arrange
        String userId = "user123";

        // Act
        service.limpiarTodasLasSesiones(userId);

        // Assert
        verify(repo, times(1)).deleteByIdUsuario(userId);
    }

    @Test
    void testValidarSesion_IpIncorrecta() {
        // Arrange
        Sesion sesion = new Sesion();
        sesion.setIdSesion("S1");
        sesion.setIpCliente("10.0.0.1");
        sesion.setEstado(EstadoSesion.ACTIVA);
        sesion.setFechaUltimaActividad(LocalDateTime.now());
        when(repo.findByJwtTokenId("jwt123"))
                .thenReturn(Optional.of(sesion));

        // Act
        boolean result = service.validarSesion("jwt123", "8.8.8.8");

        // Assert
        assertFalse(result);
        verify(repo, times(1)).findById("S1"); // eliminarSesion → busca por ID
    }

    @Test
    void testValidarSesion_ExpiradaPorInactividad() {
        // Arrange
        Sesion sesion = new Sesion();
        sesion.setIdSesion("S1");
        sesion.setIpCliente("10.0.0.1");
        sesion.setEstado(EstadoSesion.ACTIVA);

        // Última actividad hace 30 horas → expirada
        sesion.setFechaUltimaActividad(LocalDateTime.now().minusHours(30));

        when(repo.findByJwtTokenId("jwt123"))
                .thenReturn(Optional.of(sesion));

        // Act
        boolean result = service.validarSesion("jwt123", "10.0.0.1");

        // Assert
        assertFalse(result);
        verify(repo, times(1)).findById("S1");
    }

    @Test
    void testEliminarSesion_SesionExiste() {
        // Arrange
        Sesion sesion = new Sesion();
        sesion.setIdSesion("S1");

        when(repo.findById("S1")).thenReturn(Optional.of(sesion));

        // Act
        service.eliminarSesion("S1");

        // Assert
        verify(repo, times(1)).delete(sesion);
    }

    @Test
    void testEliminarSesion_SesionNoExiste() {
        // Arrange
        when(repo.findById("NOPE")).thenReturn(Optional.empty());

        // Act
        service.eliminarSesion("NOPE");

        // Assert
        verify(repo, never()).delete(any());
    }
}
