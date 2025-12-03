package com.esimedia.features.content.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.services.ContentAuthorizationService;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.dto.ContentUpdateDTO;
import com.esimedia.features.content.dto.ContentVideoUploadDTO;
import com.esimedia.features.content.entity.*;
import com.esimedia.features.content.enums.Resolucion;
import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.content.repository.*;
import com.esimedia.shared.util.JwtValidationUtil;

@ExtendWith(MockitoExtension.class)
public class VideoContentServiceTest {

    @Mock private ValidationService validationService;
    @Mock private JwtValidationUtil jwtValidationService;
    @Mock private ContentAuthorizationService contentAuthorizationService;
    @Mock private ContenidosVideoRepository contenidoVideoRepository;
    @Mock private ContenidoVideoTagRepository contenidoVideoTagRepository;
    @Mock private TagsRepository tagsRepository;
    @Mock private CreadorContenidoRepository creadorContenidoRepository;
    @Mock private ValoracionContenidoRepository valoracionRepository;
    @Mock private UsuarioNormalRepository usuarioNormalRepository;

    @InjectMocks
    private VideoContentService videoContentService;

    private static final String AUTH_HEADER = "Bearer test.jwt.token";
    private static final String USER_ID = "user123";
    private static final String CONTENT_ID = "content456";

    private ContentVideoUploadDTO videoDTO;
    private ContenidosVideo video;
    private CreadorContenido creador;

    @BeforeEach
    void setUp() {
        videoDTO = new ContentVideoUploadDTO();
        videoDTO.setTitulo("Test Video");
        videoDTO.setDescripcion("Test Description");
        videoDTO.setDuracion(120);
        videoDTO.setUrlArchivo("https://example.com/video.mp4");
        videoDTO.setResolucion("1080");
        videoDTO.setEsVIP(false);
        videoDTO.setTags(Arrays.asList("tag1", "tag2"));

        creador = new CreadorContenido();
        creador.setIdUsuario(USER_ID);
        creador.setEspecialidad("Video Production");

        video = ContenidosVideo.builder()
            .id(CONTENT_ID)
            .titulo("Test")
            .descripcion("Desc")
            .duracion(100)
            .idCreador(USER_ID)
            .especialidad("Spec")
            .urlArchivo("https://test.com/vid.mp4")
            .resolucion(Resolucion.FHD_1080)
            .esVIP(false)
            .visualizaciones(0)
            .fechaSubida(new Date())
            .visibilidad(true)
            .build();
    }

    // ========== uploadVideoContent - Cubre 2 branches ==========

    @Test
    void testUploadVideoContent_ValidationError() {
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(validationService.validateVideoContent(videoDTO)).thenReturn("Error de validación");

        String result = videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        assertEquals("Error de validación", result);
    }

