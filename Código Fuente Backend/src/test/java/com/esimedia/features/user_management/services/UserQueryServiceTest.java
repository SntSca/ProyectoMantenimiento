package com.esimedia.features.user_management.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UsuarioNormalRepository usuarioNormalRepository;
    @Mock
    private CreadorContenidoRepository creadorContenidoRepository;
    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    private UsuarioNormal usuarioNormal;
    private CreadorContenido creador;
    private Administrador admin;

    @BeforeEach
    void setUp() {
        usuarioNormal = new UsuarioNormal();
        usuarioNormal.setIdUsuario("user123");
        usuarioNormal.setEmail("test@example.com");
        usuarioNormal.setAlias("testuser");
        usuarioNormal.setNombre("Test");
        usuarioNormal.setApellidos("User");
        usuarioNormal.setPassword("hashedPassword");

        creador = new CreadorContenido();
        creador.setIdUsuario("creator123");
        creador.setEmail("creator@example.com");
        creador.setAlias("creatoruser");
        creador.setValidado(false);

        admin = new Administrador();
        admin.setIdUsuario("admin123");
        admin.setEmail("admin@example.com");
        admin.setAlias("adminuser");
    }

    // ===== FIND BY EMAIL TESTS =====

    @Test
    void testFindUsuarioNormalByEmail_Success() {
        when(usuarioNormalRepository.findByemail("test@example.com"))
            .thenReturn(Optional.of(usuarioNormal));

        UsuarioNormal result = userQueryService.findUsuarioNormalByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(usuarioNormalRepository).findByemail("test@example.com");
    }

    @Test
    void testFindUsuarioNormalByEmail_NotFound() {
        when(usuarioNormalRepository.findByemail("notfound@example.com"))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> userQueryService.findUsuarioNormalByEmail("notfound@example.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Usuario no encontrado"));
    }

    @Test
    void testFindCreadorByEmail_Success() {
        when(creadorContenidoRepository.findByemail("creator@example.com"))
            .thenReturn(Optional.of(creador));

        CreadorContenido result = userQueryService.findCreadorByEmail("creator@example.com");

        assertNotNull(result);
        assertEquals("creator@example.com", result.getEmail());
        verify(creadorContenidoRepository).findByemail("creator@example.com");
    }

    @Test
    void testFindCreadorByEmail_NotFound() {
        when(creadorContenidoRepository.findByemail("notfound@example.com"))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> userQueryService.findCreadorByEmail("notfound@example.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Creador no encontrado"));
    }

    @Test
    void testFindAdminByEmail_Success() {
        when(adminRepository.findByemail("admin@example.com"))
            .thenReturn(Optional.of(admin));

        Administrador result = userQueryService.findAdminByEmail("admin@example.com");

        assertNotNull(result);
        assertEquals("admin@example.com", result.getEmail());
        verify(adminRepository).findByemail("admin@example.com");
    }

    @Test
    void testFindAdminByEmail_NotFound() {
        when(adminRepository.findByemail("notfound@example.com"))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> userQueryService.findAdminByEmail("notfound@example.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Usuario no encontrado"));
    }

    // ===== FIND BY ID TESTS =====
    @Test
    void testFindCreadorById_NotFound() {
        when(creadorContenidoRepository.findById("unknown"))
            .thenReturn(Optional.empty());

        Optional<CreadorContenido> result = userQueryService.findCreadorById("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAdminById_Success() {
        when(adminRepository.findById("admin123"))
            .thenReturn(Optional.of(admin));

        Optional<Administrador> result = userQueryService.findAdminById("admin123");

        assertTrue(result.isPresent());
        assertEquals("admin123", result.get().getIdUsuario());
        verify(adminRepository).findById("admin123");
    }

    @Test
    void testFindAdminById_NotFound() {
        when(adminRepository.findById("unknown"))
            .thenReturn(Optional.empty());

        Optional<Administrador> result = userQueryService.findAdminById("unknown");

        assertFalse(result.isPresent());
    }

    // ===== FIND ANY USER BY ID TESTS =====

    @Test
    void testFindAnyUserById_UsuarioNormal_Success() {
        when(usuarioNormalRepository.findById("user123"))
            .thenReturn(Optional.of(usuarioNormal));
        when(creadorContenidoRepository.findById("user123"))
            .thenReturn(Optional.empty());
        when(adminRepository.findById("user123"))
            .thenReturn(Optional.empty());

        Object result = userQueryService.findAnyUserById("user123");

        assertNotNull(result);
        assertTrue(result instanceof UsuarioNormal);
        assertEquals("user123", ((UsuarioNormal) result).getIdUsuario());
    }

    @Test
    void testFindAnyUserById_Creador_Success() {
        when(usuarioNormalRepository.findById("creator123"))
            .thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById("creator123"))
            .thenReturn(Optional.of(creador));
        when(adminRepository.findById("creator123"))
            .thenReturn(Optional.empty());

        Object result = userQueryService.findAnyUserById("creator123");

        assertNotNull(result);
        assertTrue(result instanceof CreadorContenido);
        assertEquals("creator123", ((CreadorContenido) result).getIdUsuario());
    }

    @Test
    void testFindAnyUserById_Admin_Success() {
        when(usuarioNormalRepository.findById("admin123"))
            .thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById("admin123"))
            .thenReturn(Optional.empty());
        when(adminRepository.findById("admin123"))
            .thenReturn(Optional.of(admin));

        Object result = userQueryService.findAnyUserById("admin123");

        assertNotNull(result);
        assertTrue(result instanceof Administrador);
        assertEquals("admin123", ((Administrador) result).getIdUsuario());
    }

    @Test
    void testFindAnyUserById_NotFound() {
        when(usuarioNormalRepository.findById("unknown"))
            .thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById("unknown"))
            .thenReturn(Optional.empty());
        when(adminRepository.findById("unknown"))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> userQueryService.findAnyUserById("unknown")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Usuario no encontrado"));
    }

    // ===== UPDATE TESTS =====

    @Test
    void testUpdateUsuarioNormal_Success() {
        when(usuarioNormalRepository.save(usuarioNormal)).thenReturn(usuarioNormal);

        userQueryService.updateUsuarioNormal(usuarioNormal);

        verify(usuarioNormalRepository).save(usuarioNormal);
    }

    @Test
    void testUpdateCreador_Success() {
        when(creadorContenidoRepository.save(creador)).thenReturn(creador);

        userQueryService.updateCreador(creador);

        verify(creadorContenidoRepository).save(creador);
    }

    @Test
    void testUpdateAdmin_Success() {
        when(adminRepository.save(admin)).thenReturn(admin);

        userQueryService.updateAdmin(admin);

        verify(adminRepository).save(admin);
    }

    // ===== FIND BY EMAIL (OPTIONAL) TESTS =====

    @Test
    void testFindByEmail_Success() {
        when(usuarioNormalRepository.findByemail("test@example.com"))
            .thenReturn(Optional.of(usuarioNormal));

        Optional<UsuarioNormal> result = userQueryService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(usuarioNormalRepository.findByemail("notfound@example.com"))
            .thenReturn(Optional.empty());

        Optional<UsuarioNormal> result = userQueryService.findByEmail("notfound@example.com");

        assertFalse(result.isPresent());
    }

    // ===== FIND BY ALIAS TESTS =====

    @Test
    void testFindByAlias_Success() {
        when(usuarioNormalRepository.findByalias("testuser"))
            .thenReturn(Optional.of(usuarioNormal));

        Optional<UsuarioNormal> result = userQueryService.findByAlias("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getAlias());
    }

    @Test
    void testFindByAlias_NotFound() {
        when(usuarioNormalRepository.findByalias("unknown"))
            .thenReturn(Optional.empty());

        Optional<UsuarioNormal> result = userQueryService.findByAlias("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindById_Success() {
        when(usuarioNormalRepository.findById("user123"))
            .thenReturn(Optional.of(usuarioNormal));

        Optional<UsuarioNormal> result = userQueryService.findById("user123");

        assertTrue(result.isPresent());
        assertEquals("user123", result.get().getIdUsuario());
    }

    // ===== FIND ANY USER BY ALIAS TESTS =====

    @Test
    void testFindAnyUserByAlias_UsuarioNormal_Success() {
        when(usuarioNormalRepository.findByalias("testuser"))
            .thenReturn(Optional.of(usuarioNormal));

        Optional<Usuario> result = userQueryService.findAnyUserByAlias("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getAlias());
        verify(usuarioNormalRepository).findByalias("testuser");
    }

    @Test
    void testFindAnyUserByAlias_Creador_Success() {
        when(usuarioNormalRepository.findByalias("creatoruser"))
            .thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias("creatoruser"))
            .thenReturn(Optional.of(creador));

        Optional<Usuario> result = userQueryService.findAnyUserByAlias("creatoruser");

        assertTrue(result.isPresent());
        assertEquals("creatoruser", result.get().getAlias());
        verify(creadorContenidoRepository).findByAlias("creatoruser");
    }

    @Test
    void testFindAnyUserByAlias_Admin_Success() {
        when(usuarioNormalRepository.findByalias("adminuser"))
            .thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias("adminuser"))
            .thenReturn(Optional.empty());
        when(adminRepository.findByalias("adminuser"))
            .thenReturn(Optional.of(admin));

        Optional<Usuario> result = userQueryService.findAnyUserByAlias("adminuser");

        assertTrue(result.isPresent());
        assertEquals("adminuser", result.get().getAlias());
        verify(adminRepository).findByalias("adminuser");
    }

    @Test
    void testFindAnyUserByAlias_NotFound() {
        when(usuarioNormalRepository.findByalias("unknown"))
            .thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias("unknown"))
            .thenReturn(Optional.empty());
        when(adminRepository.findByalias("unknown"))
            .thenReturn(Optional.empty());

        Optional<Usuario> result = userQueryService.findAnyUserByAlias("unknown");

        assertFalse(result.isPresent());
    }

    // ===== DELETE USER TESTS =====

    @Test
    void testDeleteUser_Success() {
        when(usuarioNormalRepository.existsById("user123")).thenReturn(true);
        doNothing().when(usuarioNormalRepository).deleteById("user123");

        userQueryService.deleteUser("user123");

        verify(usuarioNormalRepository).existsById("user123");
        verify(usuarioNormalRepository).deleteById("user123");
    }

    @Test
    void testDeleteUser_NotFound() {
        when(usuarioNormalRepository.existsById("unknown")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> userQueryService.deleteUser("unknown")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Usuario no encontrado"));
        verify(usuarioNormalRepository, never()).deleteById(anyString());
    }

    // ===== VALIDATE CREATOR TESTS =====

    @Test
    void testValidateCreator_Success() {
        when(creadorContenidoRepository.findById("creator123"))
            .thenReturn(Optional.of(creador));
        when(creadorContenidoRepository.save(any(CreadorContenido.class)))
            .thenReturn(creador);

        String result = userQueryService.validateCreator("creator123");

        assertEquals("Creador validado exitosamente", result);
        assertTrue(creador.isValidado());
        verify(creadorContenidoRepository).save(creador);
    }

    @Test
    void testValidateCreator_NotFound() {
        when(creadorContenidoRepository.findById("unknown"))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> userQueryService.validateCreator("unknown")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Creador no encontrado"));
        verify(creadorContenidoRepository, never()).save(any());
    }

    @Test
    void testValidateCreator_AlreadyValidated() {
        creador.setValidado(true);
        when(creadorContenidoRepository.findById("creator123"))
            .thenReturn(Optional.of(creador));
        when(creadorContenidoRepository.save(creador))
            .thenReturn(creador);

        String result = userQueryService.validateCreator("creator123");

        assertEquals("Creador validado exitosamente", result);
        assertTrue(creador.isValidado());
        verify(creadorContenidoRepository).save(creador);
    }

    @Test
    void testFindUsuarioNormalById_NotFound() {
        when(usuarioNormalRepository.findById("unknown"))
            .thenReturn(Optional.empty());

        Optional<UsuarioNormal> result = userQueryService.findUsuarioNormalById("unknown");

        assertFalse(result.isPresent());
    }
}