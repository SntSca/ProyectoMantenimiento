package com.esimedia.features.user_management.services;

import com.esimedia.features.user_management.dto.AdminProfileUpdateDTO;
import com.esimedia.features.user_management.dto.CreatorProfileUpdateDTO;
import com.esimedia.features.user_management.dto.UserProfileUpdateDTO;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.SesionRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.services.ValoracionService;
import com.esimedia.features.favoritos.repository.ContenidoFavoritoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagementServiceTest {

    @Mock
    private UsuarioNormalRepository usuarioNormalRepository;

    @Mock
    private CreadorContenidoRepository creadorContenidoRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private ValoracionService valoracionService;

    @Mock
    private ContenidoFavoritoRepository contenidoFavoritoRepository;

    @InjectMocks
    private ManagementService managementService;

    private UsuarioNormal usuario;
    private CreadorContenido creador;
    private Administrador admin;
    private Date fechaNacimiento;

    @BeforeEach
    void setUp() {
        LocalDate fecha = LocalDate.now().minusYears(25);
        fechaNacimiento = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());

        usuario = new UsuarioNormal();
        usuario.setIdUsuario("u1");
        usuario.setNombre("Juan");
        usuario.setApellidos("Pérez");
        usuario.setAlias("juanp");
        usuario.setEmail("juan@gmail.com");
        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setFlagVIP(false);

        creador = new CreadorContenido();
        creador.setIdUsuario("c1");
        creador.setNombre("María");
        creador.setApellidos("García");
        creador.setAlias("mariag");
        creador.setAliasCreador("mariaCreator");
        creador.setDescripcion("Creadora de contenido");

        admin = new Administrador();
        admin.setIdUsuario("a1");
        admin.setNombre("Pedro");
        admin.setApellidos("López");
        admin.setAlias("pedrol");
    }

    // ============================================
    // TESTS EXISTENTES
    // ============================================

    @Test
    void getUserById_deberiaRetornarUsuarioCuandoExiste() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));

        UsuarioNormal result = managementService.getUserById("u1");

        assertNotNull(result);
        assertEquals("u1", result.getIdUsuario());
        verify(usuarioNormalRepository).findById("u1");
    }

    @Test
    void getUserById_deberiaLanzarExcepcionCuandoNoExiste() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> managementService.getUserById("u1"));
    }

    @Test
    void userExists_deberiaRetornarTrueCuandoExiste() {
        when(usuarioNormalRepository.existsById("u1")).thenReturn(true);

        boolean result = managementService.userExists("u1");

        assertTrue(result);
        verify(usuarioNormalRepository).existsById("u1");
    }

    @Test
    void userExists_deberiaRetornarFalseCuandoNoExiste() {
        when(usuarioNormalRepository.existsById("u1")).thenReturn(false);

        boolean result = managementService.userExists("u1");

        assertFalse(result);
    }

    @Test
    void toggleUserBlock_deberiaBloquearYGuardarUsuario() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));

        managementService.toggleUserBlock("u1", true);

        assertTrue(usuario.isBloqueado());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void toggleUserBlock_deberiaDesbloquearUsuario() {
        usuario.setBloqueado(true);
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));

        managementService.toggleUserBlock("u1", false);

        assertFalse(usuario.isBloqueado());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void toggleUserBlock_deberiaLanzarExcepcionCuandoUsuarioNoExiste() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, 
            () -> managementService.toggleUserBlock("u1", true));
    }

    @Test
    void findCreatorById_deberiaRetornarOptional() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));

        Optional<CreadorContenido> result = managementService.findCreatorById("c1");

        assertTrue(result.isPresent());
        assertEquals("c1", result.get().getIdUsuario());
    }

    @Test
    void findAdminById_deberiaRetornarOptional() {
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));

        Optional<Administrador> result = managementService.findAdminById("a1");

        assertTrue(result.isPresent());
        assertEquals("a1", result.get().getIdUsuario());
    }

    // ============================================
    // TESTS PARA updateNormalUserProfile
    // ============================================

    @Test
    void updateNormalUserProfile_deberiaActualizarNombre() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("Carlos");

        managementService.updateNormalUserProfile("u1", dto);

        assertEquals("Carlos", usuario.getNombre());
        verify(validationService).validateFieldLength("Carlos", "nombre", ValidationService.MAX_NOMBRE_LENGTH);
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void updateNormalUserProfile_deberiaActualizarApellidos() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setApellidos("Martínez");

        managementService.updateNormalUserProfile("u1", dto);

        assertEquals("Martínez", usuario.getApellidos());
        verify(validationService).validateFieldLength("Martínez", "apellidos", ValidationService.MAX_APELLIDOS_LENGTH);
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void updateNormalUserProfile_deberiaActualizarAlias() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        // No necesitamos mockear findByalias aquí porque el usuario actual tiene alias diferente
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setAlias("newAlias");

        managementService.updateNormalUserProfile("u1", dto);

        assertEquals("newAlias", usuario.getAlias());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void updateNormalUserProfile_deberiaLanzarExcepcionSiAliasEnUso() {
        UsuarioNormal otroUsuario = new UsuarioNormal();
        otroUsuario.setIdUsuario("u2");
        
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        when(usuarioNormalRepository.findByalias("takenAlias")).thenReturn(Optional.of(otroUsuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setAlias("takenAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.updateNormalUserProfile("u1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("alias ya está en uso"));
    }

    @Test
    void updateNormalUserProfile_deberiaPermitirMismoAliasParaMismoUsuario() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setAlias("juanp"); // Mismo alias que el usuario actual

        managementService.updateNormalUserProfile("u1", dto);

        assertEquals("juanp", usuario.getAlias());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void updateNormalUserProfile_deberiaActualizarFechaNacimiento() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        LocalDate nuevaFecha = LocalDate.now().minusYears(30);
        Date nuevaFechaNac = Date.from(nuevaFecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setFechaNacimiento(nuevaFechaNac);

        managementService.updateNormalUserProfile("u1", dto);

        assertEquals(nuevaFechaNac, usuario.getFechaNacimiento());
        verify(validationService).validateFechaNacimiento(nuevaFechaNac);
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void updateNormalUserProfile_deberiaLanzarExcepcionSiIntentaCambiarFlagVIP() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setFlagVIP(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.updateNormalUserProfile("u1", dto));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("No tienes permisos"));
    }

    @Test
    void updateNormalUserProfile_deberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.empty());
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("Carlos");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.updateNormalUserProfile("u1", dto));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ============================================
    // TESTS PARA adminUpdateUserProfile
    // ============================================

    @Test
    void adminUpdateUserProfile_deberiaActualizarNombre() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("AdminChanged");

        managementService.adminUpdateUserProfile("u1", dto);

        assertEquals("AdminChanged", usuario.getNombre());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void adminUpdateUserProfile_deberiaActualizarApellidos() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setApellidos("AdminApellido");

        managementService.adminUpdateUserProfile("u1", dto);

        assertEquals("AdminApellido", usuario.getApellidos());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void adminUpdateUserProfile_deberiaActualizarAlias() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        // No necesitamos mockear findByalias porque la validación ocurre a nivel de servicio
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setAlias("adminAlias");

        managementService.adminUpdateUserProfile("u1", dto);

        assertEquals("adminAlias", usuario.getAlias());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void adminUpdateUserProfile_deberiaActualizarFechaNacimiento() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        LocalDate nuevaFecha = LocalDate.now().minusYears(20);
        Date nuevaFechaNac = Date.from(nuevaFecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setFechaNacimiento(nuevaFechaNac);

        managementService.adminUpdateUserProfile("u1", dto);

        assertEquals(nuevaFechaNac, usuario.getFechaNacimiento());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void adminUpdateUserProfile_deberiaLanzarExcepcionSiIntentaCambiarFlagVIP() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setFlagVIP(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.adminUpdateUserProfile("u1", dto));
        
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("administradores no pueden modificar la suscripción"));
    }

    @Test
    void adminUpdateUserProfile_deberiaLanzarExcepcionSiAliasEnUso() {
        UsuarioNormal otroUsuario = new UsuarioNormal();
        otroUsuario.setIdUsuario("u2");
        
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));
        when(usuarioNormalRepository.findByalias("takenAlias")).thenReturn(Optional.of(otroUsuario));
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setAlias("takenAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.adminUpdateUserProfile("u1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void adminUpdateUserProfile_deberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.empty());
        
        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setNombre("Test");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.adminUpdateUserProfile("u1", dto));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ============================================
    // TESTS PARA updateCreatorProfile
    // ============================================

    @Test
    void updateCreatorProfile_deberiaLanzarExcepcionSiAliasEnUsoEnUsuarioNormal() {
        UsuarioNormal otroUsuario = new UsuarioNormal();
        otroUsuario.setIdUsuario("u2");
        
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));
        when(usuarioNormalRepository.findByalias("takenAlias")).thenReturn(Optional.of(otroUsuario));
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setAlias("takenAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.updateCreatorProfile("c1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateCreatorProfile_deberiaLanzarExcepcionSiAliasEnUsoEnOtroCreador() {
        CreadorContenido otroCreador = new CreadorContenido();
        otroCreador.setIdUsuario("c2");
        
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));
        when(usuarioNormalRepository.findByalias("takenAlias")).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias("takenAlias")).thenReturn(Optional.of(otroCreador));
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setAlias("takenAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.updateCreatorProfile("c1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateCreatorProfile_deberiaActualizarAliasCreador() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));
        when(creadorContenidoRepository.findByAliasCreador("newCreatorName")).thenReturn(Optional.empty());
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setAliasCreador("newCreatorName");

        managementService.updateCreatorProfile("c1", dto);

        assertEquals("newCreatorName", creador.getAliasCreador());
        verify(creadorContenidoRepository).save(creador);
    }

    @Test
    void updateCreatorProfile_deberiaLanzarExcepcionSiAliasCreadorEnUso() {
        CreadorContenido otroCreador = new CreadorContenido();
        otroCreador.setIdUsuario("c2");
        
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));
        when(creadorContenidoRepository.findByAliasCreador("takenCreatorAlias")).thenReturn(Optional.of(otroCreador));
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setAliasCreador("takenCreatorAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.updateCreatorProfile("c1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("alias de creador ya está en uso"));
    }

    @Test
    void updateCreatorProfile_deberiaActualizarDescripcion() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setDescripcion("Nueva descripción del creador");

        managementService.updateCreatorProfile("c1", dto);

        assertEquals("Nueva descripción del creador", creador.getDescripcion());
        verify(creadorContenidoRepository).save(creador);
    }

    @Test
    void updateCreatorProfile_deberiaLanzarExcepcionSiCreadorNoExiste() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.empty());
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setNombre("Test");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.updateCreatorProfile("c1", dto));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ============================================
    // TESTS PARA adminUpdateCreatorProfile
    // ============================================

    @Test
    void adminUpdateCreatorProfile_deberiaActualizarAliasCreador() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));
        when(creadorContenidoRepository.findByAliasCreador("adminCreatorAlias")).thenReturn(Optional.empty());
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setAliasCreador("adminCreatorAlias");

        managementService.adminUpdateCreatorProfile("c1", dto);

        assertEquals("adminCreatorAlias", creador.getAliasCreador());
        verify(creadorContenidoRepository).save(creador);
    }

    @Test
    void adminUpdateCreatorProfile_deberiaActualizarDescripcion() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setDescripcion("Descripción actualizada por admin");

        managementService.adminUpdateCreatorProfile("c1", dto);

        assertEquals("Descripción actualizada por admin", creador.getDescripcion());
        verify(creadorContenidoRepository).save(creador);
    }

    @Test
    void adminUpdateCreatorProfile_deberiaLanzarExcepcionSiCreadorNoExiste() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.empty());
        
        CreatorProfileUpdateDTO dto = new CreatorProfileUpdateDTO();
        dto.setNombre("Test");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.adminUpdateCreatorProfile("c1", dto));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ============================================
    // TESTS PARA updateAdminProfile
    // ============================================

    @Test
    void updateAdminProfile_deberiaActualizarNombre() {
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setNombre("NuevoNombreAdmin");

        managementService.updateAdminProfile("a1", dto);

        assertEquals("NuevoNombreAdmin", admin.getNombre());
        verify(adminRepository).save(admin);
    }

    @Test
    void updateAdminProfile_deberiaActualizarApellidos() {
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setApellidos("NuevosApellidosAdmin");

        managementService.updateAdminProfile("a1", dto);

        assertEquals("NuevosApellidosAdmin", admin.getApellidos());
        verify(adminRepository).save(admin);
    }

    @Test
    void updateAdminProfile_deberiaActualizarAlias() {
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        when(usuarioNormalRepository.findByalias("newAdminAlias")).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias("newAdminAlias")).thenReturn(Optional.empty());
        when(adminRepository.findByalias("newAdminAlias")).thenReturn(Optional.empty());
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setAlias("newAdminAlias");

        managementService.updateAdminProfile("a1", dto);

        assertEquals("newAdminAlias", admin.getAlias());
        verify(adminRepository).save(admin);
    }

    @Test
    void updateAdminProfile_deberiaLanzarExcepcionSiAliasEnUsoEnUsuarioNormal() {
        UsuarioNormal otroUsuario = new UsuarioNormal();
        otroUsuario.setIdUsuario("u2");
        
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        when(usuarioNormalRepository.findByalias("takenAlias")).thenReturn(Optional.of(otroUsuario));
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setAlias("takenAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.updateAdminProfile("a1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ============================================
    // TESTS PARA adminUpdateAdminProfile
    // ============================================

    @Test
    void adminUpdateAdminProfile_deberiaActualizarAlias() {
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        when(usuarioNormalRepository.findByalias("newAlias")).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias("newAlias")).thenReturn(Optional.empty());
        when(adminRepository.findByalias("newAlias")).thenReturn(Optional.empty());
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setAlias("newAlias");

        managementService.adminUpdateAdminProfile("a1", dto);

        assertEquals("newAlias", admin.getAlias());
        verify(adminRepository).save(admin);
    }

    @Test
    void adminUpdateAdminProfile_deberiaPermitirMismoAliasParaMismoAdmin() {
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setAlias("pedrol"); // Mismo alias que el admin actual

        managementService.adminUpdateAdminProfile("a1", dto);

        assertEquals("pedrol", admin.getAlias());
        verify(adminRepository).save(admin);
    }

    @Test
    void adminUpdateAdminProfile_deberiaLanzarExcepcionSiAliasEnUsoEnUsuarioNormal() {
        UsuarioNormal otroUsuario = new UsuarioNormal();
        otroUsuario.setIdUsuario("u2");
        
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        when(usuarioNormalRepository.findByalias("takenAlias")).thenReturn(Optional.of(otroUsuario));
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setAlias("takenAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.adminUpdateAdminProfile("a1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("alias ya está en uso"));
    }

    @Test
    void adminUpdateAdminProfile_deberiaLanzarExcepcionSiAliasEnUsoEnCreador() {
        CreadorContenido otroCreador = new CreadorContenido();
        otroCreador.setIdUsuario("c2");
        
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        when(usuarioNormalRepository.findByalias("takenAlias")).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias("takenAlias")).thenReturn(Optional.of(otroCreador));
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setAlias("takenAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.adminUpdateAdminProfile("a1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("alias ya está en uso"));
    }

    @Test
    void adminUpdateAdminProfile_deberiaLanzarExcepcionSiAliasEnUsoEnOtroAdmin() {
        Administrador otroAdmin = new Administrador();
        otroAdmin.setIdUsuario("a2");
        
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));
        when(usuarioNormalRepository.findByalias("takenAlias")).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias("takenAlias")).thenReturn(Optional.empty());
        when(adminRepository.findByalias("takenAlias")).thenReturn(Optional.of(otroAdmin));
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setAlias("takenAlias");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.adminUpdateAdminProfile("a1", dto));
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("alias ya está en uso"));
    }

    @Test
    void adminUpdateAdminProfile_deberiaLanzarExcepcionSiAdminNoExiste() {
        when(adminRepository.findById("a1")).thenReturn(Optional.empty());
        
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setNombre("Test");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.adminUpdateAdminProfile("a1", dto));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Administrador no encontrado"));
    }

    // ============================================
    // TESTS PARA deleteCreatorAsAdmin
    // ============================================

    @Test
    void deleteCreatorAsAdmin_deberiaEliminarCreadorYSusSesiones() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));

        managementService.deleteCreatorAsAdmin("c1");

        verify(sesionRepository).deleteByIdUsuario("c1");
        verify(creadorContenidoRepository).delete(creador);
    }

    @Test
    void deleteCreatorAsAdmin_deberiaLanzarExcepcionSiCreadorNoExiste() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.deleteCreatorAsAdmin("c1"));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Creador no encontrado"));
        verify(sesionRepository, never()).deleteByIdUsuario(anyString());
        verify(creadorContenidoRepository, never()).delete(any());
    }



    // ============================================
    // TESTS PARA deleteCreatorSelf
    // ============================================

    @Test
    void deleteCreatorSelf_deberiaEliminarCreadorYSusSesiones() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));

        managementService.deleteCreatorSelf("c1");

        verify(sesionRepository).deleteByIdUsuario("c1");
        verify(creadorContenidoRepository).delete(creador);
    }

    @Test
    void deleteCreatorSelf_deberiaLanzarExcepcionSiCreadorNoExiste() {
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.deleteCreatorSelf("c1"));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Creador no encontrado"));
        verify(sesionRepository, never()).deleteByIdUsuario(anyString());
        verify(creadorContenidoRepository, never()).delete(any());
    }

    // ============================================
    // TESTS PARA deleteUserSelf
    // ============================================

    @Test
    void deleteUserSelf_deberiaEliminarUsuarioYSusSesiones() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));

        managementService.deleteUserSelf("u1");

        verify(contenidoFavoritoRepository).deleteByIdUsuario("u1");  // 👈 AÑADIR
        verify(sesionRepository).deleteByIdUsuario("u1");
        verify(usuarioNormalRepository).delete(usuario);
    }


    @Test
    void deleteUserSelf_deberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> managementService.deleteUserSelf("u1"));
        
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Usuario no encontrado"));
        verify(sesionRepository, never()).deleteByIdUsuario(anyString());
        verify(usuarioNormalRepository, never()).delete(any());
    }

    @Test
    void testToggleCreatorBlock_success() {
        // Arrange
        when(creadorContenidoRepository.findById("c1")).thenReturn(Optional.of(creador));

        // Act
        managementService.toggleCreatorBlock("c1", true);

        // Assert
        assertTrue(creador.isBloqueado());
        verify(creadorContenidoRepository).save(creador);
    }

    @Test
    void testToggleCreatorBlock_notFound() {
        when(creadorContenidoRepository.findById("notFound"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> managementService.toggleCreatorBlock("notFound", true));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testDeleteAdminAsAdmin_selfDelete_notAllowed() {
        assertThrows(IllegalStateException.class, () ->
                managementService.deleteAdminAsAdmin("a1", "a1"));
    }

    @Test
    void testDeleteAdminAsAdmin_onlyOneAdmin() {
        when(adminRepository.count()).thenReturn(1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                managementService.deleteAdminAsAdmin("a1", "admin2"));

        assertEquals("No se puede eliminar el último administrador del sistema", ex.getMessage());
    }

    @Test
    void testDeleteAdminAsAdmin_adminToDeleteNotFound() {
        when(adminRepository.count()).thenReturn(2L);
        when(adminRepository.findById("a404")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                managementService.deleteAdminAsAdmin("a404", "admin2"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testDeleteAdminAsAdmin_success() {
        when(adminRepository.count()).thenReturn(3L);
        when(adminRepository.findById("a1")).thenReturn(Optional.of(admin));

        managementService.deleteAdminAsAdmin("a1", "adminMain");

        verify(sesionRepository).deleteByIdUsuario("a1");
        verify(adminRepository).delete(admin);
    }

    @Test
    void testChangeUserVipStatus_success() {
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));

        managementService.changeUserVipStatus("u1", true);

        assertTrue(usuario.isFlagVIP());
        verify(usuarioNormalRepository).save(usuario);
    }

    @Test
    void testChangeUserVipStatus_notFound() {
        when(usuarioNormalRepository.findById("unknown"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> managementService.changeUserVipStatus("unknown", true));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testGetUserVipStatus_success() {
        usuario.setFlagVIP(true);
        when(usuarioNormalRepository.findById("u1")).thenReturn(Optional.of(usuario));

        boolean vip = managementService.getUserVipStatus("u1");

        assertTrue(vip);
    }

    @Test
    void testGetUserVipStatus_notFound() {
        when(usuarioNormalRepository.findById("none"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> managementService.getUserVipStatus("none"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}