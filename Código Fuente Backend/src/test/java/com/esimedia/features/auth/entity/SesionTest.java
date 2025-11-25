package com.esimedia.features.auth.entity;

import org.junit.jupiter.api.Test;

import com.esimedia.features.auth.enums.EstadoSesion;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SesionTest {

    @Test
    void testConstructoresYGettersSetters() {
        Sesion sesion = new Sesion();
        assertNotNull(sesion.getFechaInicio());
        assertNotNull(sesion.getFechaUltimaActividad());
        assertEquals(EstadoSesion.ACTIVA, sesion.getEstado());

        sesion.setIdSesion("s1");
        sesion.setIdUsuario("u1");
        sesion.setIpCliente("127.0.0.1");
        sesion.setJwtTokenId("jwt123");
        sesion.setResetToken("reset");
        sesion.setResetTokenExpiry(LocalDateTime.now().plusDays(1));
        sesion.setEstado(EstadoSesion.BLOQUEADA);

        assertEquals("s1", sesion.getIdSesion());
        assertEquals("u1", sesion.getIdUsuario());
        assertEquals("127.0.0.1", sesion.getIpCliente());
        assertEquals("jwt123", sesion.getJwtTokenId());
        assertEquals("reset", sesion.getResetToken());
        assertNotNull(sesion.getResetTokenExpiry());
        assertEquals(EstadoSesion.BLOQUEADA, sesion.getEstado());
    }

    @Test
    void testMetodosUtilidad() {
        Sesion sesion = new Sesion();

        // actualizarUltimaActividad
        LocalDateTime antes = sesion.getFechaUltimaActividad();
        sesion.actualizarUltimaActividad();
        assertTrue(sesion.getFechaUltimaActividad().isAfter(antes) || 
                   sesion.getFechaUltimaActividad().isEqual(antes));

        // expirarSesion
        sesion.expirarSesion();
        assertEquals(EstadoSesion.EXPIRADA, sesion.getEstado());

        // bloquearSesion
        sesion.bloquearSesion();
        assertEquals(EstadoSesion.BLOQUEADA, sesion.getEstado());

        // isActiva
        sesion.setEstado(EstadoSesion.ACTIVA);
        assertTrue(sesion.isActiva());
        sesion.setEstado(EstadoSesion.EXPIRADA);
        assertFalse(sesion.isActiva());
    }

    @Test
    void testEqualsHashCodeToStringCanEqual() {
        Sesion sesion1 = new Sesion();
        Sesion sesion2 = new Sesion();

        // Diferenciar fechas para que equals no falle
        sesion2.setFechaInicio(sesion1.getFechaInicio().plusSeconds(1));
        sesion2.setFechaUltimaActividad(sesion1.getFechaUltimaActividad().plusSeconds(1));

        assertNotEquals(sesion1, sesion2);
        assertNotEquals(sesion1.hashCode(), sesion2.hashCode());

        // Probar toString
        assertTrue(sesion1.toString().contains("Sesion"));

        // Probar canEqual
        assertTrue(sesion1.canEqual(new Sesion()));
        assertFalse(sesion1.canEqual("otroObjeto"));
    }
}
