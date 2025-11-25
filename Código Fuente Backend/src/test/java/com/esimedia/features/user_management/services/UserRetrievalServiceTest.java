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
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.*;
import com.esimedia.features.auth.repository.*;
import com.esimedia.shared.util.JwtUtil;

import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
public class UserRetrievalServiceTest {

    @Mock private UsuarioNormalRepository usuarioNormalRepository;
    @Mock private CreadorContenidoRepository creadorContenidoRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserRetrievalService userRetrievalService;

    private static final String USER_ID = "user123";
    private static final String EMAIL = "test@example.com";
    private static final String ALIAS = "testuser";
    private static final String AUTH_HEADER = "Bearer validtoken";

    private UsuarioNormal usuarioNormal;
    private CreadorContenido creador;
    private Administrador admin;

    @BeforeEach
    void setUp() {
        usuarioNormal = new UsuarioNormal();
        usuarioNormal.setIdUsuario(USER_ID);
        usuarioNormal.setEmail(EMAIL);
        usuarioNormal.setAlias(ALIAS);

        creador = new CreadorContenido();
        creador.setIdUsuario("creador123");
        creador.setEmail("creador@example.com");
        creador.setAlias("creadoralias");

        admin = new Administrador();
        admin.setIdUsuario("admin123");
        admin.setEmail("admin@example.com");
        admin.setAlias("adminalias");
    }

    // ========== findByEmail ==========

