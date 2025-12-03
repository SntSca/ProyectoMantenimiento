package com.esimedia.features.auth.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

class UsuarioNormalTest {
    
    @Test
    void testDefaultConstructor() {
        UsuarioNormal usuario = new UsuarioNormal();
        
        assertNull(usuario.getIdUsuario());
        assertNull(usuario.getNombre());
        assertNull(usuario.getApellidos());
        assertNull(usuario.getEmail());
        assertNull(usuario.getAlias());
        assertNull(usuario.getPassword());
        assertNull(usuario.getFechaNacimiento());
        assertFalse(usuario.isFlagVIP());
        assertFalse(usuario.isBloqueado());
        assertFalse(usuario.isConfirmado());
    }

    @Test
    void testBuilderWithAllFields() {
        Date fechaNacimiento = new Date();
        String fotoPerfil = "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...";
        
        UsuarioNormal usuario = UsuarioNormal.builder()
            .nombre("Juan")
            .apellidos("Perez")
            .email("juan@example.com")
            .alias("juanperez")
            .password("Password123!")
            .fechaNacimiento(fechaNacimiento)
            .flagVIP(true)
            .bloqueado(false)
            .confirmado(true)
            .fotoPerfil(fotoPerfil)
            .build();
        
        assertEquals("Juan", usuario.getNombre());
        assertEquals("Perez", usuario.getApellidos());
        assertEquals("juan@example.com", usuario.getEmail());
        assertEquals("juanperez", usuario.getAlias());
        assertEquals("Password123!", usuario.getPassword());
        assertEquals(fechaNacimiento, usuario.getFechaNacimiento());
        assertTrue(usuario.isFlagVIP());
        assertFalse(usuario.isBloqueado());
        assertTrue(usuario.isConfirmado());
        assertEquals(fotoPerfil, usuario.getFotoPerfil());
    }

    @Test
    void testBuilderWithDefaultValues() {
        UsuarioNormal usuario = UsuarioNormal.builder()
            .nombre("Maria")
            .apellidos("Garcia")
            .email("maria@example.com")
            .alias("mariagarcia")
            .password("Pass456!")
            .build();
        
        assertEquals("Maria", usuario.getNombre());
        assertEquals("Garcia", usuario.getApellidos());
        assertEquals("maria@example.com", usuario.getEmail());
        assertEquals("mariagarcia", usuario.getAlias());
        assertEquals("Pass456!", usuario.getPassword());
        assertNull(usuario.getFechaNacimiento());
        assertFalse(usuario.isFlagVIP());
        assertFalse(usuario.isBloqueado());
        assertFalse(usuario.isConfirmado());
    }

    @Test
    void testSetters() {
        UsuarioNormal usuario = new UsuarioNormal();
        Date fechaNacimiento = new Date();
        
        usuario.setFlagVIP(true);
        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setBloqueado(true);
        usuario.setConfirmado(true);
        
        assertTrue(usuario.isFlagVIP());
        assertEquals(fechaNacimiento, usuario.getFechaNacimiento());
        assertTrue(usuario.isBloqueado());
        assertTrue(usuario.isConfirmado());
    }

    @Test
    void testEqualsWithSameObject() {
        UsuarioNormal usuario = UsuarioNormal.builder()
            .nombre("Ana")
            .email("ana@example.com")
            .build();
        
        assertEquals(usuario, usuario);
    }

    @Test
    void testEqualsWithEqualObjects() {
        Date fechaNacimiento = new Date();
        
        UsuarioNormal usuario1 = UsuarioNormal.builder()
            .nombre("Carlos")
            .apellidos("Martinez")
            .email("carlos@example.com")
            .alias("carlosm")
            .password("Carlos123!")
            .fechaNacimiento(fechaNacimiento)
            .flagVIP(true)
            .bloqueado(false)
            .confirmado(true)
            .build();
        
        UsuarioNormal usuario2 = UsuarioNormal.builder()
            .nombre("Carlos")
            .apellidos("Martinez")
            .email("carlos@example.com")
            .alias("carlosm")
            .password("Carlos123!")
            .fechaNacimiento(fechaNacimiento)
            .flagVIP(true)
            .bloqueado(false)
            .confirmado(true)
            .build();
        
        assertEquals(usuario1, usuario2);
    }

