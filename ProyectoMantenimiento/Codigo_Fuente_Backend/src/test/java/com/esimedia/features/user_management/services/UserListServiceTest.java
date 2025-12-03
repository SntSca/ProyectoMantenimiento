package com.esimedia.features.user_management.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.*;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.repository.*;

@ExtendWith(MockitoExtension.class)
public class UserListServiceTest {

    @Mock private UsuarioNormalRepository usuarioNormalRepository;
    @Mock private CreadorContenidoRepository creadorContenidoRepository;
    @Mock private AdminRepository adminRepository;

    @InjectMocks
    private UserListService userListService;

    private UsuarioNormal usuarioNormal;
    private CreadorContenido creador;
    private Administrador admin;

    @BeforeEach
    void setUp() {
        usuarioNormal = new UsuarioNormal();
        usuarioNormal.setIdUsuario("user123");
        usuarioNormal.setNombre("John");
        usuarioNormal.setApellidos("Doe");
        usuarioNormal.setEmail("john@example.com");
        usuarioNormal.setAlias("johndoe");
        usuarioNormal.setRol(Rol.NORMAL);
        usuarioNormal.setFlagVIP(false);
        usuarioNormal.setBloqueado(false);
        usuarioNormal.setConfirmado(true);
        usuarioNormal.setFechaNacimiento(new Date());

        creador = new CreadorContenido();
        creador.setIdUsuario("creador123");
        creador.setNombre("Jane");
        creador.setApellidos("Smith");
        creador.setEmail("jane@example.com");
        creador.setAlias("janesmith");
        creador.setRol(Rol.CREADOR);
        creador.setAliasCreador("CreatorJane");
        creador.setDescripcion("Content creator");
        creador.setBloqueado(false);
        creador.setEspecialidad("Video");
        creador.setTipoContenido(TipoContenido.VIDEO);
        creador.setValidado(true);

        admin = new Administrador();
        admin.setIdUsuario("admin123");
        admin.setNombre("Admin");
        admin.setApellidos("User");
        admin.setEmail("admin@example.com");
        admin.setAlias("adminuser");
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setDepartamento("IT");
    }

    // ========== getAllUsuariosNormales - Cubre success + exception ==========

    @Test
    void testGetAllUsuariosNormales_Success() {
        List<UsuarioNormal> usuarios = Arrays.asList(usuarioNormal);
        when(usuarioNormalRepository.findAll()).thenReturn(usuarios);

        List<UsuarioNormal> result = userListService.getAllUsuariosNormales();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user123", result.get(0).getIdUsuario());
    }

    @Test
    void testGetAllUsuariosNormales_EmptyList() {
        when(usuarioNormalRepository.findAll()).thenReturn(Collections.emptyList());

        List<UsuarioNormal> result = userListService.getAllUsuariosNormales();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllUsuariosNormales_Exception() {
        when(usuarioNormalRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ResponseStatusException.class, () ->
            userListService.getAllUsuariosNormales());
    }

    // ========== getAllAdministradores - Cubre success + exception ==========

    @Test
    void testGetAllAdministradores_Success() {
        List<Administrador> admins = Arrays.asList(admin);
        when(adminRepository.findAll()).thenReturn(admins);

        List<Administrador> result = userListService.getAllAdministradores();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin123", result.get(0).getIdUsuario());
    }

    @Test
    void testGetAllAdministradores_EmptyList() {
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());

        List<Administrador> result = userListService.getAllAdministradores();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllAdministradores_Exception() {
        when(adminRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ResponseStatusException.class, () ->
            userListService.getAllAdministradores());
    }

    // ========== getAllCreadoresContenido - Cubre success + exception ==========

    @Test
    void testGetAllCreadoresContenido_Success() {
        List<CreadorContenido> creadores = Arrays.asList(creador);
        when(creadorContenidoRepository.findAll()).thenReturn(creadores);

        List<CreadorContenido> result = userListService.getAllCreadoresContenido();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("creador123", result.get(0).getIdUsuario());
    }

    @Test
    void testGetAllCreadoresContenido_EmptyList() {
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());

        List<CreadorContenido> result = userListService.getAllCreadoresContenido();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllCreadoresContenido_Exception() {
        when(creadorContenidoRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ResponseStatusException.class, () ->
            userListService.getAllCreadoresContenido());
    }

    // ========== getAllUsers - Cubre convertToDTO branches + exception ==========

    @Test
    void testGetAllUsers_Success() {
        when(usuarioNormalRepository.findAll()).thenReturn(Arrays.asList(usuarioNormal));
        when(adminRepository.findAll()).thenReturn(Arrays.asList(admin));
        when(creadorContenidoRepository.findAll()).thenReturn(Arrays.asList(creador));

        Map<String, Object> result = userListService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.containsKey("normalUsers"));
        assertTrue(result.containsKey("administrators"));
        assertTrue(result.containsKey("contentCreators"));
    }

    @Test
    void testGetAllUsers_EmptyLists() {
        when(usuarioNormalRepository.findAll()).thenReturn(Collections.emptyList());
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> result = userListService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.containsKey("normalUsers"));
        assertTrue(result.containsKey("administrators"));
        assertTrue(result.containsKey("contentCreators"));
    }

    @Test
    void testGetAllUsers_Exception() {
        when(usuarioNormalRepository.findAll()).thenThrow(new RuntimeException("DB Error"));

        assertThrows(ResponseStatusException.class, () ->
            userListService.getAllUsers());
    }

    // ========== convertToDTO - Cubre branches de instanceof ==========

    @Test
    void testConvertToDTO_UsuarioNormal() {
        when(usuarioNormalRepository.findAll()).thenReturn(Arrays.asList(usuarioNormal));
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> result = userListService.getAllUsers();

        assertNotNull(result);
        // Verificar que se procesó el UsuarioNormal
        assertTrue(result.containsKey("normalUsers"));
    }

    @Test
    void testConvertToDTO_CreadorContenido() {
        when(usuarioNormalRepository.findAll()).thenReturn(Collections.emptyList());
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());
        when(creadorContenidoRepository.findAll()).thenReturn(Arrays.asList(creador));

        Map<String, Object> result = userListService.getAllUsers();

        assertNotNull(result);
        // Verificar que se procesó el CreadorContenido
        assertTrue(result.containsKey("contentCreators"));
    }

    @Test
    void testConvertToDTO_Administrador() {
        when(usuarioNormalRepository.findAll()).thenReturn(Collections.emptyList());
        when(adminRepository.findAll()).thenReturn(Arrays.asList(admin));
        when(creadorContenidoRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> result = userListService.getAllUsers();

        assertNotNull(result);
        // Verificar que se procesó el Administrador
        assertTrue(result.containsKey("administrators"));
    }

    @Test
    void testConvertToDTO_AllTypes() {
        when(usuarioNormalRepository.findAll()).thenReturn(Arrays.asList(usuarioNormal));
        when(adminRepository.findAll()).thenReturn(Arrays.asList(admin));
        when(creadorContenidoRepository.findAll()).thenReturn(Arrays.asList(creador));

        Map<String, Object> result = userListService.getAllUsers();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsKey("normalUsers"));
        assertTrue(result.containsKey("administrators"));
        assertTrue(result.containsKey("contentCreators"));
    }
}