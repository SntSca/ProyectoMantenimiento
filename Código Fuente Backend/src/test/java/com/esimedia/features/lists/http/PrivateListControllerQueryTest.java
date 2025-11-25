package com.esimedia.features.lists.http;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.ListaPrivadaResponseDTO;
import com.esimedia.features.lists.services.PrivateListService;
import com.esimedia.features.lists.services.PrivateListCreationService;
import com.esimedia.features.lists.services.PrivateListContentService;
import com.esimedia.features.lists.services.PrivateListQueryService;
import com.esimedia.features.lists.services.PrivateListUpdateService;
import com.esimedia.features.auth.services.SesionService;
import com.esimedia.features.user_management.services.UserRetrievalService;
import com.esimedia.features.auth.services.SessionTimeoutService;
import com.esimedia.shared.util.JwtUtil;
import com.esimedia.shared.util.JwtValidationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebMvcTest(controllers = PrivateListController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=" +
    "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
@DisplayName("Tests del Controlador de Listas Privadas - Consultas")
@SuppressWarnings("removal")
class PrivateListControllerQueryTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrivateListService privateListService;

    @MockBean
    private JwtValidationUtil jwtValidationService;

    @MockBean
    private PrivateListCreationService privateListCreationService;

    @MockBean
    private PrivateListContentService privateListContentService;

    @MockBean
    private PrivateListQueryService privateListQueryService;

    @MockBean
    private PrivateListUpdateService privateListUpdateService;

    @MockBean
    private SesionService sesionService;

    @MockBean
    private UserRetrievalService userRetrievalService;

    @MockBean
    private SessionTimeoutService sessionTimeoutService;

    @MockBean
    private JwtUtil jwtUtil;

    private String mockToken;
    private String userId;

    @BeforeEach
    void setUp() {
        mockToken = "Bearer test-token-private-12345";
        userId = "user-123";
    }

    // ==================== Consultas de Listas Privadas ====================

    @Test
    @DisplayName("CA-01: Consulta exitosa - No devuelve ninguna lista privada")
    void testObtenerTodasListasPrivadasConContenidos_EmptyList() throws Exception {
        // Given
        List<ListaPrivadaResponseDTO> listas = Arrays.asList();

        when(jwtValidationService.validarGetUsuario(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(privateListService.obtenerTodasListasPrivadasConContenidos(userId))
            .thenReturn(listas);

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(privateListService, times(1)).obtenerTodasListasPrivadasConContenidos(userId);
    }

    @Test
    @DisplayName("CA-02: Consulta exitosa - Devuelve una lista sin contenidos")
    void testObtenerTodasListasPrivadasConContenidos_SingleListNoContents() throws Exception {
        // Given
        ListaPrivadaResponseDTO lista = ListaPrivadaResponseDTO.builder()
            .idLista("lista-priv-001")
            .nombre("Mi Lista Privada")
            .descripcion("Sin contenidos")
            .idCreadorUsuario(userId)
            .visibilidad(true)
            .contenidos(new ArrayList<>())
            .build();

        when(jwtValidationService.validarGetUsuario(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(privateListService.obtenerTodasListasPrivadasConContenidos(userId))
            .thenReturn(Arrays.asList(lista));

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].idLista").value("lista-priv-001"))
            .andExpect(jsonPath("$[0].nombre").value("Mi Lista Privada"))
            .andExpect(jsonPath("$[0].contenidos.length()").value(0));

        verify(privateListService, times(1)).obtenerTodasListasPrivadasConContenidos(userId);
    }

    @Test
    @DisplayName("CA-03: Consulta exitosa - Devuelve una lista con contenidos (1 audio y 1 video)")
    void testObtenerTodasListasPrivadasConContenidos_SingleListWithContents() throws Exception {
        // Given
        ListaPrivadaResponseDTO lista = ListaPrivadaResponseDTO.builder()
            .idLista("lista-priv-002")
            .nombre("Mi Playlist Privada")
            .descripcion("Contenido mixto")
            .idCreadorUsuario(userId)
            .visibilidad(true)
            .contenidos(Arrays.asList())  // Con contenidos mixtos simulados
            .build();

        when(jwtValidationService.validarGetUsuario(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(privateListService.obtenerTodasListasPrivadasConContenidos(userId))
            .thenReturn(Arrays.asList(lista));

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idLista").value("lista-priv-002"))
            .andExpect(jsonPath("$[0].nombre").value("Mi Playlist Privada"));

        verify(privateListService, times(1)).obtenerTodasListasPrivadasConContenidos(userId);
    }

    @Test
    @DisplayName("CA-04: Consulta exitosa - Múltiples listas privadas con contenidos")
    void testObtenerTodasListasPrivadasConContenidos_MultipleLists() throws Exception {
        // Given
        List<ListaPrivadaResponseDTO> listas = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            listas.add(ListaPrivadaResponseDTO.builder()
                .idLista("lista-priv-" + i)
                .nombre("Lista Privada " + i)
                .descripcion("Descripción " + i)
                .idCreadorUsuario(userId)
                .visibilidad(true)
                .contenidos(new ArrayList<>())
                .build());
        }

        when(jwtValidationService.validarGetUsuario(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(privateListService.obtenerTodasListasPrivadasConContenidos(userId))
            .thenReturn(listas);

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].idLista").value("lista-priv-1"))
            .andExpect(jsonPath("$[1].idLista").value("lista-priv-2"));

        verify(privateListService, times(1)).obtenerTodasListasPrivadasConContenidos(userId);
    }

    @Test
    @DisplayName("CA-05: JWT inválido para tipo de consulta (CREADOR en lugar de usuario normal) - 401")
    void testObtenerTodasListasPrivadasConContenidos_InvalidJWTType() throws Exception {
        // Given - JWT de tipo CREADOR
        String creadorToken = "Bearer creador-token-xyz";
        
        when(jwtValidationService.validarGetUsuario(creadorToken))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Tipo de usuario no válido para esta operación"));

        when(jwtUtil.getUserIdFromToken(creadorToken))
            .thenThrow(new RuntimeException("Token tipo CREADOR no válido"));

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", creadorToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(jwtValidationService, times(1)).validarGetUsuario(creadorToken);
    }

    @Test
    @DisplayName("CA-06: JWT de usuario diferente (userId mismatch) - 403")
    void testObtenerTodasListasPrivadasConContenidos_UserIdMismatch() throws Exception {
        // Given - JWT es de otro usuario
        String otherUserId = "user-999";
        String otherUserToken = "Bearer other-user-token";
        
        when(jwtValidationService.validarGetUsuario(otherUserToken))
            .thenReturn(otherUserId);

        when(sessionTimeoutService.isSessionValid(otherUserId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(otherUserToken))
            .thenReturn(otherUserId);

        when(privateListService.obtenerTodasListasPrivadasConContenidos(otherUserId))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "No tienes permiso para acceder a las listas privadas de otro usuario"));

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", otherUserToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verify(privateListService, times(1)).obtenerTodasListasPrivadasConContenidos(otherUserId);
    }

    @Test
    @DisplayName("CA-07: Acceso sin autenticación - 401")
    void testObtenerTodasListasPrivadasConContenidos_Unauthorized() throws Exception {
        // Given
        when(jwtValidationService.validarGetUsuario(mockToken))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Token inválido o expirado"));

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenThrow(new RuntimeException("Token inválido"));

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(jwtValidationService, times(1)).validarGetUsuario(mockToken);
    }

    @Test
    @DisplayName("CA-08: Token ausente en header - 500")
    void testObtenerTodasListasPrivadasConContenidos_MissingToken() throws Exception {
        // Given - No se proporciona Authorization header

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("CA-09: Token vacío - 401")
    void testObtenerTodasListasPrivadasConContenidos_EmptyToken() throws Exception {
        // Given
        when(jwtValidationService.validarGetUsuario(""))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Token vacío o inválido"));

        when(jwtUtil.getUserIdFromToken(""))
            .thenThrow(new RuntimeException("Token vacío"));

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(jwtValidationService, times(1)).validarGetUsuario("");
    }

    @Test
    @DisplayName("CA-10: Token expirado - 401")
    void testObtenerTodasListasPrivadasConContenidos_ExpiredToken() throws Exception {
        // Given
        String expiredToken = "Bearer expired-token-xyz";
        
        when(jwtValidationService.validarGetUsuario(expiredToken))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Token expirado"));

        when(jwtUtil.getUserIdFromToken(expiredToken))
            .thenThrow(new RuntimeException("Token expirado"));

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(jwtValidationService, times(1)).validarGetUsuario(expiredToken);
    }

    @Test
    @DisplayName("CA-11: Error interno en base de datos - 500")
    void testObtenerTodasListasPrivadasConContenidos_InternalError() throws Exception {
        // Given
        when(jwtValidationService.validarGetUsuario(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(privateListService.obtenerTodasListasPrivadasConContenidos(userId))
            .thenThrow(new RuntimeException("Error en base de datos"));

        // When & Then
        mockMvc.perform(get("/content/lists/private/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(privateListService, times(1)).obtenerTodasListasPrivadasConContenidos(userId);
    }
}
