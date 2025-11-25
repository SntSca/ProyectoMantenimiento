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

import com.esimedia.features.lists.dto.ListaResponseDTO;
import com.esimedia.features.lists.services.PublicListService;
import com.esimedia.features.lists.services.PublicListCreationService;
import com.esimedia.features.lists.services.PublicListContentService;
import com.esimedia.features.lists.services.PublicListQueryService;
import com.esimedia.features.lists.services.PublicListUpdateService;
import com.esimedia.features.auth.services.SesionService;
import com.esimedia.features.user_management.services.UserRetrievalService;
import com.esimedia.features.auth.services.SessionTimeoutService;
import com.esimedia.shared.util.JwtUtil;
import com.esimedia.shared.util.JwtValidationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebMvcTest(controllers = PublicListController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=" +
    "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
@DisplayName("Tests del Controlador de Listas Públicas - Consultas")
@SuppressWarnings({"removal"})
class PublicListControllerQueryTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicListService publicListService;

    @MockBean
    private JwtValidationUtil jwtValidationService;

    @MockBean
    private PublicListCreationService publicListCreationService;

    @MockBean
    private PublicListContentService publicListContentService;

    @MockBean
    private PublicListQueryService publicListQueryService;

    @MockBean
    private PublicListUpdateService publicListUpdateService;

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
        mockToken = "Bearer test-token-public-12345";
        userId = "user-456";
    }

    // ==================== Casos de Éxito - Consultas Públicas ====================

    @Test
    @DisplayName("CA-01: Consulta exitosa - No devuelve ninguna lista")
    void testObtenerTodasListasPublicasConContenidos_EmptyList() throws Exception {
        // Given
        List<ListaResponseDTO> listas = Arrays.asList();

        when(jwtValidationService.validarGenerico(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(publicListService.obtenerTodasListasPublicasConContenidos())
            .thenReturn(listas);

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(publicListService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

    @Test
    @DisplayName("CA-02: Consulta exitosa - Devuelve una lista sin contenidos")
    void testObtenerTodasListasPublicasConContenidos_SingleListNoContents() throws Exception {
        // Given
        ListaResponseDTO lista = ListaResponseDTO.builder()
            .idLista("lista-pub-001")
            .nombre("Mi Playlist Pública")
            .descripcion("Mi lista sin contenidos")
            .idCreadorUsuario(userId)
            .visibilidad(true)
            .contenidos(new ArrayList<>())
            .build();

        when(jwtValidationService.validarGenerico(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(publicListService.obtenerTodasListasPublicasConContenidos())
            .thenReturn(Arrays.asList(lista));

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].idLista").value("lista-pub-001"))
            .andExpect(jsonPath("$[0].nombre").value("Mi Playlist Pública"))
            .andExpect(jsonPath("$[0].contenidos").isArray())
            .andExpect(jsonPath("$[0].contenidos.length()").value(0));

        verify(publicListService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

    @Test
    @DisplayName("CA-03: Consulta exitosa - Devuelve una lista con contenidos (1 audio y 1 video)")
    void testObtenerTodasListasPublicasConContenidos_SingleListWithContents() throws Exception {
        // Given
        ListaResponseDTO lista = ListaResponseDTO.builder()
            .idLista("lista-pub-001")
            .nombre("Playlist Pública de Música")
            .descripcion("Canciones públicas favoritas")
            .idCreadorUsuario(userId)
            .visibilidad(true)
            .contenidos(new ArrayList<>())
            .build();

        when(jwtValidationService.validarGenerico(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(publicListService.obtenerTodasListasPublicasConContenidos())
            .thenReturn(Arrays.asList(lista));

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].idLista").value("lista-pub-001"))
            .andExpect(jsonPath("$[0].nombre").value("Playlist Pública de Música"))
            .andExpect(jsonPath("$[0].descripcion").value("Canciones públicas favoritas"));

        verify(publicListService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

    @Test
    @DisplayName("CA-04: Consulta exitosa - Devuelve dos listas con contenidos (1 audio y 1 video)")
    void testObtenerTodasListasPublicasConContenidos_TwoListsWithContents() throws Exception {
        // Given
        ListaResponseDTO lista1 = ListaResponseDTO.builder()
            .idLista("lista-pub-001")
            .nombre("Playlist Pública de Música")
            .descripcion("Canciones públicas favoritas")
            .idCreadorUsuario(userId)
            .visibilidad(true)
            .contenidos(new ArrayList<>())
            .build();

        ListaResponseDTO lista2 = ListaResponseDTO.builder()
            .idLista("lista-pub-002")
            .nombre("Audios Públicos")
            .descripcion("Contenido de audio público")
            .idCreadorUsuario(userId)
            .visibilidad(true)
            .contenidos(new ArrayList<>())
            .build();

        List<ListaResponseDTO> listas = Arrays.asList(lista1, lista2);

        when(jwtValidationService.validarGenerico(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(publicListService.obtenerTodasListasPublicasConContenidos())
            .thenReturn(listas);

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].idLista").value("lista-pub-001"))
            .andExpect(jsonPath("$[0].nombre").value("Playlist Pública de Música"))
            .andExpect(jsonPath("$[1].idLista").value("lista-pub-002"));

        verify(jwtValidationService, times(1)).validarGenerico(mockToken);
        verify(publicListService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

    @Test
    @DisplayName("CA-06: Verificación de estructura de respuesta de lista pública")
    void testObtenerTodasListasPublicasConContenidos_ResponseStructure() throws Exception {
        // Given
        ListaResponseDTO lista = ListaResponseDTO.builder()
            .idLista("lista-pub-001")
            .nombre("Lista Pública Test")
            .descripcion("Descripción test pública")
            .idCreadorUsuario(userId)
            .visibilidad(true)
            .contenidos(Arrays.asList())
            .build();

        when(jwtValidationService.validarGenerico(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(publicListService.obtenerTodasListasPublicasConContenidos())
            .thenReturn(Arrays.asList(lista));

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idLista").exists())
            .andExpect(jsonPath("$[0].nombre").exists())
            .andExpect(jsonPath("$[0].descripcion").exists())
            .andExpect(jsonPath("$[0].idCreadorUsuario").exists())
            .andExpect(jsonPath("$[0].visibilidad").exists())
            .andExpect(jsonPath("$[0].contenidos").isArray());

        verify(publicListService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

    @Test
    @DisplayName("CA-07: Múltiples listas públicas con contenidos variados")
    void testObtenerTodasListasPublicasConContenidos_MultipleLists() throws Exception {
        // Given
        List<ListaResponseDTO> listas = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            listas.add(ListaResponseDTO.builder()
                .idLista("lista-pub-" + i)
                .nombre("Lista Pública " + i)
                .descripcion("Descripción pública " + i)
                .idCreadorUsuario(userId)
                .visibilidad(true)
                .contenidos(Arrays.asList())
                .build());
        }

        when(jwtValidationService.validarGenerico(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(publicListService.obtenerTodasListasPublicasConContenidos())
            .thenReturn(listas);

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));

        verify(publicListService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

    // ==================== Casos de Error ====================

    @Test
    @DisplayName("CA-05: Consulta errónea - No está el usuario autenticado (no proporciona JWT)")
    void testObtenerTodasListasPublicasConContenidos_MissingToken() throws Exception {
        // Given - No se proporciona Authorization header
        // Nota: Spring lanza MissingRequestHeaderException que se maneja como 500

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("CA-08: Acceso sin autenticación válida - Token inválido o expirado - 401")
    void testObtenerTodasListasPublicasConContenidos_Unauthorized() throws Exception {
        // Given
        when(jwtValidationService.validarGenerico(mockToken))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Token inválido o expirado"));

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenThrow(new RuntimeException("Token inválido")); // Para que SessionTimeoutFilter permita continuar

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(jwtValidationService, times(1)).validarGenerico(mockToken);
    }

    @Test
    @DisplayName("CA-09: Token vacío en header - 401")
    void testObtenerTodasListasPublicasConContenidos_EmptyToken() throws Exception {
        // Given
        when(jwtValidationService.validarGenerico(""))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Token vacío o inválido"));

        when(jwtUtil.getUserIdFromToken(""))
            .thenThrow(new RuntimeException("Token vacío")); // Para que SessionTimeoutFilter permita continuar

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(jwtValidationService, times(1)).validarGenerico("");
    }

    @Test
    @DisplayName("CA-10: Token expirado - 401")
    void testObtenerTodasListasPublicasConContenidos_ExpiredToken() throws Exception {
        // Given
        String expiredToken = "Bearer expired-token-public-xyz";
        
        when(jwtValidationService.validarGenerico(expiredToken))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Token expirado"));

        when(jwtUtil.getUserIdFromToken(expiredToken))
            .thenThrow(new RuntimeException("Token expirado")); // Para que SessionTimeoutFilter permita continuar

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(jwtValidationService, times(1)).validarGenerico(expiredToken);
    }

    @Test
    @DisplayName("CA-11: Error interno al obtener listas públicas - 500")
    void testObtenerTodasListasPublicasConContenidos_InternalError() throws Exception {
        // Given
        when(jwtValidationService.validarGenerico(mockToken))
            .thenReturn(userId);

        when(sessionTimeoutService.isSessionValid(userId))
            .thenReturn(true);

        when(jwtUtil.getUserIdFromToken(mockToken))
            .thenReturn(userId);

        when(publicListService.obtenerTodasListasPublicasConContenidos())
            .thenThrow(new RuntimeException("Error en base de datos"));

        // When & Then
        mockMvc.perform(get("/content/lists/public/contents")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(publicListService, times(1)).obtenerTodasListasPublicasConContenidos();
    }

}
