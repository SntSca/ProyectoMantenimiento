package com.esimedia.features.lists.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.esimedia.features.content.entity.ContenidoAudioTag;
import com.esimedia.features.content.entity.ContenidoVideoTag;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.entity.ContenidosVideo;
import com.esimedia.features.content.entity.Tags;
import com.esimedia.features.content.enums.Resolucion;
import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.content.repository.ContenidoAudioTagRepository;
import com.esimedia.features.content.repository.ContenidoVideoTagRepository;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.features.content.repository.TagsRepository;
import com.esimedia.features.lists.dto.ContenidoListaResponseDTO;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests del Helper de Listas Privadas")
class PrivateListHelperTest {

    @Mock
    private ContenidosAudioRepository contenidoAudioRepository;

    @Mock
    private ContenidosVideoRepository contenidoVideoRepository;

    @Mock
    private ContenidoAudioTagRepository contenidoAudioTagRepository;

    @Mock
    private ContenidoVideoTagRepository contenidoVideoTagRepository;

    @Mock
    private TagsRepository tagsRepository;

    @InjectMocks
    private PrivateListHelper privateListHelper;

    private ContenidosAudio audio;
    private ContenidosVideo video;
    private List<Tags> tags;
    private List<ContenidoAudioTag> audioTags;
    private List<ContenidoVideoTag> videoTags;

    @BeforeEach
    void setUp() {
        // Setup de Tags
        Tags tag1 = new Tags();
        tag1.setIdTag("tag1");
        tag1.setNombre("Rock");

        Tags tag2 = new Tags();
        tag2.setIdTag("tag2");
        tag2.setNombre("Clásico");

        tags = Arrays.asList(tag1, tag2);

        // Setup de Audio
        audio = ContenidosAudio.builder()
            .id("audio-123")
            .titulo("Mi Audio Test")
            .descripcion("Descripción del audio")
            .duracion(180)
            .especialidad("Música")
            .visibilidad(true)
            .esVIP(false)
            .miniatura("miniatura".getBytes())
            .formatoMiniatura("image/jpeg")
            .fechaSubida(new Date())
            .fechaDisponibleHasta(new Date(System.currentTimeMillis() + 86400000))
            .valoracionMedia(4.5)
            .restriccionEdad(RestriccionEdad.ESCOLARES)
            .fichero("audio_data".getBytes())
            .ficheroExtension(".mp3")
            .idCreador("creator-123")
            .build();

        // Setup de Video
        video = ContenidosVideo.builder()
            .id("video-456")
            .titulo("Mi Video Test")
            .descripcion("Descripción del video")
            .duracion(300)
            .especialidad("Documentales")
            .visibilidad(true)
            .esVIP(true)
            .miniatura("miniatura_video".getBytes())
            .formatoMiniatura("image/png")
            .fechaSubida(new Date())
            .fechaDisponibleHasta(new Date(System.currentTimeMillis() + 86400000))
            .valoracionMedia(4.8)
            .restriccionEdad(RestriccionEdad.ADULTOS)
            .urlArchivo("https://example.com/video.mp4")
            .resolucion(Resolucion.HD_720)
            .idCreador("creator-456")
            .build();

        // Setup de AudioTags
        ContenidoAudioTag audioTag1 = new ContenidoAudioTag("audio-123", "tag1");
        ContenidoAudioTag audioTag2 = new ContenidoAudioTag("audio-123", "tag2");

        audioTags = Arrays.asList(audioTag1, audioTag2);

        // Setup de VideoTags
        ContenidoVideoTag videoTag1 = new ContenidoVideoTag();
        videoTag1.setIdContenido("video-456");
        videoTag1.setIdTag("tag1");

        videoTags = Arrays.asList(videoTag1);
    }

    // ==================== Obtener Contenidos Completos ====================

