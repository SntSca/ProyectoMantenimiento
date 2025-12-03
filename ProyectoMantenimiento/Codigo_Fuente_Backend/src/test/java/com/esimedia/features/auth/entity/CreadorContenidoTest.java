package com.esimedia.features.auth.entity;

import org.junit.jupiter.api.Test;
import com.esimedia.features.auth.enums.TipoContenido;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class CreadorContenidoTest {

    // ==================== Tests Constructores ====================
    
    @Test
    void testConstructorVacio() {
        // Act
        CreadorContenido creador = new CreadorContenido();
        
        // Assert
        assertNotNull(creador);
        assertNull(creador.getAliasCreador());
        assertNull(creador.getDescripcion());
        assertNull(creador.getTipoContenido());
        assertFalse(creador.isBloqueado());
        assertFalse(creador.isValidado());
        // Verificar campos heredados
        assertNull(creador.getIdUsuario());
        assertNull(creador.getNombre());
        assertNull(creador.getEmail());
    }
    
    @Test
    void testBuilder_CamposBasicos() {
        // Act
        CreadorContenido creador = CreadorContenido.builder()
            .nombre("Ana")
            .apellidos("García")
            .email("ana@test.com")
            .alias("anagarcia")
            .password("pass123")
            .aliasCreador("creadora_ana")
            .descripcion("Creadora de contenido musical")
            .tipoContenido(TipoContenido.AUDIO)
            .build();
        
        // Assert
        assertEquals("Ana", creador.getNombre());
        assertEquals("García", creador.getApellidos());
        assertEquals("ana@test.com", creador.getEmail());
        assertEquals("anagarcia", creador.getAlias());
        assertEquals("pass123", creador.getPassword());
        assertEquals("creadora_ana", creador.getAliasCreador());
        assertEquals("Creadora de contenido musical", creador.getDescripcion());
        assertEquals(TipoContenido.AUDIO, creador.getTipoContenido());
        assertFalse(creador.isBloqueado());
        assertFalse(creador.isValidado());
    }
    
    @Test
    void testBuilder_ConBloqueadoYValidado() {
        // Act
        CreadorContenido creador = CreadorContenido.builder()
            .nombre("Carlos")
            .apellidos("López")
            .email("carlos@test.com")
            .alias("carlosl")
            .password("pass456")
            .aliasCreador("creador_carlos")
            .tipoContenido(TipoContenido.VIDEO)
            .bloqueado(true)
            .validado(true)
            .build();
        
        // Assert
        assertTrue(creador.isBloqueado());
        assertTrue(creador.isValidado());
        assertEquals(TipoContenido.VIDEO, creador.getTipoContenido());
    }
    
    @Test
    void testBuilder_ConFotoPerfil() {
        // Arrange
        String foto = "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...";
        
        // Act
        CreadorContenido creador = CreadorContenido.builder()
            .nombre("María")
            .apellidos("Pérez")
            .email("maria@test.com")
            .alias("mariap")
            .password("pass789")
            .aliasCreador("creadora_maria")
            .tipoContenido(TipoContenido.AUDIO)
            .fotoPerfil(foto)
            .build();
        
        // Assert
        assertEquals(foto, creador.getFotoPerfil());
    }

    // ==================== Tests Getters y Setters ====================
    
    @Test
    void testGettersSetters_AliasCreador() {
        CreadorContenido creador = new CreadorContenido();
        String aliasCreador = "creador_test";
        
        creador.setAliasCreador(aliasCreador);
        
        assertEquals(aliasCreador, creador.getAliasCreador());
    }
    
    @Test
    void testGettersSetters_Descripcion() {
        CreadorContenido creador = new CreadorContenido();
        String descripcion = "Descripción del creador de contenido";
        
        creador.setDescripcion(descripcion);
        
        assertEquals(descripcion, creador.getDescripcion());
    }
    
    @Test
    void testGettersSetters_Bloqueado() {
        CreadorContenido creador = new CreadorContenido();
        
        creador.setBloqueado(true);
        assertTrue(creador.isBloqueado());
        
        creador.setBloqueado(false);
        assertFalse(creador.isBloqueado());
    }
    
    @Test
    void testGettersSetters_TipoContenido() {
        CreadorContenido creador = new CreadorContenido();
        
        creador.setTipoContenido(TipoContenido.AUDIO);
        assertEquals(TipoContenido.AUDIO, creador.getTipoContenido());
        
        creador.setTipoContenido(TipoContenido.VIDEO);
        assertEquals(TipoContenido.VIDEO, creador.getTipoContenido());
    }
    
    @Test
    void testGettersSetters_Validado() {
        CreadorContenido creador = new CreadorContenido();
        
        creador.setValidado(true);
        assertTrue(creador.isValidado());
        
        creador.setValidado(false);
        assertFalse(creador.isValidado());
    }

    // ==================== Tests equals() ====================
    
    @Test
    void testEquals_MismoObjeto() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario("id1");
        creador.setAliasCreador("creador1");
        
        assertEquals(creador, creador);
    }
    
    @Test
    void testEquals_ObjetosIguales() {
        Date fecha = new Date();
        
        CreadorContenido c1 = CreadorContenido.builder()
            .nombre("Ana")
            .apellidos("García")
            .email("ana@test.com")
            .alias("anagarcia")
            .password("pass123")
            .aliasCreador("creadora_ana")
            .descripcion("Descripción")
            .tipoContenido(TipoContenido.AUDIO)
            .bloqueado(false)
            .validado(true)
            .build();
        c1.setIdUsuario("id1");
        c1.setFechaRegistro(fecha);
        
        CreadorContenido c2 = CreadorContenido.builder()
            .nombre("Ana")
            .apellidos("García")
            .email("ana@test.com")
            .alias("anagarcia")
            .password("pass123")
            .aliasCreador("creadora_ana")
            .descripcion("Descripción")
            .tipoContenido(TipoContenido.AUDIO)
            .bloqueado(false)
            .validado(true)
            .build();
        c2.setIdUsuario("id1");
        c2.setFechaRegistro(fecha);
        
        assertEquals(c1, c2);
    }
    
    @Test
    void testEquals_Null() {
        CreadorContenido creador = new CreadorContenido();
        creador.setAliasCreador("creador1");
        
        assertNotEquals(null, creador);
    }
    
    @Test
    void testEquals_DiferenteClase() {
        CreadorContenido creador = new CreadorContenido();
        creador.setAliasCreador("creador1");
        String otherObject = "Not a CreadorContenido";
        
        assertNotEquals(creador, otherObject);
    }
    
    @Test
    void testEquals_DiferenteAliasCreador() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setAliasCreador("creador1");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setAliasCreador("creador2");
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_AliasCreadorNulo_UnoDeLosObjetos() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setAliasCreador("creador1");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setAliasCreador(null);
        
        assertNotEquals(c1, c2);
        assertNotEquals(c2, c1);
    }
    
    @Test
    void testEquals_DiferenteDescripcion() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setDescripcion("Descripción 1");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setDescripcion("Descripción 2");
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_DescripcionNulo_UnoDeLosObjetos() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setDescripcion("Descripción 1");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setDescripcion(null);
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_DiferenteBloqueado() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setBloqueado(true);
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setBloqueado(false);
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_DiferenteTipoContenido() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setTipoContenido(TipoContenido.AUDIO);
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setTipoContenido(TipoContenido.VIDEO);
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_TipoContenidoNulo_UnoDeLosObjetos() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setTipoContenido(TipoContenido.AUDIO);
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setTipoContenido(null);
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_DiferenteValidado() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setValidado(true);
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setValidado(false);
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_DiferenteCampoHeredado_IdUsuario() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setIdUsuario("id1");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setIdUsuario("id2");
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_DiferenteCampoHeredado_Nombre() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setNombre("Ana");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setNombre("Carlos");
        
        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_DiferenteCampoHeredado_Email() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setEmail("ana@test.com");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setEmail("carlos@test.com");

        assertNotEquals(c1, c2);
    }
    
    @Test
    void testEquals_TodosLosCamposNulos() {
        CreadorContenido c1 = new CreadorContenido();
        CreadorContenido c2 = new CreadorContenido();
        
        assertEquals(c1, c2);
    }
    
    @Test
    void testEquals_CompararConUsuarioNormal() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario("id1");
        creador.setNombre("Ana");
        
        UsuarioNormal usuario = new UsuarioNormal();
        usuario.setIdUsuario("id1");
        usuario.setNombre("Ana");
        
        // No deberían ser iguales porque son clases diferentes
        assertNotEquals(creador, usuario);
    }

    // ==================== Tests hashCode() ====================
    
    @Test
    void testHashCode_ObjetosIguales() {
        Date fecha = new Date();
        
        CreadorContenido c1 = CreadorContenido.builder()
            .nombre("Ana")
            .email("ana@test.com")
            .alias("anagarcia")
            .password("pass123")
            .aliasCreador("creadora_ana")
            .tipoContenido(TipoContenido.AUDIO)
            .build();
        c1.setIdUsuario("id1");
        c1.setFechaRegistro(fecha);
        
        CreadorContenido c2 = CreadorContenido.builder()
            .nombre("Ana")
            .email("ana@test.com")
            .alias("anagarcia")
            .password("pass123")
            .aliasCreador("creadora_ana")
            .tipoContenido(TipoContenido.AUDIO)
            .build();
        c2.setIdUsuario("id1");
        c2.setFechaRegistro(fecha);
        
        assertEquals(c1.hashCode(), c2.hashCode());
    }
    
    @Test
    void testHashCode_ObjetosDiferentes() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setAliasCreador("creador1");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setAliasCreador("creador2");
        
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }
    
    @Test
    void testHashCode_Consistencia() {
        CreadorContenido creador = new CreadorContenido();
        creador.setIdUsuario("id1");
        creador.setAliasCreador("creador1");
        creador.setTipoContenido(TipoContenido.AUDIO);
        
        int hash1 = creador.hashCode();
        int hash2 = creador.hashCode();
        
        assertEquals(hash1, hash2);
    }
    
    @Test
    void testHashCode_IncluirCamposHeredados() {
        CreadorContenido c1 = new CreadorContenido();
        c1.setIdUsuario("id1");
        c1.setNombre("Ana");
        c1.setAliasCreador("creadora_ana");
        
        CreadorContenido c2 = new CreadorContenido();
        c2.setIdUsuario("id2");
        c2.setNombre("Ana");
        c2.setAliasCreador("creadora_ana");
        
        // Hash diferente porque idUsuario es diferente
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    // ==================== Tests toString() ====================
    
    @Test
    void testToString_CamposNulos() {
        CreadorContenido creador = new CreadorContenido();
        
        String result = creador.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("CreadorContenido"));
    }

    // ==================== Tests Builder ====================
    
    @Test
    void testBuilder_ValoresPorDefecto() {
        CreadorContenido creador = CreadorContenido.builder()
            .nombre("Test")
            .apellidos("User")
            .email("test@test.com")
            .alias("testuser")
            .password("pass")
            .aliasCreador("creador_test")
            .tipoContenido(TipoContenido.AUDIO)
            .build();
        
        assertFalse(creador.isBloqueado());
        assertFalse(creador.isValidado());
    }
    
    @Test
    void testBuilder_SetAllFields() {
        String foto = "data:image/png;base64,iVBORw0KGgoAAAA...";
        
        CreadorContenido creador = CreadorContenido.builder()
            .nombre("Complete")
            .apellidos("Test")
            .email("complete@test.com")
            .alias("completetest")
            .password("password")
            .aliasCreador("creador_complete")
            .descripcion("Descripción completa")
            .tipoContenido(TipoContenido.VIDEO)
            .bloqueado(true)
            .validado(true)
            .fotoPerfil(foto)
            .build();
        
        assertEquals("Complete", creador.getNombre());
        assertEquals("Test", creador.getApellidos());
        assertEquals("complete@test.com", creador.getEmail());
        assertEquals("completetest", creador.getAlias());
        assertEquals("password", creador.getPassword());
        assertEquals("creador_complete", creador.getAliasCreador());
        assertEquals("Descripción completa", creador.getDescripcion());
        assertEquals(TipoContenido.VIDEO, creador.getTipoContenido());
        assertTrue(creador.isBloqueado());
        assertTrue(creador.isValidado());
        assertEquals(foto, creador.getFotoPerfil());
    }
    
    @Test
    void testBuilder_ChainedCalls() {
        CreadorContenido creador = CreadorContenido.builder()
            .nombre("Chain")
            .apellidos("Test")
            .email("chain@test.com")
            .alias("chaintest")
            .password("pass")
            .aliasCreador("creador_chain")
            .descripcion("Test description")
            .tipoContenido(TipoContenido.AUDIO)
            .bloqueado(false)
            .validado(false)
            .build();
        
        assertNotNull(creador);
        assertEquals("creador_chain", creador.getAliasCreador());
        assertEquals("Test description", creador.getDescripcion());
    }
}