    @Test
    void testEqualsWithDifferentObjects() {
        UsuarioNormal usuario1 = UsuarioNormal.builder()
            .nombre("Luis")
            .email("luis@example.com")
            .build();
        
        UsuarioNormal usuario2 = UsuarioNormal.builder()
            .nombre("Lucia")
            .email("lucia@example.com")
            .build();
        
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEqualsWithNull() {
        UsuarioNormal usuario = new UsuarioNormal();
        
        assertNotEquals(null, usuario);
    }

    @Test
    void testEqualsWithDifferentClass() {
        UsuarioNormal usuario = new UsuarioNormal();
        String otherObject = "test";
        
        assertNotEquals(usuario, otherObject);
    }

    @Test
    void testHashCodeConsistency() {
        Date fechaNacimiento = new Date();
        UsuarioNormal usuario = UsuarioNormal.builder()
            .nombre("Sofia")
            .apellidos("Ruiz")
            .email("sofia@example.com")
            .alias("sofiaruiz")
            .password("Sofia123!")
            .fechaNacimiento(fechaNacimiento)
            .build();
        
        int hashCode1 = usuario.hashCode();
        int hashCode2 = usuario.hashCode();
        
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeWithEqualObjects() {
        Date fechaNacimiento = new Date();
        
        UsuarioNormal usuario1 = UsuarioNormal.builder()
            .nombre("Diego")
            .apellidos("Sanchez")
            .email("diego@example.com")
            .alias("diegos")
            .password("Diego123!")
            .fechaNacimiento(fechaNacimiento)
            .flagVIP(false)
            .build();
        
        UsuarioNormal usuario2 = UsuarioNormal.builder()
            .nombre("Diego")
            .apellidos("Sanchez")
            .email("diego@example.com")
            .alias("diegos")
            .password("Diego123!")
            .fechaNacimiento(fechaNacimiento)
            .flagVIP(false)
            .build();
        
        assertEquals(usuario1.hashCode(), usuario2.hashCode());
    }

    @Test
    void testCanEqual() {
        UsuarioNormal usuario1 = new UsuarioNormal();
        UsuarioNormal usuario2 = new UsuarioNormal();

        assertEquals(true, usuario1.equals(usuario2));
    }

    @Test
    void testEqualsWithDifferentFlagVIP() {
        UsuarioNormal usuario1 = UsuarioNormal.builder()
            .nombre("Elena")
            .email("elena@example.com")
            .flagVIP(true)
            .build();
        
        UsuarioNormal usuario2 = UsuarioNormal.builder()
            .nombre("Elena")
            .email("elena@example.com")
            .flagVIP(false)
            .build();
        
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEqualsWithDifferentBloqueado() {
        UsuarioNormal usuario1 = UsuarioNormal.builder()
            .nombre("Miguel")
            .email("miguel@example.com")
            .bloqueado(true)
            .build();
        
        UsuarioNormal usuario2 = UsuarioNormal.builder()
            .nombre("Miguel")
            .email("miguel@example.com")
            .bloqueado(false)
            .build();
        
        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void testEqualsWithDifferentConfirmado() {
        UsuarioNormal usuario1 = UsuarioNormal.builder()
            .nombre("Laura")
            .email("laura@example.com")
            .confirmado(true)
            .build();
        
        UsuarioNormal usuario2 = UsuarioNormal.builder()
            .nombre("Laura")
            .email("laura@example.com")
            .confirmado(false)
            .build();
        
        assertNotEquals(usuario1, usuario2);
    }

}