    @Test
    @DisplayName("CA-01: Obtener contenidos completos con audios - Éxito")
    void testObtenerContenidosCompletos_ConAudios_Success() {
        // Given
        List<String> idsContenidos = Arrays.asList("audio-123");

        when(contenidoAudioRepository.findById("audio-123"))
            .thenReturn(Optional.of(audio));
        when(contenidoAudioTagRepository.findByIdContenido("audio-123"))
            .thenReturn(audioTags);
        when(tagsRepository.findByIdTagIn(anyList()))
            .thenReturn(tags);

        // When
        List<ContenidoListaResponseDTO> resultado = privateListHelper.obtenerContenidosCompletos(idsContenidos);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        ContenidoListaResponseDTO dto = resultado.get(0);
        assertEquals("audio-123", dto.getId());
        assertEquals("Mi Audio Test", dto.getTitulo());
        assertEquals("Descripción del audio", dto.getDescripcion());
        assertEquals(180, dto.getDuracion());
        assertEquals("Música", dto.getEspecialidad());
        assertTrue(dto.isVisibilidad());
        assertFalse(dto.isEsVIP());
        assertEquals(4.5, dto.getValoracionMedia());
        assertEquals(12, dto.getRestriccionEdad());
        assertNotNull(dto.getTags());
        assertEquals(2, dto.getTags().size());
        assertNotNull(dto.getFichero());
        assertEquals(".mp3", dto.getFicheroExtension());

        verify(contenidoAudioRepository, times(1)).findById("audio-123");
        verify(contenidoAudioTagRepository, times(1)).findByIdContenido("audio-123");
        verify(tagsRepository, times(1)).findByIdTagIn(anyList());
    }

    @Test
    @DisplayName("CA-02: Obtener contenidos completos con videos - Éxito")
    void testObtenerContenidosCompletos_ConVideos_Success() {
        // Given
        List<String> idsContenidos = Arrays.asList("video-456");

        when(contenidoAudioRepository.findById("video-456"))
            .thenReturn(Optional.empty());
        when(contenidoVideoRepository.findById("video-456"))
            .thenReturn(Optional.of(video));
        when(contenidoVideoTagRepository.findByIdContenido("video-456"))
            .thenReturn(videoTags);
        when(tagsRepository.findByIdTagIn(anyList()))
            .thenReturn(Arrays.asList(tags.get(0)));

        // When
        List<ContenidoListaResponseDTO> resultado = privateListHelper.obtenerContenidosCompletos(idsContenidos);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        ContenidoListaResponseDTO dto = resultado.get(0);
        assertEquals("video-456", dto.getId());
        assertEquals("Mi Video Test", dto.getTitulo());
        assertEquals("Descripción del video", dto.getDescripcion());
        assertEquals(300, dto.getDuracion());
        assertEquals("Documentales", dto.getEspecialidad());
        assertTrue(dto.isVisibilidad());
        assertTrue(dto.isEsVIP());
        assertEquals(4.8, dto.getValoracionMedia());
        assertEquals(18, dto.getRestriccionEdad());
        assertNotNull(dto.getTags());
        assertEquals(1, dto.getTags().size());
        assertEquals("https://example.com/video.mp4", dto.getUrlArchivo());
        assertEquals("720", dto.getResolucion());

        verify(contenidoAudioRepository, times(1)).findById("video-456");
        verify(contenidoVideoRepository, times(1)).findById("video-456");
        verify(contenidoVideoTagRepository, times(1)).findByIdContenido("video-456");
        verify(tagsRepository, times(1)).findByIdTagIn(anyList());
    }

    @Test
    @DisplayName("CA-03: Obtener contenidos completos con audios y videos - Éxito")
    void testObtenerContenidosCompletos_ConAudiosYVideos_Success() {
        // Given
        List<String> idsContenidos = Arrays.asList("audio-123", "video-456");

        // Configurar mock para audio
        when(contenidoAudioRepository.findById("audio-123"))
            .thenReturn(Optional.of(audio));
        when(contenidoAudioTagRepository.findByIdContenido("audio-123"))
            .thenReturn(audioTags);

        // Configurar mock para video
        when(contenidoAudioRepository.findById("video-456"))
            .thenReturn(Optional.empty());
        when(contenidoVideoRepository.findById("video-456"))
            .thenReturn(Optional.of(video));
        when(contenidoVideoTagRepository.findByIdContenido("video-456"))
            .thenReturn(videoTags);

        when(tagsRepository.findByIdTagIn(anyList()))
            .thenReturn(tags);

        // When
        List<ContenidoListaResponseDTO> resultado = privateListHelper.obtenerContenidosCompletos(idsContenidos);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("audio-123", resultado.get(0).getId());
        assertEquals("video-456", resultado.get(1).getId());

        verify(contenidoAudioRepository, times(2)).findById(anyString());
        verify(contenidoVideoRepository, times(1)).findById("video-456");
    }