    @Test
    void testFindByEmail_Found() {
        when(usuarioNormalRepository.findByemail(EMAIL)).thenReturn(Optional.of(usuarioNormal));

        Optional<UsuarioNormal> result = userRetrievalService.findByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertEquals(EMAIL, result.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(usuarioNormalRepository.findByemail(EMAIL)).thenReturn(Optional.empty());

        Optional<UsuarioNormal> result = userRetrievalService.findByEmail(EMAIL);

        assertFalse(result.isPresent());
    }

    // ========== findByAlias ==========

    @Test
    void testFindByAlias_Found() {
        when(usuarioNormalRepository.findByalias(ALIAS)).thenReturn(Optional.of(usuarioNormal));

        Optional<UsuarioNormal> result = userRetrievalService.findByAlias(ALIAS);

        assertTrue(result.isPresent());
        assertEquals(ALIAS, result.get().getAlias());
    }

    @Test
    void testFindByAlias_NotFound() {
        when(usuarioNormalRepository.findByalias(ALIAS)).thenReturn(Optional.empty());

        Optional<UsuarioNormal> result = userRetrievalService.findByAlias(ALIAS);

        assertFalse(result.isPresent());
    }

    // ========== findById ==========

    @Test
    void testFindById_Found() {
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(usuarioNormal));

        Optional<UsuarioNormal> result = userRetrievalService.findById(USER_ID);

        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get().getIdUsuario());
    }

    @Test
    void testFindById_NotFound() {
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Optional<UsuarioNormal> result = userRetrievalService.findById(USER_ID);

        assertFalse(result.isPresent());
    }

    // ========== findAnyUserById - Cubre 4 branches ==========

    @Test
    void testFindAnyUserById_UsuarioNormal() {
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(usuarioNormal));

        Optional<Usuario> result = userRetrievalService.findAnyUserById(USER_ID);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof UsuarioNormal);
    }

    @Test
    void testFindAnyUserById_Creador() {
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));

        Optional<Usuario> result = userRetrievalService.findAnyUserById(USER_ID);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof CreadorContenido);
    }

    @Test
    void testFindAnyUserById_Administrador() {
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        Optional<Usuario> result = userRetrievalService.findAnyUserById(USER_ID);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof Administrador);
    }

    @Test
    void testFindAnyUserById_NotFound() {
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Optional<Usuario> result = userRetrievalService.findAnyUserById(USER_ID);

        assertFalse(result.isPresent());
    }

    // ========== findAnyUserByAlias - Cubre 6 branches ==========

    @Test
    void testFindAnyUserByAlias_UsuarioNormal() {
        when(usuarioNormalRepository.findByalias(ALIAS)).thenReturn(Optional.of(usuarioNormal));

        Optional<Usuario> result = userRetrievalService.findAnyUserByAlias(ALIAS);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof UsuarioNormal);
    }

    @Test
    void testFindAnyUserByAlias_Creador() {
        when(usuarioNormalRepository.findByalias(ALIAS)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias(ALIAS)).thenReturn(Optional.of(creador));

        Optional<Usuario> result = userRetrievalService.findAnyUserByAlias(ALIAS);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof CreadorContenido);
    }

    @Test
    void testFindAnyUserByAlias_Administrador() {
        when(usuarioNormalRepository.findByalias(ALIAS)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias(ALIAS)).thenReturn(Optional.empty());
        when(adminRepository.findByalias(ALIAS)).thenReturn(Optional.of(admin));

        Optional<Usuario> result = userRetrievalService.findAnyUserByAlias(ALIAS);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof Administrador);
    }

    @Test
    void testFindAnyUserByAlias_NotFound() {
        when(usuarioNormalRepository.findByalias(ALIAS)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByAlias(ALIAS)).thenReturn(Optional.empty());
        when(adminRepository.findByalias(ALIAS)).thenReturn(Optional.empty());

        Optional<Usuario> result = userRetrievalService.findAnyUserByAlias(ALIAS);

        assertFalse(result.isPresent());
    }

    // ========== findAnyUserByEmail - Cubre 6 branches ==========

    @Test
    void testFindAnyUserByEmail_UsuarioNormal() {
        when(usuarioNormalRepository.findByemail(EMAIL)).thenReturn(Optional.of(usuarioNormal));

        Optional<Usuario> result = userRetrievalService.findAnyUserByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof UsuarioNormal);
    }

    @Test
    void testFindAnyUserByEmail_Creador() {
        when(usuarioNormalRepository.findByemail(EMAIL)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByemail(EMAIL)).thenReturn(Optional.of(creador));

        Optional<Usuario> result = userRetrievalService.findAnyUserByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof CreadorContenido);
    }

    @Test
    void testFindAnyUserByEmail_Administrador() {
        when(usuarioNormalRepository.findByemail(EMAIL)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByemail(EMAIL)).thenReturn(Optional.empty());
        when(adminRepository.findByemail(EMAIL)).thenReturn(Optional.of(admin));

        Optional<Usuario> result = userRetrievalService.findAnyUserByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof Administrador);
    }

    @Test
    void testFindAnyUserByEmail_NotFound() {
        when(usuarioNormalRepository.findByemail(EMAIL)).thenReturn(Optional.empty());
        when(creadorContenidoRepository.findByemail(EMAIL)).thenReturn(Optional.empty());
        when(adminRepository.findByemail(EMAIL)).thenReturn(Optional.empty());

        Optional<Usuario> result = userRetrievalService.findAnyUserByEmail(EMAIL);

        assertFalse(result.isPresent());
    }

    // ========== getUserFromAuthHeader - Cubre 4 branches + excepciones ==========

    @Test
    void testGetUserFromAuthHeader_UsuarioNormal() {
        when(jwtUtil.getUserIdFromToken("validtoken")).thenReturn(USER_ID);
        when(usuarioNormalRepository.existsById(USER_ID)).thenReturn(true);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(usuarioNormal));

        Usuario result = userRetrievalService.getUserFromAuthHeader(AUTH_HEADER);

        assertNotNull(result);
        assertTrue(result instanceof UsuarioNormal);
    }

    @Test
    void testGetUserFromAuthHeader_Creador() {
        when(jwtUtil.getUserIdFromToken("validtoken")).thenReturn(USER_ID);
        when(usuarioNormalRepository.existsById(USER_ID)).thenReturn(false);
        when(creadorContenidoRepository.existsById(USER_ID)).thenReturn(true);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));

        Usuario result = userRetrievalService.getUserFromAuthHeader(AUTH_HEADER);

        assertNotNull(result);
        assertTrue(result instanceof CreadorContenido);
    }

    @Test
    void testGetUserFromAuthHeader_Administrador() {
        when(jwtUtil.getUserIdFromToken("validtoken")).thenReturn(USER_ID);
        when(usuarioNormalRepository.existsById(USER_ID)).thenReturn(false);
        when(creadorContenidoRepository.existsById(USER_ID)).thenReturn(false);
        when(adminRepository.existsById(USER_ID)).thenReturn(true);
        when(adminRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        Usuario result = userRetrievalService.getUserFromAuthHeader(AUTH_HEADER);

        assertNotNull(result);
        assertTrue(result instanceof Administrador);
    }

    @Test
    void testGetUserFromAuthHeader_NotFound() {
        when(jwtUtil.getUserIdFromToken("validtoken")).thenReturn(USER_ID);
        when(usuarioNormalRepository.existsById(USER_ID)).thenReturn(false);
        when(creadorContenidoRepository.existsById(USER_ID)).thenReturn(false);
        when(adminRepository.existsById(USER_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
            userRetrievalService.getUserFromAuthHeader(AUTH_HEADER));
    }

    @Test
    void testGetUserFromAuthHeader_InvalidToken_IllegalArgumentException() {
        when(jwtUtil.getUserIdFromToken(anyString())).thenThrow(new IllegalArgumentException("Invalid token"));

        assertThrows(ResponseStatusException.class, () ->
            userRetrievalService.getUserFromAuthHeader(AUTH_HEADER));
    }

    @Test
    void testGetUserFromAuthHeader_InvalidToken_JwtException() {
        when(jwtUtil.getUserIdFromToken(anyString())).thenThrow(new JwtException("JWT error"));

        assertThrows(ResponseStatusException.class, () ->
            userRetrievalService.getUserFromAuthHeader(AUTH_HEADER));
    }

    @Test
    void testGetUserFromAuthHeader_ExistsButNotFound() {
        when(jwtUtil.getUserIdFromToken("validtoken")).thenReturn(USER_ID);
        when(usuarioNormalRepository.existsById(USER_ID)).thenReturn(true);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            userRetrievalService.getUserFromAuthHeader(AUTH_HEADER));
    }
}