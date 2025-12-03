package com.esimedia.features.content.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.services.ContentAuthorizationService;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.dto.ContentUpdateDTO;
import com.esimedia.features.content.entity.ContenidoAudioTag;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.entity.ValoracionContenido;
import com.esimedia.features.content.repository.ContenidoAudioTagRepository;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.TagsRepository;
import com.esimedia.features.content.repository.ValoracionContenidoRepository;
import com.esimedia.shared.util.ContentProcessingUtil;
import com.esimedia.shared.util.ContentTagProcessor;
import com.esimedia.shared.util.JwtValidationUtil;

import java.util.List;

@Service
public class AudioContentManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AudioContentManagementService.class);
    private static final String ERROR_INTERNO_SERVIDOR = "Error interno del servidor";
    private static final String AUDIO_NOT_FOUND = "Contenido de audio no encontrado";

    private final JwtValidationUtil jwtValidationService;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final ValidationService validationService;
    private final ContenidosAudioRepository contenidoAudioRepository;
    private final ContenidoAudioTagRepository contenidoAudioTagRepository;
    private final ValoracionContenidoRepository valoracionRepository;
    private final ContentTagProcessor tagProcessor;
    private final ContentAuthorizationService contentAuthorizationService;

    public AudioContentManagementService(
        JwtValidationUtil jwtValidationService,
        CreadorContenidoRepository creadorContenidoRepository,
        ValidationService validationService,
        ContenidosAudioRepository contenidoAudioRepository,
        ContenidoAudioTagRepository contenidoAudioTagRepository,
        TagsRepository tagsRepository,
        ValoracionContenidoRepository valoracionRepository,
        ContentAuthorizationService contentAuthorizationService
    ) {
        this.jwtValidationService = jwtValidationService;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.validationService = validationService;
        this.contenidoAudioRepository = contenidoAudioRepository;
        this.contenidoAudioTagRepository = contenidoAudioTagRepository;
        this.valoracionRepository = valoracionRepository;
        this.contentAuthorizationService = contentAuthorizationService;
        this.tagProcessor = new ContentTagProcessor(tagsRepository, ContenidoAudioTag::new);
    }

    public String updateAudioContent(String authHeader, String contentId, ContentUpdateDTO updateDTO) {
        try {
            String username = jwtValidationService.validateContentUpload(authHeader, TipoContenido.AUDIO);

            CreadorContenido creador = creadorContenidoRepository.findById(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Creador no encontrado"));

            validationService.validateContentEditPermission(creador, TipoContenido.AUDIO);

            ContenidosAudio existingContent = contenidoAudioRepository.findById(contentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, AUDIO_NOT_FOUND));

            String validationResult = validationService.validateContentUpdate(updateDTO);
            if (validationResult != null) {
                return validationResult;
            }

            updateContentFields(existingContent, updateDTO);

            tagProcessor.updateContentTags(
                contentId,
                updateDTO.getTags(),
                () -> contenidoAudioTagRepository.findByIdContenido(contentId)
                         .stream().map(ContenidoAudioTag::getIdTag).toList(),
                tagId -> contenidoAudioTagRepository.deleteByIdContenidoAndIdTag(contentId, tagId),
                relation -> contenidoAudioTagRepository.save((ContenidoAudioTag) relation)
            );

            contenidoAudioRepository.save(existingContent);

            logger.info("Contenido de audio {} actualizado exitosamente por {}", contentId, username);
            return "SUCCESS:Contenido de audio actualizado exitosamente";

        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error("Error actualizando contenido de audio {}: {}", contentId, e.getMessage());
            return ERROR_INTERNO_SERVIDOR;
        }
    }

    public void deleteAudioContent(String authHeader, String contentId) {
        try {
            contentAuthorizationService.validateContentDeletion(authHeader, contentId);

            contenidoAudioRepository.findById(contentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, AUDIO_NOT_FOUND));

            contenidoAudioTagRepository.deleteByIdContenido(contentId);

            List<ValoracionContenido> valoraciones = valoracionRepository.findByIdContenido(contentId);
            valoracionRepository.deleteAll(valoraciones);

            contenidoAudioRepository.deleteById(contentId);

            logger.info("Contenido de audio {} eliminado exitosamente", contentId);
        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error("Error eliminando contenido de audio {}: {}", contentId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO_SERVIDOR);
        }
    }

    public void incrementarVisualizaciones(String contentId) {
        try {
            ContenidosAudio content = contenidoAudioRepository.findById(contentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, AUDIO_NOT_FOUND));
            content.setVisualizaciones(content.getVisualizaciones() + 1);
            contenidoAudioRepository.save(content);
            logger.info("Visualizaciones incrementadas para audio {}", contentId);
        } 
        catch (Exception e) {
            logger.error("Error incrementando visualizaciones para audio {}: {}", contentId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO_SERVIDOR);
        }
    }

    private void updateContentFields(ContenidosAudio content, ContentUpdateDTO updateDTO) {
        if (updateDTO.getTitulo() != null) {
            content.setTitulo(updateDTO.getTitulo());
        }
        if (updateDTO.getDescripcion() != null) {
            content.setDescripcion(updateDTO.getDescripcion());
        }
        if (updateDTO.getEsVIP() != null) {
            content.setEsVIP(updateDTO.getEsVIP());
        }
        if (updateDTO.getVisibilidad() != null) {
            content.setVisibilidad(updateDTO.getVisibilidad());
        }
        if (updateDTO.getFechaExpiracion() != null) {
            content.setFechaDisponibleHasta(ContentProcessingUtil.parseFechaExpiracion(updateDTO.getFechaExpiracion()));
        }
        if (updateDTO.getRestriccionEdad() != null) {
            content.setRestriccionEdad(getRestriccionEdadFromValue(updateDTO.getRestriccionEdad()));
        }
    }

    private RestriccionEdad getRestriccionEdadFromValue(Integer valor) {
        if (valor == null) return null;

        for (RestriccionEdad restriccion : RestriccionEdad.values()) {
            if (restriccion.getValor() == valor) {
                return restriccion;
            }
        }
        throw new IllegalArgumentException("Valor de restricción de edad no válido: " + valor);
    }
}