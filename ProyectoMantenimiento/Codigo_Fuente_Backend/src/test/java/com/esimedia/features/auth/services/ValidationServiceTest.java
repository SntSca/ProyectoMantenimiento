package com.esimedia.features.auth.services;

import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.content.dto.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ValidationServiceTest {

    private CreadorContenidoRepository creadorRepo;
    private UsuarioNormalRepository usuarioRepo;
    private AdminRepository adminRepo;
    private PasswordDictionaryService passwordDictionaryService;

    private ValidationService validationService;

    @BeforeEach
    void setup() {
        creadorRepo = mock(CreadorContenidoRepository.class);
        usuarioRepo = mock(UsuarioNormalRepository.class);
        adminRepo = mock(AdminRepository.class);
        passwordDictionaryService = mock(PasswordDictionaryService.class);

        validationService = new ValidationService(creadorRepo, usuarioRepo, adminRepo, passwordDictionaryService);
    }

    // ====================== PASSWORD ======================

    @Test
    void testValidatePasswordStrength_valid() {
        when(passwordDictionaryService.isPasswordInDictionary("StrongPass!")).thenReturn(false);
        assertDoesNotThrow(() -> validationService.validatePasswordStrength("StrongPass!", "alias", "user@email.com"));
    }

    @Test
    void testValidatePasswordStrength_containsAlias() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validatePasswordStrength("alias123", "alias", "email@test.com"));
        assertTrue(ex.getReason().contains("alias"));
    }

    @Test
    void testValidatePasswordStrength_containsEmail() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validatePasswordStrength("user123pass", "alias", "user@email.com"));
        assertTrue(ex.getReason().contains("correo"));
    }

    @Test
    void testValidatePasswordStrength_inDictionary() {
        when(passwordDictionaryService.isPasswordInDictionary("password")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validatePasswordStrength("password", "alias", "user@email.com"));
        assertTrue(ex.getReason().contains("común"));
    }

    // ====================== FECHA NACIMIENTO ======================

    @Test
    void testValidateFechaNacimiento_valid() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -10);
        assertDoesNotThrow(() -> validationService.validateFechaNacimiento(cal.getTime()));
    }

    @Test
    void testValidateFechaNacimiento_null() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateFechaNacimiento(null));
        assertTrue(ex.getReason().contains("obligatoria"));
    }

    @Test
    void testValidateFechaNacimiento_future() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateFechaNacimiento(cal.getTime()));
        assertTrue(ex.getReason().contains("futura"));
    }

    @Test
    void testValidateFechaNacimiento_tooYoung() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -3);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateFechaNacimiento(cal.getTime()));
        assertTrue(ex.getReason().contains("al menos"));
    }

    @Test
    void testCalculateAge() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        int age = validationService.calculateAge(cal.getTime());
        assertEquals(20, age);
    }

    @Test
    void testCalculateAge_null() {
        assertEquals(0, validationService.calculateAge(null));
    }

    // ====================== USUARIOS ======================

    @Test
    void testValidateGlobalEmailUniqueness_ok() {
        when(usuarioRepo.existsByemail("e@mail.com")).thenReturn(false);
        when(creadorRepo.existsByEmail("e@mail.com")).thenReturn(false);
        when(adminRepo.existsByEmail("e@mail.com")).thenReturn(false);
        assertDoesNotThrow(() -> validationService.validateGlobalEmailUniqueness("e@mail.com"));
    }

    @Test
    void testValidateGlobalEmailUniqueness_conflictUsuario() {
        when(usuarioRepo.existsByemail("e@mail.com")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateGlobalEmailUniqueness("e@mail.com"));
        assertTrue(ex.getReason().contains("usuario"));
    }

    @Test
    void testValidateGlobalEmailUniqueness_conflictCreador() {
        when(usuarioRepo.existsByemail("e@mail.com")).thenReturn(false);
        when(creadorRepo.existsByEmail("e@mail.com")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateGlobalEmailUniqueness("e@mail.com"));
        assertTrue(ex.getReason().contains("creador"));
    }

    @Test
    void testValidateGlobalEmailUniqueness_conflictAdmin() {
        when(usuarioRepo.existsByemail("e@mail.com")).thenReturn(false);
        when(creadorRepo.existsByEmail("e@mail.com")).thenReturn(false);
        when(adminRepo.existsByEmail("e@mail.com")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateGlobalEmailUniqueness("e@mail.com"));
        assertTrue(ex.getReason().contains("administrador"));
    }

    @Test
    void testValidarTipoCreador_ok() {
        assertDoesNotThrow(() -> validationService.validarTipoCreador("audio"));
        assertDoesNotThrow(() -> validationService.validarTipoCreador("video"));
    }

    @Test
    void testValidarTipoCreador_invalid() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validarTipoCreador("otro"));
        assertTrue(ex.getReason().contains("inválido"));
    }

    @Test
    void testValidarTipoCreador_null() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validarTipoCreador(null));
        assertTrue(ex.getReason().contains("Debe especificar"));
    }

    // ====================== CREATOR CONTENT TYPE ======================

    @Test
    void testValidateCreatorContentTypeCompatibility_ok() {
        CreadorContenido creador = new CreadorContenido();
        creador.setTipoContenido(TipoContenido.VIDEO);
        assertDoesNotThrow(() -> validationService.validateCreatorContentTypeCompatibility(creador, TipoContenido.VIDEO));
    }

    @Test
    void testValidateCreatorContentTypeCompatibility_forbidden() {
        CreadorContenido creador = new CreadorContenido();
        creador.setTipoContenido(TipoContenido.AUDIO);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateCreatorContentTypeCompatibility(creador, TipoContenido.VIDEO));
        assertTrue(ex.getReason().contains("no puede subir"));
    }

    @Test
    void testValidateCreatorContentTypeCompatibility_null() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateCreatorContentTypeCompatibility(null, null));
        assertTrue(ex.getReason().contains("requeridos"));
    }

    // ====================== CONTENT UPLOAD ======================

    @Test
    void testValidateContentUpload_basic() {
        ContentUploadDTO dto = new ContentUploadDTO();
        dto.setTags(Collections.singletonList("tag1"));
        dto.setMiniatura("data:image/png;base64,abcd");
        assertNull(validationService.validateContentUpload(dto));
    }

    @Test
    void testValidateContentUpload_tagsNull() {
        ContentUploadDTO dto = new ContentUploadDTO();
        dto.setTags(null);
        dto.setMiniatura("data:image/png;base64,abcd");
        assertEquals("Debe incluir entre 1 y 25 tags", validationService.validateContentUpload(dto));
    }

    @Test
    void testValidateContentUpload_imagenUrl() {
        ContentUploadDTO dto = new ContentUploadDTO();
        dto.setTags(Collections.singletonList("tag1"));
        dto.setMiniatura("http://example.com/image.png");
        assertEquals("La imagen debe ser enviada en formato base64 o data URI, no como URL", validationService.validateContentUpload(dto));
    }

    // ====================== AUDIO ======================

    @Test
    void testEsAudioSoportado() {
        byte[] audio = new byte[]{0};
        // No podemos probar Tika correctamente aquí, solo comprobar que devuelve boolean
        validationService.esAudioSoportado(audio);
    }

    // ====================== DEMÁS ======================

    @Test
    void testValidateFieldLength_exceed() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateFieldLength("aaaaa", "field", 2));
        assertTrue(ex.getReason().contains("no puede tener más"));
    }

    @Test
    void testValidateFieldLength_ok() {
        assertDoesNotThrow(() -> validationService.validateFieldLength("aa", "field", 2));
    }

    // ====================== AUDIO CONTENT ======================
    @Test
    void testValidateAudioContent_valid() throws Exception {
        ContentAudioUploadDTO audioDTO = new ContentAudioUploadDTO();
        audioDTO.setFichero("U29tZUJhc2U2NEVuY29kZWREYXRh");
        audioDTO.setMiniatura("data:image/png;base64,abcd");
        audioDTO.setTags(Collections.singletonList("tag1"));

        // Mock parcial: forzamos que esAudioSoportado devuelva true
        ValidationService spyService = spy(validationService);
        doReturn(true).when(spyService).esAudioSoportado(any());

        assertNull(spyService.validateAudioContent(audioDTO));
    }

    // ====================== VIDEO CONTENT ======================
    @Test
    void testValidateVideoContent_valid() {
        ContentVideoUploadDTO videoDTO = new ContentVideoUploadDTO();
        videoDTO.setResolucion("1080");
        videoDTO.setUrlArchivo("https://www.youtube.com/watch?v=abc");
        videoDTO.setTags(Collections.singletonList("tag1"));
        videoDTO.setMiniatura("data:image/png;base64,abcd");
        assertNull(validationService.validateVideoContent(videoDTO));
    }

    @Test
    void testValidateVideoContent_invalidResolution() {
        ContentVideoUploadDTO videoDTO = new ContentVideoUploadDTO();
        videoDTO.setResolucion("999");
        videoDTO.setUrlArchivo("https://www.youtube.com/watch?v=abc");
        videoDTO.setMiniatura("data:image/png;base64,abcd"); // IMPORTANTE
        videoDTO.setTags(Collections.singletonList("tag1")); // IMPORTANTE
        String error = validationService.validateVideoContent(videoDTO);
        assertNotNull(error);
        assertTrue(error.contains("Resolución inválida"));
    }

    @Test
    void testValidateVideoContent_invalidUrl() {
        ContentVideoUploadDTO videoDTO = new ContentVideoUploadDTO();
        videoDTO.setResolucion("1080");
        videoDTO.setUrlArchivo("https://notallowed.com/video.mp4");
        videoDTO.setMiniatura("data:image/png;base64,abcd"); // IMPORTANTE
        videoDTO.setTags(Collections.singletonList("tag1")); // IMPORTANTE
        String error = validationService.validateVideoContent(videoDTO);
        assertNotNull(error);
        assertTrue(error.contains("dominio soportado"));
    }

    // ====================== ADMIN UNIQUENESS ======================
    @Test
    void testValidateAdminUniqueness_ok() {
        when(usuarioRepo.existsByemail("e@mail.com")).thenReturn(false);
        when(creadorRepo.existsByEmail("e@mail.com")).thenReturn(false);
        when(usuarioRepo.existsByalias("adminAlias")).thenReturn(false);
        when(creadorRepo.existsByAlias("adminAlias")).thenReturn(false);
        assertDoesNotThrow(() -> validationService.validateAdminUniqueness("e@mail.com", "adminAlias"));
    }

    @Test
    void testValidateAdminUniqueness_conflictEmail() {
        when(usuarioRepo.existsByemail("e@mail.com")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateAdminUniqueness("e@mail.com", "alias"));
        assertTrue(ex.getReason().contains("email"));
    }

    @Test
    void testValidateAdminUniqueness_conflictAlias() {
        when(usuarioRepo.existsByalias("alias")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateAdminUniqueness("e@mail.com", "alias"));
        assertTrue(ex.getReason().contains("alias"));
    }

    // ====================== CONTENT UPDATE ======================
    @Test
    void testValidateContentUpdate_valid() {
        ContentUpdateDTO dto = new ContentUpdateDTO();
        dto.setFechaExpiracion("2025-12-31");
        dto.setRestriccionEdad(12);
        assertNull(validationService.validateContentUpdate(dto));
    }

    @Test
    void testValidateContentUpdate_invalidAgeRestriction() {
        ContentUpdateDTO dto = new ContentUpdateDTO();
        dto.setRestriccionEdad(99);
        String error = validationService.validateContentUpdate(dto);
        assertTrue(error.contains("restricción de edad no es válido"));
    }

    // ====================== CONTENT EDIT PERMISSION ======================
    @Test
    void testValidateContentEditPermission_ok() {
        CreadorContenido creador = new CreadorContenido();
        creador.setTipoContenido(TipoContenido.AUDIO);
        assertDoesNotThrow(() -> validationService.validateContentEditPermission(creador, TipoContenido.AUDIO));
    }

    @Test
    void testValidateContentEditPermission_forbidden() {
        CreadorContenido creador = new CreadorContenido();
        creador.setTipoContenido(TipoContenido.AUDIO);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateContentEditPermission(creador, TipoContenido.VIDEO));
        assertTrue(ex.getReason().contains("no puede editar"));
    }

    // ====================== NORMAL USER SPECIFIC ======================
    @Test
    void testValidateUsuarioNormalData_ok() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -20);
        assertDoesNotThrow(() -> validationService.validateUsuarioNormalData("email@test.com", "alias", "Pass123!", cal.getTime()));
    }

    @Test
    void testValidateNormalUserSpecific_conflict() {
        when(usuarioRepo.existsByemail("email@test.com")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateNormalUserSpecific("email@test.com"));
        assertTrue(ex.getReason().contains("registrado"));
    }

    @Test
    void testValidateCreatorSpecific_valid() {
        // Preparación
        String email = "nuevo@creador.com";
        String alias = "aliasCreador";

        when(usuarioRepo.existsByemail(email)).thenReturn(false);
        when(creadorRepo.existsByEmail(email)).thenReturn(false);
        when(adminRepo.existsByEmail(email)).thenReturn(false);
        when(creadorRepo.existsByAliasCreador(alias)).thenReturn(false);

        // Ejecución y verificación: no debe lanzar excepción
        assertDoesNotThrow(() -> validationService.validateCreatorSpecific(email, alias));
    }

    @Test
    void testValidateCreatorSpecific_aliasNullOrEmpty() {
        String email = "nuevo@creador.com";

        // Alias nulo
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> validationService.validateCreatorSpecific(email, null));
        assertTrue(ex1.getReason().contains("alias del creador es obligatorio"));

        // Alias vacío
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> validationService.validateCreatorSpecific(email, "   "));
        assertTrue(ex2.getReason().contains("alias del creador es obligatorio"));
    }

    @Test
    void testValidateCreatorSpecific_aliasExists() {
        String email = "nuevo@creador.com";
        String alias = "aliasExistente";

        when(usuarioRepo.existsByemail(email)).thenReturn(false);
        when(creadorRepo.existsByEmail(email)).thenReturn(false);
        when(adminRepo.existsByEmail(email)).thenReturn(false);
        when(creadorRepo.existsByAliasCreador(alias)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateCreatorSpecific(email, alias));
        assertTrue(ex.getReason().contains("Ya existe un creador con ese alias"));
    }

    @Test
    void testValidateCreatorSpecific_emailConflict() {
        String email = "conflict@creador.com";
        String alias = "nuevoAlias";

        // Simulamos que el email ya existe en usuario normal
        when(usuarioRepo.existsByemail(email)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validationService.validateCreatorSpecific(email, alias));
        assertTrue(ex.getReason().contains("usuario"));
    }

    @Test
    void testValidarTipoCreador_unexpectedException() {
        try (MockedStatic<TipoContenido> mocked = mockStatic(TipoContenido.class)) {

            mocked.when(() -> TipoContenido.valueOf(anyString()))
                    .thenThrow(new RuntimeException("Error inesperado"));

            ResponseStatusException ex =
                    assertThrows(ResponseStatusException.class,
                            () -> validationService.validarTipoCreador("audio"));

            assertTrue(ex.getReason().contains("Error inesperado en validación"));
        }
    }
}
