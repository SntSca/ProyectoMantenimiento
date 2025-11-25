package com.esimedia.features.auth.dto;

import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseDTOTest {

    @Test
    void testGettersAndSetters() {
        UserResponseDTO user = new UserResponseDTO();

        user.setIdUsuario("u1");
        user.setNombre("Nombre");
        user.setApellidos("Apellido");
        user.setEmail("email@example.com");
        user.setAlias("alias");
        user.setFotoPerfil("foto.png");
        Date now = new Date();
        user.setFechaRegistro(now);
        user.setTwoFactorEnabled(true);
        user.setThirdFactorEnabled(false);
        user.setRol(Rol.NORMAL);

        user.setFlagVIP(true);
        user.setFechaNacimiento(now);
        user.setBloqueadoUsuarioNormal(false);
        user.setConfirmado(true);

        user.setAliasCreador("aliasC");
        user.setDescripcion("descripcion");
        user.setBloqueadoCreador(false);
        user.setEspecialidad("especialidad");
        user.setTipoContenido(TipoContenido.VIDEO);
        user.setValidado(true);

        user.setDepartamento("dep");

        assertEquals("u1", user.getIdUsuario());
        assertEquals("Nombre", user.getNombre());
        assertEquals("Apellido", user.getApellidos());
        assertEquals("email@example.com", user.getEmail());
        assertEquals("alias", user.getAlias());
        assertEquals("foto.png", user.getFotoPerfil());
        assertEquals(now, user.getFechaRegistro());
        assertTrue(user.isTwoFactorEnabled());
        assertFalse(user.isThirdFactorEnabled());
        assertEquals(Rol.NORMAL, user.getRol());

        assertTrue(user.getFlagVIP());
        assertEquals(now, user.getFechaNacimiento());
        assertFalse(user.getBloqueadoUsuarioNormal());
        assertTrue(user.getConfirmado());

        assertEquals("aliasC", user.getAliasCreador());
        assertEquals("descripcion", user.getDescripcion());
        assertFalse(user.getBloqueadoCreador());
        assertEquals("especialidad", user.getEspecialidad());
        assertEquals(TipoContenido.VIDEO, user.getTipoContenido());
        assertTrue(user.getValidado());

        assertEquals("dep", user.getDepartamento());
    }

    @Test
    void testEqualsHashCodeToString() {
        UserResponseDTO user1 = new UserResponseDTO();
        UserResponseDTO user2 = new UserResponseDTO();

        // Igualdad por referencia
        assertEquals(user1, user1);

        // Diferentes objetos con campos vacíos -> iguales
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());

        // toString no nulo
        assertNotNull(user1.toString());

        // canEqual con null y otro tipo
        assertFalse(user1.equals(null));
        assertFalse(user1.equals("un string"));

        // Modificar un campo para verificar desigualdad
        user2.setIdUsuario("u1");
        assertNotEquals(user1, user2);
    }
}