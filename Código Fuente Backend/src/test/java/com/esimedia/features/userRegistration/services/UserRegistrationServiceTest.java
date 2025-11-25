package com.esimedia.features.userRegistration.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.dto.AdministradorDTO;
import com.esimedia.features.auth.dto.CreadorContenidoDTO;
import com.esimedia.features.auth.dto.UsuarioNormalDTO;
import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Token;
import com.esimedia.features.auth.entity.UsuarioFactory;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.enums.TipoToken;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.TokenRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

import com.esimedia.features.auth.services.EmailService;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.user_registration.services.UserRegistrationService;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UsuarioNormalRepository usuarioNormalRepository;

    @Mock
    private CreadorContenidoRepository creadorContenidoRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ValidationService validationService;

    @Mock
    private UsuarioFactory usuarioFactory;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private UsuarioNormalDTO usuarioDTO;
    private CreadorContenidoDTO creadorDTO;
    private AdministradorDTO adminDTO;
    private UsuarioNormal usuario;
    private CreadorContenido creador;
    private Administrador admin;
    private Date fechaNacimiento;

    @BeforeEach
    void setUp() {
        LocalDate fecha = LocalDate.now().minusYears(25);
        fechaNacimiento = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Usuario Normal DTO
        usuarioDTO = new UsuarioNormalDTO();
        usuarioDTO.setNombre("Juan");
        usuarioDTO.setApellidos("Pérez");
        usuarioDTO.setEmail("juan@gmail.com");
        usuarioDTO.setAlias("juanp");
        usuarioDTO.setPassword("Password123!");
        usuarioDTO.setFechaNacimiento(fechaNacimiento);

        // Usuario Normal Entity
        usuario = new UsuarioNormal();
        usuario.setIdUsuario("u123");
        usuario.setNombre("Juan");
        usuario.setApellidos("Pérez");
        usuario.setEmail("juan@gmail.com");
        usuario.setAlias("juanp");
        usuario.setPassword("hashedPassword");
        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setConfirmado(false);

        // Creador DTO
        creadorDTO = new CreadorContenidoDTO();
        creadorDTO.setNombre("María");
        creadorDTO.setApellidos("García");
        creadorDTO.setEmail("maria@gmail.com");
        creadorDTO.setAlias("mariag");
        creadorDTO.setAliasCreador("mariaCreator");
        creadorDTO.setPassword("Password123!");
        creadorDTO.setTipoContenido(TipoContenido.VIDEO);

        // Creador Entity
        creador = new CreadorContenido();
        creador.setIdUsuario("c123");
        creador.setNombre("María");
        creador.setApellidos("García");
        creador.setEmail("maria@gmail.com");
        creador.setAlias("mariag");
        creador.setAliasCreador("mariaCreator");
        creador.setPassword("hashedPassword");
        creador.setTipoContenido(TipoContenido.VIDEO);
        creador.setValidado(false);

        // Admin DTO
        adminDTO = new AdministradorDTO();
        adminDTO.setNombre("Pedro");
        adminDTO.setApellidos("López");
        adminDTO.setEmail("pedro@esimedia.admin");
        adminDTO.setAlias("pedrol");
        adminDTO.setPassword("Password123!");
        adminDTO.setDepartamento("IT");

        // Admin Entity
        admin = new Administrador();
        admin.setIdUsuario("a123");
        admin.setNombre("Pedro");
        admin.setApellidos("López");
        admin.setEmail("pedro@esimedia.admin");
        admin.setAlias("pedrol");
        admin.setPassword("hashedPassword");
        admin.setDepartamento("IT");
    }

    // ========================================================================
    // registerNormalUser - SUCCESS CASES
    // ========================================================================

    // ========================================================================
    // registerNormalUser - VALIDATION FAILURES
    // ========================================================================


    @Test
    void registerNormalUser_FactoryThrowsIllegalStateException() {
        when(usuarioFactory.crearUsuario(any(UsuarioNormalDTO.class), any(ValidationService.class)))
                .thenThrow(new IllegalStateException("Factory error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerNormalUser(usuarioDTO));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Factory error"));
    }

    @Test
    void registerNormalUser_DatabaseError_ThrowsInternalServerError() throws Exception {
        when(usuarioFactory.crearUsuario(any(UsuarioNormalDTO.class), any(ValidationService.class)))
                .thenReturn(usuario);
        when(usuarioNormalRepository.save(any(UsuarioNormal.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerNormalUser(usuarioDTO));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }

    @Test
    void registerNormalUser_CleanupFailsAfterError() throws Exception {
        when(usuarioFactory.crearUsuario(any(UsuarioNormalDTO.class), any(ValidationService.class)))
                .thenReturn(usuario);
        when(usuarioNormalRepository.save(any(UsuarioNormal.class)))
                .thenThrow(new RuntimeException("Database error"));
        doThrow(new RuntimeException("Cleanup error"))
                .when(usuarioNormalRepository).deleteById(anyString());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerNormalUser(usuarioDTO));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        verify(usuarioNormalRepository).deleteById(usuario.getIdUsuario());
    }

    @Test
    void registerNormalUser_CleanupWithNullId() throws Exception {
        UsuarioNormal usuarioSinId = new UsuarioNormal();
        usuarioSinId.setIdUsuario(null);
        
        when(usuarioFactory.crearUsuario(any(UsuarioNormalDTO.class), any(ValidationService.class)))
                .thenReturn(usuarioSinId);
        when(usuarioNormalRepository.save(any(UsuarioNormal.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerNormalUser(usuarioDTO));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        verify(usuarioNormalRepository, never()).deleteById(anyString());
    }

    // ========================================================================
    // registerCreator - SUCCESS CASES
    // ========================================================================

    @Test
    void registerCreator_Success() {
        when(usuarioFactory.crearUsuario(any(CreadorContenidoDTO.class), any(ValidationService.class)))
                .thenReturn(creador);
        when(creadorContenidoRepository.save(any(CreadorContenido.class))).thenReturn(creador);

        String result = userRegistrationService.registerCreator(creadorDTO);

        assertEquals("¡Registro enviado! Un administrador tiene que validar tu solicitud de creador de contenido.", result);
        verify(creadorContenidoRepository).save(any(CreadorContenido.class));
    }

    // ========================================================================
    // registerCreator - FAILURE CASES
    // ========================================================================

    @Test
    void registerCreator_IllegalArgumentException_ThrowsBadRequest() {
        when(usuarioFactory.crearUsuario(any(CreadorContenidoDTO.class), any(ValidationService.class)))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerCreator(creadorDTO));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("formato de la solicitud es incorrecto"));
    }

    @Test
    void registerCreator_InternalError_ThrowsInternalServerError() {
        when(usuarioFactory.crearUsuario(any(CreadorContenidoDTO.class), any(ValidationService.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerCreator(creadorDTO));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Error interno durante el registro del creador"));
    }

    // ========================================================================
    // registerAdmin - SUCCESS CASES
    // ========================================================================

    @Test
    void registerAdmin_Success() {
        when(usuarioFactory.crearUsuario(any(AdministradorDTO.class), any(ValidationService.class)))
                .thenReturn(admin);
        when(adminRepository.save(any(Administrador.class))).thenReturn(admin);

        String result = userRegistrationService.registerAdmin(adminDTO);

        assertEquals("Administrador registrado exitosamente.", result);
        verify(validationService).validateAdminUniqueness(adminDTO.getEmail(), adminDTO.getAlias());
        verify(adminRepository).save(any(Administrador.class));
    }

    // ========================================================================
    // registerAdmin - FAILURE CASES
    // ========================================================================

    @Test
    void registerAdmin_EmailExists_ThrowsConflict() {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con este email"))
                .when(validationService).validateAdminUniqueness(anyString(), anyString());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerAdmin(adminDTO));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Ya existe un usuario registrado con este email"));
    }

    @Test
    void registerAdmin_AliasExists_ThrowsConflict() {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con este alias"))
                .when(validationService).validateAdminUniqueness(anyString(), anyString());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerAdmin(adminDTO));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Ya existe un usuario registrado con este alias"));
    }


    @Test
    void registerAdmin_IllegalArgumentException_ThrowsBadRequest() {
        when(usuarioFactory.crearUsuario(any(AdministradorDTO.class), any(ValidationService.class)))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerAdmin(adminDTO));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Datos de administrador inválidos"));
    }

    @Test
    void registerAdmin_InternalError_ThrowsInternalServerError() {
        when(usuarioFactory.crearUsuario(any(AdministradorDTO.class), any(ValidationService.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.registerAdmin(adminDTO));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Error interno durante el registro del administrador"));
    }

    // ========================================================================
    // confirmUserAccount - SUCCESS CASES
    // ========================================================================

    @Test
    void confirmUserAccount_Success() {
        String tokenValue = "validToken123";
        Token token = Token.builder()
                .id(usuario.getIdUsuario())
                .tokenCreado(tokenValue)
                .tipoToken(TipoToken.CONFIRMACION_CUENTA)
                .usuarioEmail(usuario.getEmail())
                .fechaInicio(LocalDateTime.now())
                .fechaUltimaActividad(LocalDateTime.now().plusDays(1))
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();

        when(tokenRepository.findAll()).thenReturn(List.of(token));
        when(usuarioNormalRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
        when(usuarioNormalRepository.save(any(UsuarioNormal.class))).thenReturn(usuario);
        when(tokenRepository.save(any(Token.class))).thenReturn(token);

        String result = userRegistrationService.confirmUserAccount(tokenValue);

        assertEquals("Cuenta validada exitosamente", result);
        assertTrue(usuario.isConfirmado());
        assertEquals(EstadoToken.UTILIZADA, token.getEstado());
        verify(usuarioNormalRepository).save(usuario);
        verify(tokenRepository).save(token);
    }

    // ========================================================================
    // confirmUserAccount - FAILURE CASES
    // ========================================================================

    @Test
    void confirmUserAccount_TokenNotFound_ThrowsBadRequest() {
        when(tokenRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.confirmUserAccount("invalidToken"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Token de confirmación inválido"));
    }

    @Test
    void confirmUserAccount_TokenInvalid_ThrowsBadRequest() {
        String tokenValue = "expiredToken";
        Token token = Token.builder()
                .id(usuario.getIdUsuario())
                .tokenCreado(tokenValue)
                .tipoToken(TipoToken.CONFIRMACION_CUENTA)
                .usuarioEmail(usuario.getEmail())
                .fechaInicio(LocalDateTime.now().minusDays(2))
                .fechaUltimaActividad(LocalDateTime.now().minusDays(2))
                .estado(EstadoToken.EXPIRADA)
                .build();

        when(tokenRepository.findAll()).thenReturn(List.of(token));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.confirmUserAccount(tokenValue));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Token inválido"));
    }

    @Test
    void confirmUserAccount_UserNotFound_ThrowsNotFound() {
        String tokenValue = "validToken123";
        Token token = Token.builder()
                .id("nonexistentUser")
                .tokenCreado(tokenValue)
                .tipoToken(TipoToken.CONFIRMACION_CUENTA)
                .usuarioEmail("nonexistent@example.com")
                .fechaInicio(LocalDateTime.now())
                .fechaUltimaActividad(LocalDateTime.now().plusDays(1))
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();

        when(tokenRepository.findAll()).thenReturn(List.of(token));
        when(usuarioNormalRepository.findById("nonexistentUser")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.confirmUserAccount(tokenValue));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Usuario no encontrado"));
    }

    @Test
    void confirmUserAccount_AlreadyConfirmed_ThrowsBadRequest() {
        String tokenValue = "validToken123";
        Token token = Token.builder()
                .id(usuario.getIdUsuario())
                .tokenCreado(tokenValue)
                .tipoToken(TipoToken.CONFIRMACION_CUENTA)
                .usuarioEmail(usuario.getEmail())
                .fechaInicio(LocalDateTime.now())
                .fechaUltimaActividad(LocalDateTime.now().plusDays(1))
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();

        usuario.setConfirmado(true);

        when(tokenRepository.findAll()).thenReturn(List.of(token));
        when(usuarioNormalRepository.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.confirmUserAccount(tokenValue));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("La cuenta ya está validada"));
    }

    @Test
    void confirmUserAccount_InternalError_ThrowsInternalServerError() {
        String tokenValue = "validToken123";
        when(tokenRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.confirmUserAccount(tokenValue));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Error interno durante la confirmación"));
    }

    @Test
    void confirmUserAccount_WrongTokenType_NotFound() {
        String tokenValue = "validToken123";
        Token wrongTypeToken = Token.builder()
                .id(usuario.getIdUsuario())
                .tokenCreado(tokenValue)
                .tipoToken(TipoToken.RECUPERACION_PASSWORD)  // Tipo incorrecto
                .usuarioEmail(usuario.getEmail())
                .fechaInicio(LocalDateTime.now())
                .fechaUltimaActividad(LocalDateTime.now().plusDays(1))
                .estado(EstadoToken.SIN_CONFIRMAR)
                .build();

        when(tokenRepository.findAll()).thenReturn(List.of(wrongTypeToken));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userRegistrationService.confirmUserAccount(tokenValue));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Token de confirmación inválido"));
    }
}