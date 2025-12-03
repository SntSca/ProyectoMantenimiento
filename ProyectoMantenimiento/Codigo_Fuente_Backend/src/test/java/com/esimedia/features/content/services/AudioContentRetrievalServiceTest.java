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

import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.entity.*;
import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.content.repository.*;
import com.esimedia.shared.util.JwtValidationUtil;

@ExtendWith(MockitoExtension.class)
public class AudioContentRetrievalServiceTest {

    @Mock private JwtValidationUtil jwtValidationService;
    @Mock private UsuarioNormalRepository usuarioNormalRepository;
    @Mock private ValidationService validationService;
    @Mock private ContenidosAudioRepository contenidoAudioRepository;
    @Mock private ContenidoAudioTagRepository contenidoAudioTagRepository;
    @Mock private TagsRepository tagsRepository;
    @Mock private ValoracionContenidoRepository valoracionRepository;

    @InjectMocks
    private AudioContentRetrievalService audioContentRetrievalService;

    private static final String AUTH_HEADER = "Bearer token";
    private static final String USER_ID = "user123";
    private static final String AUDIO_ID = "audio456";

    private ContenidosAudio audio;

    @BeforeEach
    void setUp() {
        audio = ContenidosAudio.builder()
            .id(AUDIO_ID)
            .titulo("Test Audio")
            .descripcion("Description")
            .duracion(180)
            .idCreador(USER_ID)
            .especialidad("Music")
            .fichero(new byte[]{1, 2, 3})
            .ficheroExtension("mp3")
            .esVIP(false)
            .visualizaciones(0)
            .fechaSubida(new Date())
            .visibilidad(true)
            .build();
    }

    // ========== getAllAudiosAsDTO - Rol branches ==========

    @Test
    void testGetAllAudiosAsDTO_RolNormal_WithAge() {
        UsuarioNormal user = new UsuarioNormal();
        user.setFechaNacimiento(new Date(System.currentTimeMillis() - 630720000000L)); // 20 años
        
        audio.setRestriccionEdad(RestriccionEdad.ADULTOS);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(validationService.calculateAge(any())).thenReturn(20);
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAllAudiosAsDTO_RolNormal_BelowAgeRestriction() {
        UsuarioNormal user = new UsuarioNormal();
        user.setFechaNacimiento(new Date(System.currentTimeMillis() - 315360000000L)); // 10 años
        
        audio.setRestriccionEdad(RestriccionEdad.ADULTOS);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(validationService.calculateAge(any())).thenReturn(10);
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllAudiosAsDTO_RolNormal_NotVisible() {
        audio.setVisibilidad(false);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(new UsuarioNormal()));
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllAudiosAsDTO_RolCreador() {
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.CREADOR);
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAllAudiosAsDTO_ExpiredAudio() {
        audio.setFechaDisponibleHasta(new Date(System.currentTimeMillis() - 86400000)); // Ayer
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.ADMINISTRADOR);
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioRepository.save(any())).thenReturn(audio);
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        verify(contenidoAudioRepository).save(any());
    }

    @Test
    void testGetAllAudiosAsDTO_WithTags() {
        ContenidoAudioTag tag = new ContenidoAudioTag();
        tag.setIdTag("tag1");
        Tags tagEntity = new Tags();
        tagEntity.setNombre("TestTag");
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.ADMINISTRADOR);
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Arrays.asList(tag));
        when(tagsRepository.findByIdTagIn(any())).thenReturn(Arrays.asList(tagEntity));

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
        assertFalse(result.get(0).getTags().isEmpty());
    }

    @Test
    void testGetAllAudiosAsDTO_UserNotFound() {
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.empty());
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAllAudiosAsDTO_UserNoFechaNacimiento() {
        UsuarioNormal user = new UsuarioNormal();
        user.setFechaNacimiento(null);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
    }

    @Test
    void testGetAllAudiosAsDTO_NoRestriccionEdad() {
        UsuarioNormal user = new UsuarioNormal();
        user.setFechaNacimiento(new Date());
        
        audio.setRestriccionEdad(null);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.NORMAL);
        when(usuarioNormalRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(validationService.calculateAge(any())).thenReturn(15);
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertFalse(result.isEmpty());
    }

    // ========== getAudioById ==========

    @Test
    void testGetAudioById_Found() {
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));

        ContenidosAudio result = audioContentRetrievalService.getAudioById(AUDIO_ID);

        assertNotNull(result);
    }

    @Test
    void testGetAudioById_NotFound() {
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.empty());

        ContenidosAudio result = audioContentRetrievalService.getAudioById(AUDIO_ID);

        assertNull(result);
    }

    // ========== getAudioByIdAsDTO ==========

    @Test
    void testGetAudioByIdAsDTO_Success() {
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.of(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        ContentAudioUploadDTO result = audioContentRetrievalService.getAudioByIdAsDTO(AUTH_HEADER, AUDIO_ID);

        assertNotNull(result);
    }

    @Test
    void testGetAudioByIdAsDTO_NotFound() {
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(contenidoAudioRepository.findById(AUDIO_ID)).thenReturn(Optional.empty());

        ContentAudioUploadDTO result = audioContentRetrievalService.getAudioByIdAsDTO(AUTH_HEADER, AUDIO_ID);

        assertNull(result);
    }

    // ========== mapAudioToDTO - Cubre branches de fechas ==========

    @Test
    void testMapAudioToDTO_FechaSubidaNull() {
        audio.setFechaSubida(null);
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.ADMINISTRADOR);
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertNull(result.get(0).getFechaSubida());
    }

    @Test
    void testMapAudioToDTO_WithFechaExpiracion() {
        audio.setFechaDisponibleHasta(new Date(System.currentTimeMillis() + 86400000));
        
        when(jwtValidationService.validateContentAccess(AUTH_HEADER, TipoContenido.AUDIO)).thenReturn(USER_ID);
        when(jwtValidationService.getRolFromToken(AUTH_HEADER)).thenReturn(Rol.ADMINISTRADOR);
        when(contenidoAudioRepository.findAll()).thenReturn(Arrays.asList(audio));
        when(contenidoAudioTagRepository.findByIdContenido(any())).thenReturn(Collections.emptyList());

        List<ContentAudioUploadDTO> result = audioContentRetrievalService.getAllAudiosAsDTO(AUTH_HEADER);

        assertNotNull(result.get(0).getFechaExpiracion());
    }
}