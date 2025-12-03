package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.esimedia.features.content.entity.ContenidoAudioTag;
import com.esimedia.features.content.entity.ContenidoVideoTag;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.entity.ContenidosVideo;
import com.esimedia.features.content.entity.Tags;
import com.esimedia.features.content.repository.ContenidoAudioTagRepository;
import com.esimedia.features.content.repository.ContenidoVideoTagRepository;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.features.content.repository.TagsRepository;
import com.esimedia.features.lists.dto.ContenidoListaResponseDTO;
import com.esimedia.shared.util.ContentProcessingUtil;
import com.esimedia.shared.util.ContentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PublicListHelper {

    private static final Logger logger = LoggerFactory.getLogger(PublicListHelper.class);

    private final ContenidosAudioRepository contenidoAudioRepository;
    private final ContenidosVideoRepository contenidoVideoRepository;
    private final ContenidoAudioTagRepository contenidoAudioTagRepository;
    private final ContenidoVideoTagRepository contenidoVideoTagRepository;
    private final TagsRepository tagsRepository;

    public PublicListHelper(
            ContenidosAudioRepository contenidoAudioRepository,
            ContenidosVideoRepository contenidoVideoRepository,
            ContenidoAudioTagRepository contenidoAudioTagRepository,
            ContenidoVideoTagRepository contenidoVideoTagRepository,
            TagsRepository tagsRepository) {
        this.contenidoAudioRepository = contenidoAudioRepository;
        this.contenidoVideoRepository = contenidoVideoRepository;
        this.contenidoAudioTagRepository = contenidoAudioTagRepository;
        this.contenidoVideoTagRepository = contenidoVideoTagRepository;
        this.tagsRepository = tagsRepository;
    }

    /**
     * Método auxiliar para obtener contenidos completos (audios y videos) con tags
     */
    public List<ContenidoListaResponseDTO> obtenerContenidosCompletos(List<String> idsContenidos) {
        List<ContenidoListaResponseDTO> contenidosCompletos = new ArrayList<>();
        
        for (String idContenido : idsContenidos) {
            logger.info("Buscando contenido con ID: {}", idContenido);
            
            ContenidoListaResponseDTO dto = buscarAudio(idContenido)
                .orElseGet(() -> buscarVideo(idContenido).orElse(null));
            
            if (dto != null) {
                contenidosCompletos.add(dto);
            }
            else {
                logger.debug("No encontrado en audios ni videos para ID: {}", idContenido);
            }
        }
        
        logger.info("Contenidos encontrados: {}", contenidosCompletos.size());
        return contenidosCompletos;
    }

    /**
     * Busca un contenido de audio por ID y lo convierte a DTO
     */
    private Optional<ContenidoListaResponseDTO> buscarAudio(String idContenido) {
        return contenidoAudioRepository.findById(idContenido)
            .map(audio -> {
                logger.info("Encontrado audio con ID: {}", idContenido);
                List<String> tagNames = obtenerTagsNombres(
                    contenidoAudioTagRepository.findByIdContenido(idContenido)
                        .stream()
                        .map(ContenidoAudioTag::getIdTag)
                        .toList()
                );
                
                return construirDTOAudio(audio, tagNames);
            });
    }

    /**
     * Busca un contenido de video por ID y lo convierte a DTO
     */
    private Optional<ContenidoListaResponseDTO> buscarVideo(String idContenido) {
        return contenidoVideoRepository.findById(idContenido)
            .map(video -> {
                logger.debug("Encontrado video con ID: {}", idContenido);
                List<String> tagNames = obtenerTagsNombres(
                    contenidoVideoTagRepository.findByIdContenido(idContenido)
                        .stream()
                        .map(ContenidoVideoTag::getIdTag)
                        .toList()
                );
                
                return construirDTOVideo(video, tagNames);
            });
    }

    /**
     * Obtiene los nombres de los tags a partir de sus IDs
     */
    private List<String> obtenerTagsNombres(List<String> tagIds) {
        return tagsRepository.findByIdTagIn(tagIds)
            .stream()
            .map(Tags::getNombre)
            .toList();
    }

    /**
     * Construye el DTO de respuesta para un audio
     */
    private ContenidoListaResponseDTO construirDTOAudio(ContenidosAudio audio, List<String> tagNames) {
        String miniaturaDataUri = ContentUtil.createDataUri(audio.getMiniatura(), audio.getFormatoMiniatura());
        String ficheroDataUri = ContentUtil.createDataUriFromBinary(audio.getFichero(), audio.getFicheroExtension());
        
        return ContenidoListaResponseDTO.builder()
            .id(audio.getId())
            .titulo(audio.getTitulo())
            .descripcion(audio.getDescripcion())
            .duracion(audio.getDuracion())
            .especialidad(audio.getEspecialidad())
            .visibilidad(audio.isVisibilidad())
            .esVIP(audio.isEsVIP())
            .miniatura(miniaturaDataUri)
            .formatoMiniatura(audio.getFormatoMiniatura())
            .fechaSubida(ContentProcessingUtil.formatDate(audio.getFechaSubida()))
            .fechaExpiracion(ContentProcessingUtil.formatDate(audio.getFechaDisponibleHasta()))
            .valoracionMedia(audio.getValoracionMedia())
            .restriccionEdad(audio.getRestriccionEdad() != null ? audio.getRestriccionEdad().getValor() : null)
            .tags(tagNames)
            .fichero(ficheroDataUri)
            .ficheroExtension(audio.getFicheroExtension())
            .build();
    }

    /**
     * Construye el DTO de respuesta para un video
     */
    private ContenidoListaResponseDTO construirDTOVideo(ContenidosVideo video, List<String> tagNames) {
        String miniaturaDataUri = ContentUtil.createDataUri(video.getMiniatura(), video.getFormatoMiniatura());
        
        return ContenidoListaResponseDTO.builder()
            .id(video.getId())
            .titulo(video.getTitulo())
            .descripcion(video.getDescripcion())
            .duracion(video.getDuracion())
            .especialidad(video.getEspecialidad())
            .visibilidad(video.isVisibilidad())
            .esVIP(video.isEsVIP())
            .miniatura(miniaturaDataUri)
            .formatoMiniatura(video.getFormatoMiniatura())
            .fechaSubida(ContentProcessingUtil.formatDate(video.getFechaSubida()))
            .fechaExpiracion(ContentProcessingUtil.formatDate(video.getFechaDisponibleHasta()))
            .valoracionMedia(video.getValoracionMedia())
            .restriccionEdad(video.getRestriccionEdad() != null ? video.getRestriccionEdad().getValor() : null)
            .tags(tagNames)
            .urlArchivo(video.getUrlArchivo())
            .resolucion(video.getResolucion() != null ? video.getResolucion().toString() : null)
            .build();
    }
}