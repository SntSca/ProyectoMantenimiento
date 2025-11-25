package com.esimedia.features.content.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.dto.ContentUpdateDTO;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.shared.util.JwtValidationUtil;

import java.util.List;

@Service
public class AudioContentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AudioContentService.class);
    private static final String ERROR_INTERNO_SERVIDOR = "Error interno del servidor";

    private final AudioContentUploadService uploadService;
    private final AudioContentRetrievalService retrievalService;
    private final AudioContentManagementService managementService;
    private final JwtValidationUtil jwtValidationService;

    public AudioContentService(
        AudioContentUploadService uploadService,
        AudioContentRetrievalService retrievalService,
        AudioContentManagementService managementService,
        JwtValidationUtil jwtValidationService
    ) {
        this.uploadService = uploadService;
        this.retrievalService = retrievalService;
        this.managementService = managementService;
        this.jwtValidationService = jwtValidationService;
    }

    public String uploadAudioContent(String authHeader, ContentAudioUploadDTO audioDTO) {
        try {
            // Validar autorización usando servicio centralizado
            String username = jwtValidationService.validateContentUpload(authHeader, TipoContenido.AUDIO);

            return uploadService.uploadAudioContent(username, audioDTO);
        } 
        catch (Exception e) {
            logger.error("Error procesando contenido de audio: {}", e.getMessage());
            return ERROR_INTERNO_SERVIDOR;
        }
    }

    // La funcionalidad de decodificar miniaturas y extraer MIME se delega a ContentUtil.processImageContent(...)

    public List<ContentAudioUploadDTO> getAllAudiosAsDTO(String authHeader) {
        return retrievalService.getAllAudiosAsDTO(authHeader);
    }
    
    public ContenidosAudio getAudioById(String id) {
        return retrievalService.getAudioById(id);
    }

    /**
     * Obtiene un audio por ID y lo devuelve como DTO con data URIs completos.
     * @param authHeader Header de autorización con JWT
     * @param id ID del audio
     * @return DTO con campos de contenido como data URIs
     */
    public ContentAudioUploadDTO getAudioByIdAsDTO(String authHeader, String id) {
        return retrievalService.getAudioByIdAsDTO(authHeader, id);
    }

    public String updateAudioContent(String authHeader, String contentId, ContentUpdateDTO updateDTO) {
        return managementService.updateAudioContent(authHeader, contentId, updateDTO);
    }

    public void incrementarVisualizaciones(String contentId) {
        managementService.incrementarVisualizaciones(contentId);
    }

    /**
     * Elimina un contenido de audio
     * @param authHeader Header de autorización con JWT
     * @param contentId ID del contenido de audio a eliminar
     */
    public void deleteAudioContent(String authHeader, String contentId) {
        managementService.deleteAudioContent(authHeader, contentId);
    }
}