    @Test
    @DisplayName("CA-04: Obtener contenidos completos - Contenido no encontrado")
    void testObtenerContenidosCompletos_ContenidoNoEncontrado() {
        // Given
        List<String> idsContenidos = Arrays.asList("no-existe-123");

        when(contenidoAudioRepository.findById("no-existe-123"))
            .thenReturn(Optional.empty());
        when(contenidoVideoRepository.findById("no-existe-123"))
            .thenReturn(Optional.empty());

        // When
        List<ContenidoListaResponseDTO> resultado = privateListHelper.obtenerContenidosCompletos(idsContenidos);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(contenidoAudioRepository, times(1)).findById("no-existe-123");
        verify(contenidoVideoRepository, times(1)).findById("no-existe-123");
    }

    @Test
    @DisplayName("CA-05: Obtener contenidos completos con lista vacía")
    void testObtenerContenidosCompletos_ListaVacia() {
        // Given
        List<String> idsContenidos = new ArrayList<>();

        // When
        List<ContenidoListaResponseDTO> resultado = privateListHelper.obtenerContenidosCompletos(idsContenidos);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(contenidoAudioRepository, never()).findById(anyString());
        verify(contenidoVideoRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("CA-06: Obtener contenidos completos - Audio sin tags")
    void testObtenerContenidosCompletos_AudioSinTags() {
        // Given
        List<String> idsContenidos = Arrays.asList("audio-123");

        when(contenidoAudioRepository.findById("audio-123"))
            .thenReturn(Optional.of(audio));
        when(contenidoAudioTagRepository.findByIdContenido("audio-123"))
            .thenReturn(new ArrayList<>());
        when(tagsRepository.findByIdTagIn(anyList()))
            .thenReturn(new ArrayList<>());

        // When
        List<ContenidoListaResponseDTO> resultado = privateListHelper.obtenerContenidosCompletos(idsContenidos);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        ContenidoListaResponseDTO dto = resultado.get(0);
        assertNotNull(dto.getTags());
        assertTrue(dto.getTags().isEmpty());

        verify(contenidoAudioRepository, times(1)).findById("audio-123");
        verify(contenidoAudioTagRepository, times(1)).findByIdContenido("audio-123");
    }

    @Test
    @DisplayName("CA-07: Obtener contenidos completos - Video sin restricción de edad")
    void testObtenerContenidosCompletos_VideoSinRestriccionEdad() {
        // Given
        ContenidosVideo videoSinRestriccion = ContenidosVideo.builder()
            .id("video-456")
            .titulo("Mi Video Test")
            .descripcion("Descripción del video")
            .duracion(300)
            .especialidad("Documentales")
            .visibilidad(true)
            .esVIP(true)
            .miniatura("miniatura_video".getBytes())
            .formatoMiniatura("image/png")
            .fechaSubida(new Date())
            .fechaDisponibleHasta(new Date(System.currentTimeMillis() + 86400000))
            .valoracionMedia(4.8)
            .restriccionEdad(null)
            .urlArchivo("https://example.com/video.mp4")
            .resolucion(Resolucion.HD_720)
            .idCreador("creator-456")
            .build();
        List<String> idsContenidos = Arrays.asList("video-456");

        when(contenidoAudioRepository.findById("video-456"))
            .thenReturn(Optional.empty());
        when(contenidoVideoRepository.findById("video-456"))
            .thenReturn(Optional.of(videoSinRestriccion));
        when(contenidoVideoTagRepository.findByIdContenido("video-456"))
            .thenReturn(videoTags);
        when(tagsRepository.findByIdTagIn(anyList()))
            .thenReturn(Arrays.asList(tags.get(0)));

        // When
        List<ContenidoListaResponseDTO> resultado = privateListHelper.obtenerContenidosCompletos(idsContenidos);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        
        ContenidoListaResponseDTO dto = resultado.get(0);
        assertNull(dto.getRestriccionEdad());

        verify(contenidoVideoRepository, times(1)).findById("video-456");
    }

    @Test
    @DisplayName("CA-08: Constructor verifica dependencias")
    void testConstructor() {
        // Arrange & Act
        PrivateListHelper helper = new PrivateListHelper(
            contenidoAudioRepository,
            contenidoVideoRepository,
            contenidoAudioTagRepository,
            contenidoVideoTagRepository,
            tagsRepository
        );

        // Assert
        assertNotNull(helper);
    }
}
