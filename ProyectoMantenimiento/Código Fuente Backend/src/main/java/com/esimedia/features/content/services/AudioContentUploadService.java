package com.esimedia.features.content.services;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.entity.ContenidoAudioTag;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.repository.ContenidoAudioTagRepository;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.TagsRepository;
import com.esimedia.shared.util.ContentProcessingUtil;
import com.esimedia.shared.util.ContentTagProcessor;
import com.esimedia.shared.util.ContentUtil;
import com.esimedia.shared.security.ClamAVService;
import com.esimedia.features.notifications.services.NotificationService;


import java.util.Date;

@Service
public class AudioContentUploadService {

    private static final Logger logger = LoggerFactory.getLogger(AudioContentUploadService.class);

    private final ValidationService validationService;
    private final ClamAVService clamavService;
    private final ContenidosAudioRepository contenidoAudioRepository;
    private final ContenidoAudioTagRepository contenidoAudioTagRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final ContentTagProcessor tagProcessor;
    private final NotificationService notificationService;


    public AudioContentUploadService(
        ValidationService validationService,
        ClamAVService clamavService,
        ContenidosAudioRepository contenidoAudioRepository,
        ContenidoAudioTagRepository contenidoAudioTagRepository,
        TagsRepository tagsRepository,
        CreadorContenidoRepository creadorContenidoRepository,
        NotificationService notificationService
    ) {
        this.validationService = validationService;
        this.clamavService = clamavService;
        this.contenidoAudioRepository = contenidoAudioRepository;
        this.contenidoAudioTagRepository = contenidoAudioTagRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.notificationService = notificationService;
        this.tagProcessor = new ContentTagProcessor(tagsRepository, ContenidoAudioTag::new);
    }

    public String uploadAudioContent(String username, ContentAudioUploadDTO audioDTO) {
        String validationResult = validationService.validateAudioContent(audioDTO);
        if (validationResult != null) {
            return validationResult;
        }
        return processAudioContent(username, audioDTO);
    }

    private String processAudioContent(String userId, ContentAudioUploadDTO audioDTO) {
        try {
            // Procesar para el fichero de audio (base64 puro)
            byte[] audioBytes = ContentUtil.decodeAudio(audioDTO.getFichero());
            String ficheroExtension = audioDTO.getFicheroExtension();

            // Procesar para la imagen (si existe)
            byte[] imagenBytes = null;
            String imagenExtension = null;
            if (audioDTO.getMiniatura() != null && !audioDTO.getMiniatura().trim().isEmpty()) {
                imagenBytes = ContentUtil.decodeImage(audioDTO.getMiniatura());
                imagenExtension = audioDTO.getFormatoMiniatura();
            }

            // Obtener especialidad del creador
            CreadorContenido creador = creadorContenidoRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("Creador no encontrado"));
            String especialidad = creador.getEspecialidad();

            boolean visible = audioDTO.getVisibilidad() != null ? audioDTO.getVisibilidad() : false;


            // Crear contenido de audio
            ContenidosAudio contenidoAudio = ContenidosAudio.builder()
                .titulo(audioDTO.getTitulo())
                .descripcion(audioDTO.getDescripcion())
                .duracion(audioDTO.getDuracion())
                .idCreador(userId)
                .especialidad(especialidad)
                .fichero(audioBytes)
                .ficheroExtension(ficheroExtension)
                .esVIP(audioDTO.getEsVIP() != null ? audioDTO.getEsVIP() : false)
                .fechaSubida(Date.from(Instant.now()))
                .fechaDisponibleHasta(ContentProcessingUtil.parseFechaExpiracion(audioDTO.getFechaExpiracion()))
                .miniatura(imagenBytes)
                .formatoMiniatura(imagenExtension)
                .restriccionEdad(audioDTO.getRestriccionEdad() != null ?
                    getRestriccionEdadFromValue(audioDTO.getRestriccionEdad()) : null)
                .visibilidad(visible)
                .build();

            // Guardar contenido
            ContenidosAudio savedContent = contenidoAudioRepository.save(contenidoAudio);
            // Crear notificaciones para nuevos contenidos
            notificationService.createNotificationsForNewContent(savedContent);
            // Procesar tags
            tagProcessor.processContentTags(savedContent.getId(), audioDTO.getTags(),
                relation -> contenidoAudioTagRepository.save((ContenidoAudioTag) relation));

            logger.info("Contenido de audio creado exitosamente con ID: {}", savedContent.getId());
            return "SUCCESS:Contenido de audio subido exitosamente";

        } 
        catch (IllegalArgumentException e) {
            logger.error("Error en los datos proporcionados: {}", e.getMessage());
            return "Error en los datos proporcionados: " + e.getMessage();
        } 
        catch (Exception e) {
            logger.error("Error procesando contenido de audio: {}", e.getMessage());
            return "Error interno del servidor";
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
