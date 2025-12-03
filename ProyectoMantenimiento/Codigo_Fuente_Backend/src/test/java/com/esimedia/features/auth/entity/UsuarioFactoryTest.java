package com.esimedia.features.auth.entity;

import com.esimedia.features.auth.dto.AdministradorDTO;
import com.esimedia.features.auth.dto.CreadorContenidoDTO;
import com.esimedia.features.auth.dto.UsuarioDTO;
import com.esimedia.features.auth.dto.UsuarioNormalDTO;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.services.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UsuarioFactoryTest {

    @InjectMocks
    private com.esimedia.features.auth.entity.UsuarioFactory usuarioFactory;

    @Mock
    private ValidationService validationService;

    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setUp() {
        // Inyectar el pepper necesario para encodePassword
        ReflectionTestUtils.setField(usuarioFactory, "pepper", "test-pepper-secret");
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Test
    @DisplayName("Debe crear un UsuarioNormal correctamente desde UsuarioNormalDTO")
    void testCrearUsuarioNormal() throws Exception {
        // Given
        Date fechaNacimiento = dateFormat.parse("1990-05-15");
        
        UsuarioNormalDTO dto = new UsuarioNormalDTO();
        dto.setNombre("Juan");
        dto.setApellidos("Pérez García");
        dto.setEmail("juan.perez@example.com");
        dto.setAlias("juanperez");
        dto.setPassword("Password123!");
        dto.setFechaNacimiento(fechaNacimiento);
        dto.setFlagVIP(false);

        // Mock de validaciones (no lanzan excepciones)


        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario);
        assertInstanceOf(UsuarioNormal.class, usuario);
        
        UsuarioNormal usuarioNormal = (UsuarioNormal) usuario;
        assertEquals("Juan", usuarioNormal.getNombre());
        assertEquals("Pérez García", usuarioNormal.getApellidos());
        assertEquals("juan.perez@example.com", usuarioNormal.getEmail());
        assertEquals("juanperez", usuarioNormal.getAlias());
        assertNotNull(usuarioNormal.getPassword());
        assertNotEquals("Password123!", usuarioNormal.getPassword());
        assertEquals(fechaNacimiento, usuarioNormal.getFechaNacimiento());
        assertFalse(usuarioNormal.isFlagVIP());


    }

    @Test
    @DisplayName("Debe crear un UsuarioNormal con foto de perfil como data URI")
    void testCrearUsuarioNormalConFotoPerfil() throws Exception {
        // Given
        String fotoPerfil = "data:image/png;base64,iVBORw0KGgoAAAA...";
        Date fechaNacimiento = dateFormat.parse("1995-03-20");
        
        UsuarioNormalDTO dto = new UsuarioNormalDTO();
        dto.setNombre("María");
        dto.setApellidos("López");
        dto.setEmail("maria@example.com");
        dto.setAlias("marialopez");
        dto.setPassword("Password123!");
        dto.setFotoPerfil(fotoPerfil);
        dto.setFechaNacimiento(fechaNacimiento);
        dto.setFlagVIP(true);

        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario);
        assertInstanceOf(UsuarioNormal.class, usuario);
        
        UsuarioNormal usuarioNormal = (UsuarioNormal) usuario;
        assertNotNull(usuarioNormal.getFotoPerfil());
        assertEquals(fotoPerfil, usuarioNormal.getFotoPerfil());
        assertTrue(usuarioNormal.isFlagVIP());
    }

    @Test
    @DisplayName("Debe crear un CreadorContenido correctamente desde CreadorContenidoDTO")
    void testCrearCreadorContenido() {
        // Given
        CreadorContenidoDTO dto = new CreadorContenidoDTO();
        dto.setNombre("Ana");
        dto.setApellidos("García Ruiz");
        dto.setEmail("ana.garcia@example.com");
        dto.setAlias("anagarcia");
        dto.setPassword("SecurePass456!");
        dto.setAliasCreador("anacreadora");
        dto.setDescripcion("Creadora de contenido educativo");
        dto.setTipoContenido(TipoContenido.VIDEO);



        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario);
        assertInstanceOf(CreadorContenido.class, usuario);
        
        CreadorContenido creador = (CreadorContenido) usuario;
        assertEquals("Ana", creador.getNombre());
        assertEquals("García Ruiz", creador.getApellidos());
        assertEquals("ana.garcia@example.com", creador.getEmail());
        assertEquals("anagarcia", creador.getAlias());
        assertNotNull(creador.getPassword());
        assertEquals("anacreadora", creador.getAliasCreador());
        assertEquals("Creadora de contenido educativo", creador.getDescripcion());
        assertEquals(TipoContenido.VIDEO, creador.getTipoContenido());
    }

    @Test
    @DisplayName("Debe crear un CreadorContenido con foto de perfil como data URI")
    void testCrearCreadorContenidoConFoto() {
        // Given
        String fotoPerfil = "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...";
        
        CreadorContenidoDTO dto = new CreadorContenidoDTO();
        dto.setNombre("Pedro");
        dto.setApellidos("Martínez");
        dto.setEmail("pedro@example.com");
        dto.setAlias("pedrom");
        dto.setPassword("Pass123!");
        dto.setFotoPerfil(fotoPerfil);
        dto.setAliasCreador("pedrocreatormx");
        dto.setDescripcion("Creador de podcasts");
        dto.setTipoContenido(TipoContenido.AUDIO);

        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario);
        CreadorContenido creador = (CreadorContenido) usuario;
        assertNotNull(creador.getFotoPerfil());
        assertEquals(fotoPerfil, creador.getFotoPerfil());
        assertEquals(TipoContenido.AUDIO, creador.getTipoContenido());
    }

    @Test
    @DisplayName("Debe crear un Administrador correctamente desde AdministradorDTO")
    void testCrearAdministrador() {
        // Given
        AdministradorDTO dto = new AdministradorDTO();
        dto.setNombre("Carlos");
        dto.setApellidos("Rodríguez López");
        dto.setEmail("carlos.rodriguez@example.com");
        dto.setAlias("carlosrodriguez");
        dto.setPassword("AdminPass789!");
        dto.setDepartamento("Tecnología");



        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario);
        assertInstanceOf(Administrador.class, usuario);
        
        Administrador admin = (Administrador) usuario;
        assertEquals("Carlos", admin.getNombre());
        assertEquals("Rodríguez López", admin.getApellidos());
        assertEquals("carlos.rodriguez@example.com", admin.getEmail());
        assertEquals("carlosrodriguez", admin.getAlias());
        assertNotNull(admin.getPassword());
        assertEquals("Tecnología", admin.getDepartamento());

    }

    @Test
    @DisplayName("Debe crear un Administrador con foto de perfil como data URI")
    void testCrearAdministradorConFoto() {
        // Given
        String fotoPerfil = "data:image/png;base64,iVBORw0KGgoAAAA...";
        
        AdministradorDTO dto = new AdministradorDTO();
        dto.setNombre("Laura");
        dto.setApellidos("Sánchez");
        dto.setEmail("laura@example.com");
        dto.setAlias("laurasanchez");
        dto.setPassword("AdminSecure123!");
        dto.setFotoPerfil(fotoPerfil);
        dto.setDepartamento("Recursos Humanos");

        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario);
        Administrador admin = (Administrador) usuario;
        assertNotNull(admin.getFotoPerfil());
        assertEquals(fotoPerfil, admin.getFotoPerfil());
        assertEquals("Recursos Humanos", admin.getDepartamento());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el tipo de DTO no es soportado")
    void testCrearUsuarioConDtoNoSoportado() {
        // Given - Crear un DTO que no es de ningún tipo conocido
        UsuarioDTO dtoDesconocido = new UsuarioDTO() {
            // DTO personalizado no soportado
        };
        dtoDesconocido.setNombre("Test");
        dtoDesconocido.setApellidos("User");
        dtoDesconocido.setEmail("test@example.com");
        dtoDesconocido.setAlias("testuser");
        dtoDesconocido.setPassword("Password123!");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioFactory.crearUsuario(dtoDesconocido, validationService);
        });
        
        assertTrue(exception.getMessage().contains("Tipo de DTO no soportado"));
    }

    @Test
    @DisplayName("Debe manejar foto de perfil vacía correctamente")
    void testCrearUsuarioConFotoPerfilVacia() throws Exception {
        // Given
        Date fechaNacimiento = dateFormat.parse("2000-01-01");
        
        UsuarioNormalDTO dto = new UsuarioNormalDTO();
        dto.setNombre("Test");
        dto.setApellidos("User");
        dto.setEmail("test@example.com");
        dto.setAlias("testuser");
        dto.setPassword("Password123!");
        dto.setFotoPerfil("   ");
        dto.setFechaNacimiento(fechaNacimiento);
        dto.setFlagVIP(false);



        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario);
        UsuarioNormal usuarioNormal = (UsuarioNormal) usuario;
        assertNull(usuarioNormal.getFotoPerfil());
    }

    @Test
    @DisplayName("Debe manejar foto de perfil null correctamente")
    void testCrearUsuarioConFotoPerfilNull() throws Exception {
        // Given
        Date fechaNacimiento = dateFormat.parse("2000-01-01");
        
        UsuarioNormalDTO dto = new UsuarioNormalDTO();
        dto.setNombre("Test");
        dto.setApellidos("User");
        dto.setEmail("test@example.com");
        dto.setAlias("testuser");
        dto.setPassword("Password123!");
        dto.setFotoPerfil(null);
        dto.setFechaNacimiento(fechaNacimiento);
        dto.setFlagVIP(false);

        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario);
        UsuarioNormal usuarioNormal = (UsuarioNormal) usuario;
        assertNull(usuarioNormal.getFotoPerfil());
    }

    @Test
    @DisplayName("Debe codificar la contraseña correctamente con Argon2")
    void testEncodePassword() throws Exception {
        // Given
        String password = "MySecurePassword123!";
        Date fechaNacimiento = dateFormat.parse("2000-01-01");
        
        UsuarioNormalDTO dto = new UsuarioNormalDTO();
        dto.setNombre("Test");
        dto.setApellidos("User");
        dto.setEmail("test@example.com");
        dto.setAlias("testuser");
        dto.setPassword(password);
        dto.setFechaNacimiento(fechaNacimiento);
        dto.setFlagVIP(false);



        // When
        Usuario usuario = usuarioFactory.crearUsuario(dto, validationService);

        // Then
        assertNotNull(usuario.getPassword());
        assertNotEquals(password, usuario.getPassword());
        assertTrue(usuario.getPassword().startsWith("$argon2"));
        assertTrue(usuario.getPassword().length() > 50);
    }
}