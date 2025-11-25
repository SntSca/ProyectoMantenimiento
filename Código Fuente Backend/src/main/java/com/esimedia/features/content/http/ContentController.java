package com.esimedia.features.content.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.dto.ContentUpdateDTO;
import com.esimedia.features.content.dto.ContentVideoUploadDTO;
import com.esimedia.features.content.services.AudioContentService;
import com.esimedia.features.content.services.ValoracionService;
import com.esimedia.features.content.services.VideoContentService;
import com.esimedia.shared.util.JwtValidationUtil;

import jakarta.validation.Valid;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/content")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", exposedHeaders = "Authorization")
public class ContentController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);
    private static final String ERROR_INTERNO = "Error interno del servidor";
    private static final String UNAUTHORIZED_MESSAGE = "Usuario no autorizado para ver audios.";
    
    private final AudioContentService audioContentService;
    private final VideoContentService videoContentService;
    private final ValoracionService valoracionService;
    private final JwtValidationUtil jwtValidationService;

    public ContentController(AudioContentService audioContentService, VideoContentService videoContentService, ValoracionService valoracionService, JwtValidationUtil jwtValidationService) {
        this.audioContentService = audioContentService;
        this.videoContentService = videoContentService;
        this.valoracionService = valoracionService;
        this.jwtValidationService = jwtValidationService;
    }

    @PostMapping("/upload-audio")
    public ResponseEntity<String> uploadAudioContent(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ContentAudioUploadDTO audioDTO) {
        try {
            logger.info("Se ha registrado una petición de audio con estos datos: {}", audioDTO);
            String result = audioContentService.uploadAudioContent(authHeader, audioDTO);
            return processServiceResult(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error de validación o autorización en audio: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } 
        catch (Exception e) {
            logger.error("Error procesando contenido de audio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INTERNO);
        }
    }

    @PostMapping("/upload-video")
    public ResponseEntity<String> uploadVideoContent(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody ContentVideoUploadDTO videoDTO) {
        try {
            logger.info("Se ha registrado una petición de video con estos datos: {}", videoDTO);
            String result = videoContentService.uploadVideoContent(authHeader, videoDTO);
            return processServiceResult(result);
        } 
        catch (ResponseStatusException e) {
            logger.warn("Error de validación o autorización en video: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error procesando contenido de video: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO);
        }
    }


    @GetMapping("/getAudio/{id}")
    public ResponseEntity<ContentAudioUploadDTO> getAudio(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        try {
            ContentAudioUploadDTO audioDTO = audioContentService.getAudioByIdAsDTO(authHeader, id);
            if (audioDTO == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(audioDTO);
        }
        catch (Exception e) {
            logger.error("Error obteniendo audio por ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado nada.");
        }
    }

    @GetMapping("/getVideo/{id}")
    public ResponseEntity<ContentVideoUploadDTO> getVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        try {
            ContentVideoUploadDTO videoDTO = videoContentService.getVideoByIdAsDTO(authHeader, id);
            if (videoDTO == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(videoDTO);
        }
        catch (Exception e) {
            logger.error("Error obteniendo video por ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado nada.");
        }
    }

    @GetMapping("/getAllContent")
    public ResponseEntity<List<Object>> getAllContent(
            @RequestHeader("Authorization") String authHeader) {
        try {
            List<Object> allContent = new ArrayList<>();
            
            // Agregar todos los audios (solo si el usuario tiene permisos)
            List<ContentAudioUploadDTO> audios = audioContentService.getAllAudiosAsDTO(authHeader);
            allContent.addAll(audios);
            List<ContentVideoUploadDTO> videos = videoContentService.getAllVideosAsDTO(authHeader);
            allContent.addAll(videos);
            return ResponseEntity.ok(allContent);
        }
        catch (ResponseStatusException e) {
        // Usuario no autorizado para ver audios, continuar sin agregar
        logger.info("Usuario no autorizado para ver audios: {}", e.getReason());
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MESSAGE);
        }
        catch (Exception e) {
            logger.error("Error obteniendo todo el contenido: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO);
        }
    }

    @GetMapping("/getAllAudios")
    public ResponseEntity<List<ContentAudioUploadDTO>> getAllAudios(
            @RequestHeader("Authorization") String authHeader) {
        try {
            List<ContentAudioUploadDTO> audios = audioContentService.getAllAudiosAsDTO(authHeader);
            return ResponseEntity.ok(audios);
        }
        catch (Exception e) {
            logger.error("Error obteniendo todos los audios: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO);
        }
    }

    @GetMapping("/getAllVideos")
    public ResponseEntity<List<ContentVideoUploadDTO>> getAllVideos(
            @RequestHeader("Authorization") String authHeader) {
        try {
            List<ContentVideoUploadDTO> videos = videoContentService.getAllVideosAsDTO(authHeader);
            return ResponseEntity.ok(videos);
        }
        catch (Exception e) {
            logger.error("Error obteniendo todos los videos: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO);
        }
    }

    @PutMapping("/update-audio/{id}")
    public ResponseEntity<String> updateAudioContent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @Valid @RequestBody ContentUpdateDTO updateDTO) {
        try {
            logger.info("Se ha registrado una petición de actualización de audio con ID: {}", id);
            String result = audioContentService.updateAudioContent(authHeader, id, updateDTO);
            return processServiceResult(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error de validación o autorización en actualización de audio: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } 
        catch (Exception e) {
            logger.error("Error procesando actualización de audio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INTERNO);
        }
    }

    @PutMapping("/update-video/{id}")
    public ResponseEntity<String> updateVideoContent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @Valid @RequestBody ContentUpdateDTO updateDTO) {
        try {
            logger.info("Se ha registrado una petición de actualización de video con ID: {}", id);
            String result = videoContentService.updateVideoContent(authHeader, id, updateDTO);
            return processServiceResult(result);
        }
        catch (ResponseStatusException e) {
            logger.warn("Error de validación o autorización en actualización de video: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } 
        catch (Exception e) {
            logger.error("Error procesando actualización de video: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INTERNO);
        }
    }

    @PostMapping("/rate/{idContenido}")
    public ResponseEntity<String> rateContent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String idContenido,
            @RequestBody int valoracion) {
        try {
            // Usar cualquiera, ya que es genérico
            String username = jwtValidationService.validateContentAccess(authHeader, TipoContenido.AUDIO); 
            valoracionService.valorarContenido(idContenido, username, valoracion);
            return ResponseEntity.ok("Valoración registrada exitosamente");
        }
        catch (ResponseStatusException e) {
            logger.warn("Error en valoración: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        catch (Exception e) {
            logger.error("Error procesando valoración: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INTERNO);
        }
    }

    /**
     * Método común para procesar el resultado del servicio
     */
    private ResponseEntity<String> processServiceResult(String result) {
        if (result.startsWith("SUCCESS:")) {
            String successMessage = result.substring(8);
            return ResponseEntity.ok(successMessage);
        } 
        else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/increment-views-audio/{id}")
    public ResponseEntity<String> incrementAudioViews(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        try {
            jwtValidationService.validateContentAccess(authHeader, TipoContenido.AUDIO);
            audioContentService.incrementarVisualizaciones(id);
            return ResponseEntity.ok("Visualizaciones incrementadas para audio");
        } 
        catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } 
        catch (Exception e) {
            logger.error("Error incrementando visualizaciones para audio {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INTERNO);
        }
    }

    @PostMapping("/increment-views-video/{id}")
    public ResponseEntity<String> incrementVideoViews(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        try {
            jwtValidationService.validateContentAccess(authHeader, TipoContenido.VIDEO);
            videoContentService.incrementarVisualizaciones(id);
            return ResponseEntity.ok("Visualizaciones incrementadas para video");
        } 
        catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } 
        catch (Exception e) {
            logger.error("Error incrementando visualizaciones para video {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INTERNO);
        }
    }

    @DeleteMapping("/delete-audio/{id}")
    public ResponseEntity<String> deleteAudioContent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        try {
            logger.info("Se ha registrado una petición de eliminación de audio con ID: {}", id);
            audioContentService.deleteAudioContent(authHeader, id);
            return ResponseEntity.ok("Contenido de audio eliminado exitosamente");
        } 
        catch (ResponseStatusException e) {
            logger.warn("Error de validación o autorización en eliminación de audio: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } 
        catch (Exception e) {
            logger.error("Error eliminando contenido de audio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INTERNO);
        }
    }

    @DeleteMapping("/delete-video/{id}")
    public ResponseEntity<String> deleteVideoContent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {
        try {
            logger.info("Se ha registrado una petición de eliminación de video con ID: {}", id);
            videoContentService.deleteVideoContent(authHeader, id);
            return ResponseEntity.ok("Contenido de video eliminado exitosamente");
        } 
        catch (ResponseStatusException e) {
            logger.warn("Error de validación o autorización en eliminación de video: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } 
        catch (Exception e) {
            logger.error("Error eliminando contenido de video: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INTERNO);
        }
    }
}
