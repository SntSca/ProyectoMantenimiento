package com.esimedia.features.auth.services;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.entity.ContenidosVideo;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.shared.util.JwtValidationUtil;


/**
 * Servicio para autorizar la subida de contenido por parte de creadores.
 * Verifica que el usuario autenticado sea un creador de contenido válido
 * y del tipo correcto para el contenido que está subiendo.
 */
@Service
public class ContentAuthorizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContentAuthorizationService.class);
    
    private final JwtValidationUtil jwtValidationService;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final ContenidosAudioRepository audioRepository;
    private final ContenidosVideoRepository videoRepository;
    
    public ContentAuthorizationService(JwtValidationUtil jwtValidationService, 
                                     CreadorContenidoRepository creadorContenidoRepository,
                                     ContenidosAudioRepository audioRepository,
                                     ContenidosVideoRepository videoRepository) {
        this.jwtValidationService = jwtValidationService;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.audioRepository = audioRepository;
        this.videoRepository = videoRepository;
    }

    
    /**
     * Verifica que el creador esté validado y no bloqueado
     */
    private void validateCreatorStatus(CreadorContenido creador) throws ResponseStatusException {
        if (!creador.isValidado()) {
            logger.warn("Creador {} no está validado por un administrador", creador.getAliasCreador());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "El creador de contenido debe ser validado por un administrador antes de subir contenido");
        }
        
        if (creador.isBloqueado()) {
            logger.warn("Creador {} está bloqueado", creador.getAliasCreador());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "El creador de contenido está bloqueado y no puede subir contenido");
        }
    }
    
    /**
     * Método auxiliar para validar solo el JWT sin verificar el tipo de contenido
     * Útil para endpoints que no requieren validación de tipo específico
     */
    public CreadorContenido validateCreator(String authHeader) throws ResponseStatusException {
        // Validar que es un creador usando servicio centralizado
        String creadorId = jwtValidationService.validateJwtWithBusinessRules(
            authHeader, 
            Arrays.asList(Rol.CREADOR), 
            null
        );
        CreadorContenido creador = findCreadorById(creadorId);
        validateCreatorStatus(creador);
        return creador;
    }
    
    /**
     * Busca el creador por ID (obtenido del JWT como creadorId)
     */
    private CreadorContenido findCreadorById(String creadorId) throws ResponseStatusException {
        // Buscar por ID del creador
        Optional<CreadorContenido> creadorOpt = creadorContenidoRepository.findById(creadorId);
            
        if (creadorOpt.isEmpty()) {
            logger.error("Creador de contenido no encontrado con ID: {}", creadorId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Creador de contenido no encontrado");
        }
        return creadorOpt.get();
    }

    /**
     * Valida que un usuario pueda acceder a contenido específico
     * @param authHeader Header de autorización con JWT
     * @param contenidoId ID del contenido (audio o video)
     * @param tipoContenido Tipo de contenido (AUDIO o VIDEO)
     * @throws ResponseStatusException si no está autorizado
     */
    public void validateContentAccess(String authHeader, String contenidoId, TipoContenido tipoContenido) {
        // Usar servicio centralizado para validación JWT con reglas de negocio
        String username = jwtValidationService.validateContentAccess(authHeader, tipoContenido);
        logger.info("Acceso validado para usuario {} al contenido {} de tipo {}", 
                   username, contenidoId, tipoContenido);
    }

    /**
     * Obtiene el tipo de contenido basado en el ID (busca en ambas colecciones)
     * @param contenidoId ID del contenido
     * @return TipoContenido (AUDIO o VIDEO)
     * @throws ResponseStatusException si el contenido no existe
     */
    public TipoContenido getTipoContenidoById(String contenidoId) {
        // Primero buscar en audios
        Optional<ContenidosAudio> audio = audioRepository.findById(contenidoId);
        if (audio.isPresent()) {
            return TipoContenido.AUDIO;
        }

        // Luego buscar en videos
        Optional<ContenidosVideo> video = videoRepository.findById(contenidoId);
        if (video.isPresent()) {
            return TipoContenido.VIDEO;
        }

        // No encontrado
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
            "Contenido no encontrado con ID: " + contenidoId);
    }

    /**
     * Valida autorización y obtiene tipo de contenido automáticamente
     * @param authHeader Header de autorización con JWT
     * @param contenidoId ID del contenido
     * @throws ResponseStatusException si no está autorizado o el contenido no existe
     */
    public void validateContentAccessById(String authHeader, String contenidoId) {
        TipoContenido tipoContenido = getTipoContenidoById(contenidoId);
        validateContentAccess(authHeader, contenidoId, tipoContenido);
    }

    /**
     * Valida que el usuario sea un creador para poder eliminar contenido
     * Cualquier creador puede eliminar cualquier contenido
     * @param authHeader Header de autorización con JWT
     * @param contenidoId ID del contenido
     * @throws ResponseStatusException si no está autorizado o el contenido no existe
     */
    public void validateContentDeletion(String authHeader, String contenidoId) {
        // Validar JWT y obtener creadorId (solo creadores)
        String creadorId = jwtValidationService.validateJwtWithBusinessRules(
            authHeader, 
            Arrays.asList(Rol.CREADOR), 
            null
        );
        
        CreadorContenido creador = findCreadorById(creadorId);
        validateCreatorStatus(creador);
        
        // Verificar que el contenido existe
        getTipoContenidoById(contenidoId);
        
        logger.info("Creador {} autorizado para eliminar contenido {}", creadorId, contenidoId);
    }
}