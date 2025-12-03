package com.esimedia.features.auth.services;

import com.esimedia.features.auth.dto.LoginRequestDTO;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.shared.util.JwtUtil;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.esimedia.features.user_management.services.UserRetrievalService;
import com.esimedia.features.auth.entity.Administrador;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UsuarioNormalRepository usuarioNormalRepository;
    @Mock
    private CreadorContenidoRepository creadorContenidoRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private TwoFactorAuthService twoFactorAuthService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private UserRetrievalService userRetrievalService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UsuarioNormal user;
    private static final String PEPPER = "pepper123";
    private static final String RAW_PASSWORD = "myPassword123";
    private String hashedPassword;

    @BeforeEach
    void setUp() throws Exception {
        // Hashear password con Argon2
        hashedPassword = hashPasswordWithPepper(RAW_PASSWORD, PEPPER);

        user = new UsuarioNormal();
        user.setIdUsuario("u1");
        user.setEmail("test@example.com");
        user.setAlias("alias");
        user.setPassword(hashedPassword);
        user.setConfirmado(true);
        user.setTwoFactorEnabled(false);
        user.setCredentialsVersion(1);
        user.setRol(Rol.NORMAL);

        // Inyectar "pepper" a través de reflexión
        Field pepperField = AuthenticationService.class.getDeclaredField("pepper");
        pepperField.setAccessible(true);
        pepperField.set(authenticationService, PEPPER);

        // Mock userRetrievalService con lenient() para evitar UnnecessaryStubbing
        lenient().when(userRetrievalService.findAnyUserByAlias("alias")).thenReturn(Optional.of(user));
        lenient().when(userRetrievalService.findAnyUserById("u1")).thenReturn(Optional.of(user));
    }

    // ============================
    // Helpers
    // ============================

    private String hashPasswordWithPepper(String rawPassword, String pepper) {
        String passwordWithPepper = rawPassword + pepper;
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            return argon2.hash(2, 65536, 1, passwordWithPepper.toCharArray());
        } 
        finally {
            argon2.wipeArray(passwordWithPepper.toCharArray());
        }
    }

    private String invokeMaskEmail(String email) {
        try {
            var method = AuthenticationService.class.getDeclaredMethod("maskEmail", String.class);
            method.setAccessible(true);
            return (String) method.invoke(authenticationService, email);
        } 
		catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean invokeMatchesPassword(String raw, String encoded) {
        try {
            var method = AuthenticationService.class.getDeclaredMethod("matchesPassword", String.class, String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(authenticationService, raw, encoded);
        } 
		catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object createSession(String userId, int step) throws Exception {
        var sessionClass = Class.forName("com.esimedia.features.auth.services.AuthenticationService$AuthSession");
        var constructor = sessionClass.getDeclaredConstructor(String.class, int.class);
        constructor.setAccessible(true);
        return constructor.newInstance(userId, step);
    }

    // ============================
    // JWT TOKEN
    // ============================

    @Test
    void generateJwtToken_deberiaLlamarAlJwtUtil() {
        when(jwtUtil.generateToken("u1", "test@example.com", 1, Rol.NORMAL))
            .thenReturn("token123");
        
        String token = authenticationService.generateJwtToken(user);
        
        assertEquals("token123", token);
        verify(jwtUtil).generateToken("u1", "test@example.com", 1, Rol.NORMAL);
    }

    @Test
    void generateJwtToken_conTipoContenido_deberiaLlamarAlJwtUtil() {
        when(jwtUtil.generateToken("u1", "test@example.com", 1, Rol.NORMAL, TipoContenido.VIDEO))
            .thenReturn("tokenVideo123");
        
        String token = authenticationService.generateJwtToken(user, TipoContenido.VIDEO);
        
        assertEquals("tokenVideo123", token);
        verify(jwtUtil).generateToken("u1", "test@example.com", 1, Rol.NORMAL, TipoContenido.VIDEO);
    }

    // ============================
    // LOGIN
    // ============================

    @Test
    void login_deberiaLanzarBadRequestSiEmailEsNull() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(null);
        dto.setPassword("123");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
                () -> authenticationService.login(dto));
        assertTrue(ex.getReason().contains("Email y contraseña"));
    }

    @Test
    void login_deberiaLanzarForbiddenSiUsuarioBloqueado() {
        when(loginAttemptService.isBlocked(eq("test@example.com"), anyString())).thenReturn(true);
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("123");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
                () -> authenticationService.login(dto));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void login_deberiaLanzarUnauthorizedSiUsuarioNoExiste() {
        when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
        when(usuarioNormalRepository.findByemail("test@example.com")).thenReturn(Optional.empty());
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("123");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
                () -> authenticationService.login(dto));
        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void login_deberiaRetornarTokenSiLoginExitoso() {
        lenient().when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
        lenient().when(usuarioNormalRepository.findByemail("test@example.com")).thenReturn(Optional.of(user));
        
        lenient().when(jwtUtil.generateToken("u1", "test@example.com", 1, Rol.NORMAL))
            .thenReturn("mockToken");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@example.com");
        dto.setPassword(RAW_PASSWORD);

        boolean match = invokeMatchesPassword(RAW_PASSWORD, user.getPassword());
        assertTrue(match);

        var response = authenticationService.login(dto);
        assertEquals("mockToken", response.getToken());
        verify(loginAttemptService).resetAttempts(eq("test@example.com"), anyString());
    }

    // ============================
    // AUTHENTICATE
    // ============================

    @Test
    void authenticate_deberiaLanzarBadRequestSiFaltaEmail() {
        Map<String, String> cred = Map.of("password", "123");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticate(cred));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Email y contraseña son requeridos"));
    }

    @Test
    void authenticate_deberiaLanzarBadRequestSiFaltaPassword() {
        Map<String, String> cred = Map.of("email", "test@example.com");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticate(cred));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void authenticate_deberiaLanzarUnauthorizedSiUsuarioNoExiste() {
        Map<String, String> cred = Map.of("email", "noexiste@example.com", "password", "123");
        when(usuarioNormalRepository.findByemail("noexiste@example.com")).thenReturn(Optional.empty());
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticate(cred));
        assertEquals(401, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Credenciales inválidas"));
    }

    @Test
    void authenticate_deberiaLanzarUnauthorizedSiUsuarioNoConfirmado() {
        UsuarioNormal usuarioNoConfirmado = new UsuarioNormal();
        usuarioNoConfirmado.setEmail("test@example.com");
        usuarioNoConfirmado.setPassword(hashedPassword);
        usuarioNoConfirmado.setConfirmado(false);
        
        Map<String, String> cred = Map.of("email", "test@example.com", "password", RAW_PASSWORD);
        when(usuarioNormalRepository.findByemail("test@example.com")).thenReturn(Optional.of(usuarioNoConfirmado));
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticate(cred));
        assertEquals(401, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("La cuenta no ha sido confirmada"));
    }

    @Test
    void authenticate_deberiaLanzarUnauthorizedSiPasswordIncorrecta() {
        Map<String, String> cred = Map.of("email", "test@example.com", "password", "wrongPassword");
        when(usuarioNormalRepository.findByemail("test@example.com")).thenReturn(Optional.of(user));
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticate(cred));
        assertEquals(401, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Credenciales inválidas"));
    }

    @Test
    void authenticate_deberiaLanzarForbiddenSiUsuarioBloqueado() {
        UsuarioNormal usuarioBloqueado = new UsuarioNormal();
        usuarioBloqueado.setIdUsuario("u1");
        usuarioBloqueado.setEmail("test@example.com");
        usuarioBloqueado.setPassword(hashedPassword);
        usuarioBloqueado.setConfirmado(true);
        usuarioBloqueado.setBloqueado(true);
        
        Map<String, String> cred = Map.of("email", "test@example.com", "password", RAW_PASSWORD);
        when(usuarioNormalRepository.findByemail("test@example.com")).thenReturn(Optional.of(usuarioBloqueado));
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticate(cred));
        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("bloqueada"));
    }

    @Test
    void authenticate_deberiaRetornarUsuarioSiAutenticacionExitosa() {
        Map<String, String> cred = Map.of("email", "test@example.com", "password", RAW_PASSWORD);
        when(usuarioNormalRepository.findByemail("test@example.com")).thenReturn(Optional.of(user));
        
        Usuario result = authenticationService.authenticate(cred);
        
        assertNotNull(result);
        assertEquals(user, result);
        assertEquals("test@example.com", result.getEmail());
    }

    // ============================
    // AUTHENTICATE PRIVILEGED USER
    // ============================

    @Test
    void authenticatePrivilegedUser_deberiaLanzarBadRequestSiFaltanCampos() {
        Map<String, String> cred = Map.of("email", "mail@example.com");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticatePrivilegedUser(cred));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void authenticatePrivilegedUser_deberiaLanzarUnauthorizedSiNoExisteUsuario() {
        Map<String, String> cred = Map.of("email", "mail@example.com", "password", "123");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticatePrivilegedUser(cred));
        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void authenticatePrivilegedUser_deberiaLanzarUnauthorizedSiPasswordIncorrectaParaCreador() {
        CreadorContenido creador = new CreadorContenido();
        creador.setEmail("mail@example.com");
        creador.setPassword(hashedPassword);
        creador.setValidado(true);
        when(creadorContenidoRepository.findByemail("mail@example.com")).thenReturn(Optional.of(creador));

        // matchesPassword real (password incorrecta)
        boolean match = invokeMatchesPassword("wrongPassword", creador.getPassword());
        assertFalse(match);

        Map<String, String> cred = Map.of("email", "mail@example.com", "password", "wrongPassword");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticatePrivilegedUser(cred));
        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void authenticatePrivilegedUser_deberiaRetornarUsuarioSiPasswordCorrectaParaCreador() {
        CreadorContenido creador = new CreadorContenido();
        creador.setEmail("mail@example.com");
        creador.setPassword(hashedPassword);
        creador.setValidado(true);
        when(creadorContenidoRepository.findByemail("mail@example.com")).thenReturn(Optional.of(creador));

        // matchesPassword real
        boolean match = invokeMatchesPassword(RAW_PASSWORD, creador.getPassword());
        assertTrue(match);

        Map<String, String> cred = Map.of("email", "mail@example.com", "password", RAW_PASSWORD);
        var result = authenticationService.authenticatePrivilegedUser(cred);
        assertEquals(creador, result);
    }

    @Test
    void authenticatePrivilegedUser_deberiaLanzarUnauthorizedSiCreadorNoValidado() {
        CreadorContenido creadorNoValidado = new CreadorContenido();
        creadorNoValidado.setEmail("mail@example.com");
        creadorNoValidado.setPassword(hashedPassword);
        creadorNoValidado.setValidado(false);
        when(creadorContenidoRepository.findByemail("mail@example.com")).thenReturn(Optional.of(creadorNoValidado));

        Map<String, String> cred = Map.of("email", "mail@example.com", "password", RAW_PASSWORD);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticatePrivilegedUser(cred));
        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("no ha sido validado"));
    }

    @Test
    void authenticatePrivilegedUser_deberiaRetornarAdminSiPasswordCorrecta() {
        Administrador admin = new Administrador();
        admin.setEmail("admin@example.com");
        admin.setPassword(hashedPassword);
        admin.setIdUsuario("admin1");
        
        // ⚠️ ANTES:
        // when(creadorContenidoRepository.findByemail("admin@example.com")).thenReturn(Optional.empty());
        // when(adminRepository.findByemail("admin@example.com")).thenReturn(Optional.of(admin));

        // ✅ DESPUÉS (lenient):
        lenient().when(creadorContenidoRepository.findByemail("admin@example.com"))
                .thenReturn(Optional.empty());
        lenient().when(adminRepository.findByemail("admin@example.com"))
                .thenReturn(Optional.of(admin));

        Map<String, String> cred = Map.of("email", "admin@example.com", "password", RAW_PASSWORD);
        var result = authenticationService.authenticatePrivilegedUser(cred);
        
        assertEquals(admin, result);
        assertEquals("admin@example.com", result.getEmail());
    }

    @Test
    void authenticatePrivilegedUser_deberiaLanzarUnauthorizedSiPasswordIncorrectaParaAdmin() {
        Administrador admin = new Administrador();
        admin.setEmail("admin@example.com");
        admin.setPassword(hashedPassword);
        
        // ⚠️ ANTES:
        // when(creadorContenidoRepository.findByemail("admin@example.com")).thenReturn(Optional.empty());
        // when(adminRepository.findByemail("admin@example.com")).thenReturn(Optional.of(admin));

        // ✅ DESPUÉS (lenient):
        lenient().when(creadorContenidoRepository.findByemail("admin@example.com"))
                .thenReturn(Optional.empty());
        lenient().when(adminRepository.findByemail("admin@example.com"))
                .thenReturn(Optional.of(admin));

        Map<String, String> cred = Map.of("email", "admin@example.com", "password", "wrongPassword");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticatePrivilegedUser(cred));
        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void authenticatePrivilegedUser_deberiaLanzarForbiddenSiCreadorBloqueado() {
        CreadorContenido creadorBloqueado = new CreadorContenido();
        creadorBloqueado.setEmail("creador@example.com");
        creadorBloqueado.setPassword(hashedPassword);
        creadorBloqueado.setValidado(true);
        creadorBloqueado.setBloqueado(true);
        
        when(creadorContenidoRepository.findByemail("creador@example.com")).thenReturn(Optional.of(creadorBloqueado));

        Map<String, String> cred = Map.of("email", "creador@example.com", "password", RAW_PASSWORD);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticatePrivilegedUser(cred));
        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("bloqueada"));
    }
}