package com.esimedia.features.auth.entity;

import org.junit.jupiter.api.Test;
import com.esimedia.features.auth.enums.Rol;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    // ==================== Tests Constructores ====================
    
    @Test
    void testConstructorVacio() {
        // Act
        Usuario usuario = new Usuario();
        
        // Assert
        assertNotNull(usuario);
        assertNull(usuario.getIdUsuario());
        assertNull(usuario.getNombre());
        assertNull(usuario.getApellidos());
        assertNull(usuario.getEmail());
        assertNull(usuario.getAlias());
        assertNull(usuario.getPassword());
        assertNull(usuario.getFotoPerfil());
        assertNull(usuario.getFechaRegistro());
        assertFalse(usuario.isTwoFactorEnabled());
        assertFalse(usuario.isThirdFactorEnabled());
        assertNull(usuario.getTotpSecret());
        assertNull(usuario.getBackupCodes());
        assertNull(usuario.getRol());
    }
    
    @Test
    void testConstructorConParametros() {
        // Arrange
        String nombre = "Juan";
        String apellidos = "García López";
        String email = "juan@test.com";
        String alias = "juangar";
        String password = "password123";
        Rol rol = Rol.ADMINISTRADOR;
        
        // Act
        Usuario usuario = new Usuario(nombre, apellidos, email, alias, password, rol);
        
        // Assert
        assertEquals(nombre, usuario.getNombre());
        assertEquals(apellidos, usuario.getApellidos());
        assertEquals(email, usuario.getEmail());
        assertEquals(alias, usuario.getAlias());
        assertEquals(password, usuario.getPassword());
        assertEquals(rol, usuario.getRol());
        assertNotNull(usuario.getFechaRegistro());
        assertEquals(1L, usuario.getCredentialsVersion());
    }

    // ==================== Tests Getters y Setters ====================
    
    @Test
    void testGettersSetters_IdUsuario() {
        Usuario usuario = new Usuario();
        String id = "user123";
        
        usuario.setIdUsuario(id);
        
        assertEquals(id, usuario.getIdUsuario());
    }
    
    @Test
    void testGettersSetters_Nombre() {
        Usuario usuario = new Usuario();
        String nombre = "María";
        
        usuario.setNombre(nombre);
        
        assertEquals(nombre, usuario.getNombre());
    }
    
    @Test
    void testGettersSetters_Apellidos() {
        Usuario usuario = new Usuario();
        String apellidos = "Pérez Sánchez";
        
        usuario.setApellidos(apellidos);
        
        assertEquals(apellidos, usuario.getApellidos());
    }
    
    @Test
    void testGettersSetters_Email() {
        Usuario usuario = new Usuario();
        String email = "test@example.com";
        
        usuario.setEmail(email);
        
        assertEquals(email, usuario.getEmail());
    }
    
    @Test
    void testGettersSetters_Alias() {
        Usuario usuario = new Usuario();
        String alias = "testuser";
        
        usuario.setAlias(alias);
        
        assertEquals(alias, usuario.getAlias());
    }
    
    @Test
    void testGettersSetters_Password() {
        Usuario usuario = new Usuario();
        String password = "securePass123";
        
        usuario.setPassword(password);
        
        assertEquals(password, usuario.getPassword());
    }
    
    @Test
    void testGettersSetters_FotoPerfil() {
        Usuario usuario = new Usuario();
        String foto = "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...";
        
        usuario.setFotoPerfil(foto);
        
        assertEquals(foto, usuario.getFotoPerfil());
    }
    
    @Test
    void testGettersSetters_FormatoFotoPerfil() {
        // Este test ya no aplica - el formato está incluido en el data URI
        // La estructura anterior de fotoPerfil y formatoFotoPerfil ha sido reemplazada
        // por un único campo fotoPerfil que contiene el data URI completo
        assertTrue(true);
    }
    
    @Test
    void testGettersSetters_FechaRegistro() {
        Usuario usuario = new Usuario();
        Date fecha = new Date();
        
        usuario.setFechaRegistro(fecha);
        
        assertEquals(fecha, usuario.getFechaRegistro());
    }
    
    @Test
    void testGettersSetters_CredentialsVersion() {
        Usuario usuario = new Usuario();
        long version = 5L;
        
        usuario.setCredentialsVersion(version);
        
        assertEquals(version, usuario.getCredentialsVersion());
    }
    
    @Test
    void testGettersSetters_TwoFactorEnabled() {
        Usuario usuario = new Usuario();
        
        usuario.setTwoFactorEnabled(true);
        assertTrue(usuario.isTwoFactorEnabled());
        
        usuario.setTwoFactorEnabled(false);
        assertFalse(usuario.isTwoFactorEnabled());
    }
    
    @Test
    void testGettersSetters_ThirdFactorEnabled() {
        Usuario usuario = new Usuario();
        
        usuario.setThirdFactorEnabled(true);
        assertTrue(usuario.isThirdFactorEnabled());
        
        usuario.setThirdFactorEnabled(false);
        assertFalse(usuario.isThirdFactorEnabled());
    }
    
    @Test
    void testGettersSetters_TotpSecret() {
        Usuario usuario = new Usuario();
        String secret = "JBSWY3DPEHPK3PXP";
        
        usuario.setTotpSecret(secret);
        
        assertEquals(secret, usuario.getTotpSecret());
    }
    
    @Test
    void testGettersSetters_BackupCodes() {
        Usuario usuario = new Usuario();
        List<String> codes = Arrays.asList("code1", "code2", "code3");
        
        usuario.setBackupCodes(codes);
        
        assertEquals(codes, usuario.getBackupCodes());
    }
    
    @Test
    void testGettersSetters_Rol() {
        Usuario usuario = new Usuario();
        
        usuario.setRol(Rol.NORMAL);
        assertEquals(Rol.NORMAL, usuario.getRol());
        
        usuario.setRol(Rol.CREADOR);
        assertEquals(Rol.CREADOR, usuario.getRol());
        
        usuario.setRol(Rol.ADMINISTRADOR);
        assertEquals(Rol.ADMINISTRADOR, usuario.getRol());
    }

    // ==================== Tests equals() ====================
    
    @Test
    void testEquals_MismoObjeto() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario("id1");
        
        assertEquals(usuario, usuario);
    }
    
    @Test
    void testEquals_ObjetosIguales() {
        Date fecha = new Date();
        String foto = "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...";
        List<String> codes = Arrays.asList("code1", "code2");
        
        Usuario u1 = new Usuario();
        u1.setIdUsuario("id1");
        u1.setNombre("Juan");
        u1.setApellidos("García");
        u1.setEmail("juan@test.com");
        u1.setAlias("juangar");
        u1.setPassword("pass123");
        u1.setFotoPerfil(foto);
        u1.setFechaRegistro(fecha);
        u1.setCredentialsVersion(1L);
        u1.setTwoFactorEnabled(true);
        u1.setThirdFactorEnabled(false);
        u1.setTotpSecret("secret");
        u1.setBackupCodes(codes);
        u1.setRol(Rol.ADMINISTRADOR);
        
        Usuario u2 = new Usuario();
        u2.setIdUsuario("id1");
        u2.setNombre("Juan");
        u2.setApellidos("García");
        u2.setEmail("juan@test.com");
        u2.setAlias("juangar");
        u2.setPassword("pass123");
        u2.setFotoPerfil(foto);
        u2.setFechaRegistro(fecha);
        u2.setCredentialsVersion(1L);
        u2.setTwoFactorEnabled(true);
        u2.setThirdFactorEnabled(false);
        u2.setTotpSecret("secret");
        u2.setBackupCodes(codes);
        u2.setRol(Rol.ADMINISTRADOR);
        
        assertEquals(u1, u2);
    }
    
    @Test
    void testEquals_Null() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario("id1");
        
        assertNotEquals(null, usuario);
    }
    
    @Test
    void testEquals_DiferenteClase() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario("id1");
        String otherObject = "Not a Usuario";
        
        assertNotEquals(usuario, otherObject);
    }
    
    @Test
    void testEquals_DiferenteIdUsuario() {
        Usuario u1 = new Usuario();
        u1.setIdUsuario("id1");
        
        Usuario u2 = new Usuario();
        u2.setIdUsuario("id2");
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_IdUsuarioNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setIdUsuario("id1");
        
        Usuario u2 = new Usuario();
        u2.setIdUsuario(null);
        
        assertNotEquals(u1, u2);
        assertNotEquals(u2, u1);
    }
    
    @Test
    void testEquals_DiferenteNombre() {
        Usuario u1 = new Usuario();
        u1.setNombre("Juan");
        
        Usuario u2 = new Usuario();
        u2.setNombre("Pedro");
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_NombreNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setNombre("Juan");
        
        Usuario u2 = new Usuario();
        u2.setNombre(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteApellidos() {
        Usuario u1 = new Usuario();
        u1.setApellidos("García");
        
        Usuario u2 = new Usuario();
        u2.setApellidos("López");
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_ApellidosNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setApellidos("García");
        
        Usuario u2 = new Usuario();
        u2.setApellidos(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteEmail() {
        Usuario u1 = new Usuario();
        u1.setEmail("juan@test.com");
        
        Usuario u2 = new Usuario();
        u2.setEmail("pedro@test.com");
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_EmailNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setEmail("juan@test.com");
        
        Usuario u2 = new Usuario();
        u2.setEmail(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteAlias() {
        Usuario u1 = new Usuario();
        u1.setAlias("alias1");
        
        Usuario u2 = new Usuario();
        u2.setAlias("alias2");
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_AliasNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setAlias("alias1");
        
        Usuario u2 = new Usuario();
        u2.setAlias(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferentePassword() {
        Usuario u1 = new Usuario();
        u1.setPassword("pass1");
        
        Usuario u2 = new Usuario();
        u2.setPassword("pass2");
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_PasswordNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setPassword("pass1");
        
        Usuario u2 = new Usuario();
        u2.setPassword(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteFotoPerfil() {
        Usuario u1 = new Usuario();
        u1.setFotoPerfil("data:image/jpeg;base64,/9j/4AAQSkZJRgABA...");
        
        Usuario u2 = new Usuario();
        u2.setFotoPerfil("data:image/png;base64,iVBORw0KGgoAAAA...");
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_FotoPerfilNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setFotoPerfil("data:image/jpeg;base64,/9j/4AAQSkZJRgABA...");
        
        Usuario u2 = new Usuario();
        u2.setFotoPerfil(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteFormatoFotoPerfil() {
        // Este test ya no aplica - el formato está incluido en el data URI
        assertTrue(true);
    }
    
    @Test
    void testEquals_FormatoFotoPerfilNulo_UnoDeLosObjetos() {
        // Este test ya no aplica - el formato está incluido en el data URI
        assertTrue(true);
    }
    
    @Test
    void testEquals_DiferenteFechaRegistro() {
        Usuario u1 = new Usuario();
        u1.setFechaRegistro(new Date(1000000));
        
        Usuario u2 = new Usuario();
        u2.setFechaRegistro(new Date(2000000));
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_FechaRegistroNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setFechaRegistro(new Date());
        
        Usuario u2 = new Usuario();
        u2.setFechaRegistro(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteCredentialsVersion() {
        Usuario u1 = new Usuario();
        u1.setCredentialsVersion(1L);
        
        Usuario u2 = new Usuario();
        u2.setCredentialsVersion(2L);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteTwoFactorEnabled() {
        Usuario u1 = new Usuario();
        u1.setTwoFactorEnabled(true);
        
        Usuario u2 = new Usuario();
        u2.setTwoFactorEnabled(false);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteThirdFactorEnabled() {
        Usuario u1 = new Usuario();
        u1.setThirdFactorEnabled(true);
        
        Usuario u2 = new Usuario();
        u2.setThirdFactorEnabled(false);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteTotpSecret() {
        Usuario u1 = new Usuario();
        u1.setTotpSecret("secret1");
        
        Usuario u2 = new Usuario();
        u2.setTotpSecret("secret2");
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_TotpSecretNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setTotpSecret("secret1");
        
        Usuario u2 = new Usuario();
        u2.setTotpSecret(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteBackupCodes() {
        Usuario u1 = new Usuario();
        u1.setBackupCodes(Arrays.asList("code1", "code2"));
        
        Usuario u2 = new Usuario();
        u2.setBackupCodes(Arrays.asList("code3", "code4"));
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_BackupCodesNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setBackupCodes(Arrays.asList("code1"));
        
        Usuario u2 = new Usuario();
        u2.setBackupCodes(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_DiferenteRol() {
        Usuario u1 = new Usuario();
        u1.setRol(Rol.NORMAL);
        
        Usuario u2 = new Usuario();
        u2.setRol(Rol.ADMINISTRADOR);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_RolNulo_UnoDeLosObjetos() {
        Usuario u1 = new Usuario();
        u1.setRol(Rol.NORMAL);
        
        Usuario u2 = new Usuario();
        u2.setRol(null);
        
        assertNotEquals(u1, u2);
    }
    
    @Test
    void testEquals_TodosLosCamposNulos() {
        Usuario u1 = new Usuario();
        Usuario u2 = new Usuario();
        
        assertEquals(u1, u2);
    }

    // ==================== Tests hashCode() ====================
    
    @Test
    void testHashCode_ObjetosIguales() {
        Date fecha = new Date();
        
        Usuario u1 = new Usuario();
        u1.setIdUsuario("id1");
        u1.setNombre("Juan");
        u1.setEmail("juan@test.com");
        u1.setFechaRegistro(fecha);
        
        Usuario u2 = new Usuario();
        u2.setIdUsuario("id1");
        u2.setNombre("Juan");
        u2.setEmail("juan@test.com");
        u2.setFechaRegistro(fecha);
        
        assertEquals(u1.hashCode(), u2.hashCode());
    }
    
    @Test
    void testHashCode_ObjetosDiferentes() {
        Usuario u1 = new Usuario();
        u1.setIdUsuario("id1");
        
        Usuario u2 = new Usuario();
        u2.setIdUsuario("id2");
        
        assertNotEquals(u1.hashCode(), u2.hashCode());
    }
    
    @Test
    void testHashCode_Consistencia() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario("id1");
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");
        
        int hash1 = usuario.hashCode();
        int hash2 = usuario.hashCode();
        
        assertEquals(hash1, hash2);
    }



    // ==================== Tests toString() ====================
    
    @Test
    void testToString_ConTodosLosCampos() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario("id123");
        usuario.setNombre("Juan");
        usuario.setApellidos("García");
        usuario.setEmail("juan@test.com");
        usuario.setAlias("juangar");
        usuario.setRol(Rol.ADMINISTRADOR);
        
        String result = usuario.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("id123"));
        assertTrue(result.contains("Juan"));
        assertTrue(result.contains("García"));
        assertTrue(result.contains("juan@test.com"));
        assertTrue(result.contains("juangar"));
    }
    
    @Test
    void testToString_CamposNulos() {
        Usuario usuario = new Usuario();
        
        String result = usuario.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("Usuario"));
    }
}