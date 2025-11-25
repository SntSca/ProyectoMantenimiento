package com.esimedia.features.auth.entity;

import com.esimedia.features.auth.enums.TipoVerificacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CodigoVerificacionTest {

    @Test
    @DisplayName("Debe crear un código de verificación con constructor parametrizado")
    void testConstructorParametrizado() {
        // Given
        String userId = "user123";
        String codigo = "ABC123";
        TipoVerificacion tipo = TipoVerificacion.LOGIN_EMAIL;
        int minutosExpiracion = 15;

        // When
        CodigoVerificacion codigoVerificacion = new CodigoVerificacion(userId, codigo, tipo, minutosExpiracion);

        // Then
        assertNotNull(codigoVerificacion);
        assertEquals(userId, codigoVerificacion.getUserId());
        assertEquals(codigo, codigoVerificacion.getCodigo());
        assertEquals(tipo, codigoVerificacion.getTipo());
        assertNotNull(codigoVerificacion.getFechaCreacion());
        assertNotNull(codigoVerificacion.getFechaExpiracion());
        assertFalse(codigoVerificacion.isUsado());
        assertTrue(codigoVerificacion.getFechaExpiracion().isAfter(codigoVerificacion.getFechaCreacion()));
    }

    @Test
    @DisplayName("Debe crear un código de verificación con constructor vacío")
    void testConstructorVacio() {
        // When
        CodigoVerificacion codigoVerificacion = new CodigoVerificacion();

        // Then
        assertNotNull(codigoVerificacion);
    }

    @Test
    @DisplayName("Debe verificar que el código NO ha expirado cuando está dentro del tiempo límite")
    void testHasExpiradoFalse() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);

        // When
        boolean expirado = codigo.hasExpirado();

        // Then
        assertFalse(expirado);
    }

    @Test
    @DisplayName("Debe verificar que el código HA expirado cuando se establece fecha pasada")
    void testHasExpiradoTrue() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));

        // When
        boolean expirado = codigo.hasExpirado();

        // Then
        assertTrue(expirado);
    }

    @Test
    @DisplayName("Debe validar que el código es válido cuando no está usado y no ha expirado")
    void testIsValidoTrue() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);

        // When
        boolean valido = codigo.isValido();

        // Then
        assertTrue(valido);
    }

    @Test
    @DisplayName("Debe validar que el código NO es válido cuando está usado")
    void testIsValidoFalsePorUsado() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo.setUsado(true);

        // When
        boolean valido = codigo.isValido();

        // Then
        assertFalse(valido);
    }

    @Test
    @DisplayName("Debe validar que el código NO es válido cuando ha expirado")
    void testIsValidoFalsePorExpirado() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));

        // When
        boolean valido = codigo.isValido();

        // Then
        assertFalse(valido);
    }

    @Test
    @DisplayName("Debe validar que el código NO es válido cuando está usado Y expirado")
    void testIsValidoFalsePorUsadoYExpirado() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo.setUsado(true);
        codigo.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));

        // When
        boolean valido = codigo.isValido();

        // Then
        assertFalse(valido);
    }

    @Test
    @DisplayName("Debe establecer y obtener el ID correctamente")
    void testSetGetId() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion();
        String id = "507f1f77bcf86cd799439011";

        // When
        codigo.setId(id);

        // Then
        assertEquals(id, codigo.getId());
    }

    @Test
    @DisplayName("Debe establecer y obtener userId correctamente")
    void testSetGetUserId() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion();
        String userId = "user456";

        // When
        codigo.setUserId(userId);

        // Then
        assertEquals(userId, codigo.getUserId());
    }

    @Test
    @DisplayName("Debe establecer y obtener codigo correctamente")
    void testSetGetCodigo() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion();
        String codigoStr = "ABCD1234";

        // When
        codigo.setCodigo(codigoStr);

        // Then
        assertEquals(codigoStr, codigo.getCodigo());
    }

    @Test
    @DisplayName("Debe establecer y obtener fechaCreacion correctamente")
    void testSetGetFechaCreacion() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion();
        LocalDateTime fecha = LocalDateTime.now();

        // When
        codigo.setFechaCreacion(fecha);

        // Then
        assertEquals(fecha, codigo.getFechaCreacion());
    }

    @Test
    @DisplayName("Debe establecer y obtener fechaExpiracion correctamente")
    void testSetGetFechaExpiracion() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion();
        LocalDateTime fecha = LocalDateTime.now().plusMinutes(30);

        // When
        codigo.setFechaExpiracion(fecha);

        // Then
        assertEquals(fecha, codigo.getFechaExpiracion());
    }

    @Test
    @DisplayName("Debe establecer y obtener usado correctamente")
    void testSetIsUsado() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion();

        // When
        codigo.setUsado(true);

        // Then
        assertTrue(codigo.isUsado());

        // When
        codigo.setUsado(false);

        // Then
        assertFalse(codigo.isUsado());
    }

    @Test
    @DisplayName("Debe validar equals cuando los objetos son iguales")
    void testEqualsTrue() {
        // Given
        CodigoVerificacion codigo1 = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo1.setId("id123");

        CodigoVerificacion codigo2 = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo2.setId("id123");
        codigo2.setFechaCreacion(codigo1.getFechaCreacion());
        codigo2.setFechaExpiracion(codigo1.getFechaExpiracion());

        // When & Then
        assertEquals(codigo1, codigo2);
    }

    @Test
    @DisplayName("Debe validar equals cuando los objetos son diferentes")
    void testEqualsFalse() {
        // Given
        CodigoVerificacion codigo1 = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        CodigoVerificacion codigo2 = new CodigoVerificacion("user2", "789012", TipoVerificacion.LOGIN_EMAIL, 15);

        // When & Then
        assertNotEquals(codigo1, codigo2);
    }

    @Test
    @DisplayName("Debe validar equals con el mismo objeto")
    void testEqualsMismoObjeto() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);

        // When & Then
        assertEquals(codigo, codigo);
    }

    @Test
    @DisplayName("Debe validar equals con null")
    void testEqualsNull() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);

        // When & Then
        assertNotEquals(null, codigo);
    }

    @Test
    @DisplayName("Debe validar equals con objeto de diferente clase")
    void testEqualsDiferenteClase() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        String otroObjeto = "No soy un CodigoVerificacion";

        // When & Then
        assertNotEquals(codigo, otroObjeto);
    }

    @Test
    @DisplayName("Debe generar el mismo hashCode para objetos iguales")
    void testHashCodeIguales() {
        // Given
        CodigoVerificacion codigo1 = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo1.setId("id123");

        CodigoVerificacion codigo2 = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo2.setId("id123");
        codigo2.setFechaCreacion(codigo1.getFechaCreacion());
        codigo2.setFechaExpiracion(codigo1.getFechaExpiracion());

        // When & Then
        assertEquals(codigo1.hashCode(), codigo2.hashCode());
    }

    @Test
    @DisplayName("Debe generar hashCode diferente para objetos diferentes")
    void testHashCodeDiferentes() {
        // Given
        CodigoVerificacion codigo1 = new CodigoVerificacion("user1", "123456", TipoVerificacion.LOGIN_EMAIL, 15);
        CodigoVerificacion codigo2 = new CodigoVerificacion("user2", "789012", TipoVerificacion.LOGIN_EMAIL, 30);

        // When & Then
        assertNotEquals(codigo1.hashCode(), codigo2.hashCode());
    }

    @Test
    @DisplayName("Debe generar toString con todos los campos")
    void testToString() {
        // Given
        CodigoVerificacion codigo = new CodigoVerificacion("user123", "ABC456", TipoVerificacion.LOGIN_EMAIL, 15);
        codigo.setId("id789");

        // When
        String resultado = codigo.toString();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.contains("user123"));
        assertTrue(resultado.contains("ABC456"));
        assertTrue(resultado.contains("LOGIN_EMAIL"));
        assertTrue(resultado.contains("id789"));
    }
}