    @Test
    void testUploadVideoContent_Exception() {
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO))
            .thenThrow(new RuntimeException("Error"));

        String result = videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        assertEquals("Error interno del servidor", result);
    }

    // ========== validateVideoProcessingInputs - Cubre branches ==========

    @Test
    void testValidateVideoProcessingInputs_InvalidUrl() {
        videoDTO.setUrlArchivo("invalid-url");
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(validationService.validateVideoContent(videoDTO)).thenReturn(null);

        String result = videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        assertTrue(result.contains("URL del video no es válida"));
    }

    @Test
    void testValidateVideoProcessingInputs_InvalidResolution() {
        videoDTO.setResolucion("invalid");
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(validationService.validateVideoContent(videoDTO)).thenReturn(null);

        String result = videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        assertTrue(result.contains("Resolución inválida"));
    }

    // ========== isValidUrl - Cubre 3 branches ==========

    @Test
    void testIsValidUrl_Null() {
        videoDTO.setUrlArchivo(null);
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(validationService.validateVideoContent(videoDTO)).thenReturn(null);

        String result = videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        assertTrue(result.contains("URL del video no es válida"));
    }

    @Test
    void testIsValidUrl_Empty() {
        videoDTO.setUrlArchivo("   ");
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(validationService.validateVideoContent(videoDTO)).thenReturn(null);

        String result = videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        assertTrue(result.contains("URL del video no es válida"));
    }

    // ========== mapResolutionString - Cubre 4 cases ==========

    @Test
    void testMapResolutionString_720() {
        videoDTO.setResolucion("720");
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(validationService.validateVideoContent(videoDTO)).thenReturn(null);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoVideoRepository.save(any())).thenReturn(video);

        videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        verify(contenidoVideoRepository).save(any());
    }

    // ========== createAndSaveVideoContent - Miniatura branches ==========

    @Test
    void testCreateAndSaveVideoContent_WithMiniatura() {
        videoDTO.setMiniatura("base64data");
        videoDTO.setFormatoMiniatura("image/png");
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(validationService.validateVideoContent(videoDTO)).thenReturn(null);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoVideoRepository.save(any())).thenReturn(video);

        videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        verify(contenidoVideoRepository).save(any());
    }

    @Test
    void testCreateAndSaveVideoContent_WithRestriccionEdad() {
        videoDTO.setRestriccionEdad(18);
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(validationService.validateVideoContent(videoDTO)).thenReturn(null);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoVideoRepository.save(any())).thenReturn(video);

        videoContentService.uploadVideoContent(AUTH_HEADER, videoDTO);

        verify(contenidoVideoRepository).save(any());
    }

    // ========== getVideoById - Cubre 2 branches ==========

    @Test
    void testGetVideoById_Found() {
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));

        ContenidosVideo result = videoContentService.getVideoById(CONTENT_ID);

        assertNotNull(result);
    }

    @Test
    void testGetVideoById_NotFound() {
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());

        ContenidosVideo result = videoContentService.getVideoById(CONTENT_ID);

        assertNull(result);
    }

    @Test
    void testGetVideoById_NumberFormatException() {
        when(contenidoVideoRepository.findById(anyString())).thenThrow(new NumberFormatException());

        ContenidosVideo result = videoContentService.getVideoById("invalid");

        assertNull(result);
    }

    // ========== getAllVideosAsDTO - Cubre branches de roles y filtros ==========

    @Test
    void testGetAllVideosAsDTO_RolNormal_WithAge() {
        UsuarioNormal user = new UsuarioNormal();
        user.setFechaNacimiento(new Date(System.currentTimeMillis() - 630720000000L)); // 20 años
        
        video.setRestriccionEdad(RestriccionEdad.ADULTOS);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(validationService.calculateAge(any())).thenReturn(20);
        when(contenidoVideoRepository.findAll()).thenReturn(Arrays.asList(video));
        when(contenidoVideoTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentVideoUploadDTO> result = videoContentService.getAllVideosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAllVideosAsDTO_RolNormal_BelowAgeRestriction() {
        UsuarioNormal user = new UsuarioNormal();
        user.setFechaNacimiento(new Date(System.currentTimeMillis() - 315360000000L)); // 10 años
        
        video.setRestriccionEdad(RestriccionEdad.ADULTOS);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(validationService.calculateAge(any())).thenReturn(10);
        when(contenidoVideoRepository.findAll()).thenReturn(Arrays.asList(video));

        List<ContentVideoUploadDTO> result = videoContentService.getAllVideosAsDTO(AUTH_HEADER);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllVideosAsDTO_RolNormal_NotVisible() {
        video.setVisibilidad(false);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(new UsuarioNormal()));
        when(contenidoVideoRepository.findAll()).thenReturn(Arrays.asList(video));

        List<ContentVideoUploadDTO> result = videoContentService.getAllVideosAsDTO(AUTH_HEADER);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllVideosAsDTO_RolCreador() {
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.CREADOR);
        when(contenidoVideoRepository.findAll()).thenReturn(Arrays.asList(video));
        when(contenidoVideoTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentVideoUploadDTO> result = videoContentService.getAllVideosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAllVideosAsDTO_ExpiredVideo() {
        video.setFechaDisponibleHasta(new Date(System.currentTimeMillis() - 86400000)); // Ayer
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.ADMINISTRADOR);
        when(contenidoVideoRepository.findAll()).thenReturn(Arrays.asList(video));
        when(contenidoVideoRepository.save(any())).thenReturn(video);
        when(contenidoVideoTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        videoContentService.getAllVideosAsDTO(AUTH_HEADER);

        verify(contenidoVideoRepository).save(any());
    }

    @Test
    void testGetAllVideosAsDTO_WithTags() {
        ContenidoVideoTag tag = new ContenidoVideoTag();
        tag.setIdTag("tag1");
        Tags tagEntity = new Tags();
        tagEntity.setNombre("TestTag");
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.ADMINISTRADOR);
        when(contenidoVideoRepository.findAll()).thenReturn(Arrays.asList(video));
        when(contenidoVideoTagRepository.findByIdContenido(any())).thenReturn(Arrays.asList(tag));
        when(tagsRepository.findByIdTagIn(any())).thenReturn(Arrays.asList(tagEntity));

        List<ContentVideoUploadDTO> result = videoContentService.getAllVideosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
        assertFalse(result.get(0).getTags().isEmpty());
    }

    // ========== getVideoByIdAsDTO - Cubre método ==========

    @Test
    void testGetVideoByIdAsDTO_Success() {
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(contenidoVideoTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        ContentVideoUploadDTO result = videoContentService.getVideoByIdAsDTO(AUTH_HEADER, CONTENT_ID);

        assertNotNull(result);
    }

    @Test
    void testGetVideoByIdAsDTO_NotFound() {
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());

        ContentVideoUploadDTO result = videoContentService.getVideoByIdAsDTO(AUTH_HEADER, CONTENT_ID);

        assertNull(result);
    }

    // ========== updateVideoContent - Cubre branches de actualización ==========

    @Test
    void testUpdateVideoContent_Success() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        updateDTO.setTitulo("New Title");
        updateDTO.setDescripcion("New Desc");
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(validationService.validateContentUpdate(updateDTO)).thenReturn(null);
        when(contenidoVideoRepository.save(any())).thenReturn(video);

        String result = videoContentService.updateVideoContent(AUTH_HEADER, CONTENT_ID, updateDTO);

        assertTrue(result.contains("SUCCESS"));
    }

    @Test
    void testUpdateVideoContent_ValidationError() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(validationService.validateContentUpdate(updateDTO)).thenReturn("Error");

        String result = videoContentService.updateVideoContent(AUTH_HEADER, CONTENT_ID, updateDTO);

        assertEquals("Error", result);
    }

    @Test
    void testUpdateVideoContent_EsVIPFalseWithUHD() {
        video.setResolucion(Resolucion.UHD_2160);
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        updateDTO.setEsVIP(false);
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(validationService.validateContentUpdate(updateDTO)).thenReturn(null);
        when(contenidoVideoRepository.save(any())).thenReturn(video);

        videoContentService.updateVideoContent(AUTH_HEADER, CONTENT_ID, updateDTO);

        verify(contenidoVideoRepository).save(argThat(v -> v.getResolucion() == Resolucion.FHD_1080));
    }

    @Test
    void testUpdateVideoContent_CreadorNotFound() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            videoContentService.updateVideoContent(AUTH_HEADER, CONTENT_ID, updateDTO));
    }

    @Test
    void testUpdateVideoContent_ContentNotFound() {
        ContentUpdateDTO updateDTO = new ContentUpdateDTO();
        
        when(jwtValidationService.validateContentUpload(AUTH_HEADER, TipoContenido.VIDEO)).thenReturn(USER_ID);
        when(creadorContenidoRepository.findById(USER_ID)).thenReturn(Optional.of(creador));
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            videoContentService.updateVideoContent(AUTH_HEADER, CONTENT_ID, updateDTO));
    }

    // ========== incrementarVisualizaciones ==========

    @Test
    void testIncrementarVisualizaciones_Success() {
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(contenidoVideoRepository.save(any())).thenReturn(video);

        videoContentService.incrementarVisualizaciones(CONTENT_ID);

        verify(contenidoVideoRepository).save(argThat(v -> v.getVisualizaciones() == 1));
    }

    @Test
    void testIncrementarVisualizaciones_NotFound() {
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            videoContentService.incrementarVisualizaciones(CONTENT_ID));
    }

    // ========== deleteVideoContent ==========

    @Test
    void testDeleteVideoContent_Success() {
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Collections.emptyList());

        videoContentService.deleteVideoContent(AUTH_HEADER, CONTENT_ID);

        verify(contenidoVideoTagRepository).deleteByIdContenido(CONTENT_ID);
        verify(contenidoVideoRepository).deleteById(CONTENT_ID);
    }

    @Test
    void testDeleteVideoContent_NotFound() {
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
            videoContentService.deleteVideoContent(AUTH_HEADER, CONTENT_ID));
    }

    @Test
    void testDeleteVideoContent_WithValoraciones() {
        ValoracionContenido val = new ValoracionContenido();
        
        when(contenidoVideoRepository.findById(CONTENT_ID)).thenReturn(Optional.of(video));
        when(valoracionRepository.findByIdContenido(CONTENT_ID)).thenReturn(Arrays.asList(val));

        videoContentService.deleteVideoContent(AUTH_HEADER, CONTENT_ID);

        verify(valoracionRepository).deleteAll(any());
    }
}