package com.esimedia.features.content.services;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.content.enums.Resolucion;
import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.shared.util.ContentUtil;
import com.esimedia.shared.util.JwtValidationUtil;
import com.esimedia.shared.util.ContentProcessingUtil;
import com.esimedia.shared.util.ContentTagProcessor;
import com.esimedia.features.auth.services.ContentAuthorizationService;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.dto.ContentUpdateDTO;
import com.esimedia.features.content.dto.ContentVideoUploadDTO;
import com.esimedia.features.content.entity.ContenidoVideoTag;
import com.esimedia.features.content.entity.ContenidosVideo;
import com.esimedia.features.content.entity.Tags;
import com.esimedia.features.content.entity.ValoracionContenido;
import com.esimedia.features.content.repository.ContenidoVideoTagRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.features.content.repository.TagsRepository;
import com.esimedia.features.content.repository.ValoracionContenidoRepository;
import com.esimedia.features.notifications.services.NotificationService;


import java.util.List;
import java.util.Date;

@Service
public class VideoContentService {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoContentService.class);
    
    private static final String CONTENIDO_VIDEO_NO_ENCONTRADO = "Contenido de video no encontrado";
    private static final String ERROR_INTERNO_SERVIDOR = "Error interno del servidor";
    
    private final ValidationService validationService;
    private final JwtValidationUtil jwtValidationService;
    private final ContentAuthorizationService contentAuthorizationService;
    private final ContenidosVideoRepository contenidoVideoRepository;
    private final ContenidoVideoTagRepository contenidoVideoTagRepository;
    private final TagsRepository tagsRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final ValoracionContenidoRepository valoracionRepository;
    private final UsuarioNormalRepository usuarioNormalRepository;
    private final ContentTagProcessor tagProcessor;
    private final NotificationService notificationService;


    public VideoContentService(
        ValidationService validationService,
        ContenidosVideoRepository contenidoVideoRepository,
        ContenidoVideoTagRepository contenidoVideoTagRepository,
        TagsRepository tagRepository,
        JwtValidationUtil jwtValidationService,
        ContentAuthorizationService contentAuthorizationService,
        CreadorContenidoRepository creadorContenidoRepository,
        ValoracionContenidoRepository valoracionRepository,
        UsuarioNormalRepository usuarioNormalRepository,
        NotificationService notificationService
    ) {
        this.validationService = validationService;
        this.contenidoVideoRepository = contenidoVideoRepository;
        this.contenidoVideoTagRepository = contenidoVideoTagRepository;
        this.tagsRepository = tagRepository;
        this.jwtValidationService = jwtValidationService;
        this.contentAuthorizationService = contentAuthorizationService;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.valoracionRepository = valoracionRepository;
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.notificationService = notificationService;
        this.tagProcessor = new ContentTagProcessor(tagRepository, (contenidoId, tagId) -> {
            ContenidoVideoTag videoTag = new ContenidoVideoTag();
            videoTag.setIdContenido(contenidoId);
            videoTag.setIdTag(tagId);
            return videoTag;
        });
    }

    public String uploadVideoContent(String authHeader, ContentVideoUploadDTO videoDTO) {
        try {
            // Validar autorización usando servicio centralizado
            String username = jwtValidationService.validateContentUpload(authHeader, TipoContenido.VIDEO);
            
            String validationResult = validationService.validateVideoContent(videoDTO);
            if (validationResult != null) {
                return validationResult; 
            }
            return processVideoContent(username, videoDTO);
        } 
        catch (Exception e) {
            logger.error("Error procesando contenido de video: {}", e.getMessage());
            return ERROR_INTERNO_SERVIDOR;
        }
    }    private String processVideoContent(String userId, ContentVideoUploadDTO videoDTO) {
        try {
            String validationError = validateVideoProcessingInputs(videoDTO);
            if (validationError != null) {
                return validationError;
            }
            
            return createAndSaveVideoContent(userId, videoDTO);
            
        } 
        catch (Exception e) {
            logger.error("Error procesando contenido de video: {}", e.getMessage());
            return ERROR_INTERNO_SERVIDOR;
        }
    }
    
    private String validateVideoProcessingInputs(ContentVideoUploadDTO videoDTO) {
        String videoUrl = videoDTO.getUrlArchivo();
        if (!isValidUrl(videoUrl)) {
            return "La URL del video no es válida";
        }
        
        Resolucion resolucion = mapResolutionString(videoDTO.getResolucion());
        if (resolucion == null) {
            return "Resolución inválida. Use: 720, 1080 o 4k";
        }
        
        return null;
    }
    
    private String createAndSaveVideoContent(String userId, ContentVideoUploadDTO videoDTO) {
        String videoUrl = videoDTO.getUrlArchivo();
        Resolucion resolucion = mapResolutionString(videoDTO.getResolucion());
        
        // Procesar miniatura (base64 puro) y formato (MIME tipo directo)
        byte[] imagenArchivo = null;
        String formatoImagen = null;
        
        if (videoDTO.getMiniatura() != null && !videoDTO.getMiniatura().trim().isEmpty()) {
            imagenArchivo = ContentUtil.decodeImage(videoDTO.getMiniatura());
            formatoImagen = videoDTO.getFormatoMiniatura();
        }
        
        boolean visible = videoDTO.getVisibilidad() != null ? videoDTO.getVisibilidad() : false;

        // Obtener especialidad del creador
        CreadorContenido creador = creadorContenidoRepository.findById(userId).orElseThrow(() ->
            new RuntimeException("Creador no encontrado"));
        String especialidad = creador.getEspecialidad();

        // Crear contenido de video usando la factoría y builder
        ContenidosVideo contenidoVideo = ContenidosVideo.builder()
            .titulo(videoDTO.getTitulo())
            .descripcion(videoDTO.getDescripcion())
            .duracion(videoDTO.getDuracion())
            .idCreador(userId)
            .especialidad(especialidad)
            .urlArchivo(videoUrl)
            .resolucion(resolucion)
            .esVIP(Boolean.TRUE.equals(videoDTO.getEsVIP()))
            .fechaSubida(Date.from(Instant.now()))
            .fechaDisponibleHasta(ContentProcessingUtil.parseFechaExpiracion(videoDTO.getFechaExpiracion()))
            .miniatura(imagenArchivo)
            .formatoMiniatura(formatoImagen)
            .restriccionEdad(videoDTO.getRestriccionEdad() != null ? 
                getRestriccionEdadFromValue(videoDTO.getRestriccionEdad()) : null)
            .visibilidad(visible)
            .build();
        
        // Validar reglas de negocio
        contenidoVideo.validar();
        
        // Guardar contenido
        ContenidosVideo savedContent = contenidoVideoRepository.save(contenidoVideo);
        
        // Procesar tags
        tagProcessor.processContentTags(savedContent.getId(), videoDTO.getTags(), 
            relation -> contenidoVideoTagRepository.save((ContenidoVideoTag) relation));
        // Crear notificaciones para nuevos contenidos
        notificationService.createNotificationsForNewContent(savedContent);

        logger.info("Contenido de video creado exitosamente con ID: {}", savedContent.getId());
        return "SUCCESS:Contenido de video subido exitosamente";
    }
    
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://");
    }
    
    /**
     * Mapea la cadena de resolución al enum correspondiente
     */
    private Resolucion mapResolutionString(String resolucionStr) {
        Resolucion result = null;
        switch (resolucionStr) {
            case "720":
                result = Resolucion.HD_720;
                break;
            case "1080":
                result = Resolucion.FHD_1080;
                break;
            case "4K":
                result = Resolucion.UHD_2160;
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Convierte un valor entero a RestriccionEdad
     */
    private RestriccionEdad getRestriccionEdadFromValue(Integer valor) {
        if (valor == null) return null;
        
        for (RestriccionEdad restriccion : RestriccionEdad.values()) {
            if (restriccion.getValor() == valor) {
                return restriccion;
            }
        }
        throw new IllegalArgumentException("Valor de restricción de edad no válido: " + valor);
    }

    public ContenidosVideo getVideoById(String id) {
        try {
            return contenidoVideoRepository.findById(id).orElse(null);
        } 
        catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Obtiene todos los videos como DTOs con data URIs completos.
     * Solo creadores de VIDEO, usuarios NORMAL y ADMINISTRADORES pueden acceder.
     * @param authHeader Header de autorización con JWT
     * @return Lista de DTOs de video
     */
    public List<ContentVideoUploadDTO> getAllVideosAsDTO(String authHeader) {
        // Validar que el usuario puede acceder a contenido de video usando servicio centralizado
        String username = jwtValidationService.validateContentAccess(authHeader, TipoContenido.VIDEO);
        Rol userRole = jwtValidationService.getRolFromToken(authHeader);
        logger.debug("Usuario {} con rol {} autorizado para ver lista de videos", username, userRole);
        
        // Obtener edad del usuario si es NORMAL
        Integer userAge = getUserAge(username, userRole);
        logger.debug("Usuario NORMAL encontrado: {}, edad calculada: {}", userRole == Rol.NORMAL, userAge);
        
        List<ContenidosVideo> videos = contenidoVideoRepository.findAll();
        // Actualizar visibilidad si expirado
        updateExpiredVideos(videos);
        return videos.stream()
            .filter(video -> isVideoAccessible(video, userRole, userAge))
            .map(video -> mapVideoToDTO(video, username))
            .toList();
    }

    private Integer getUserAge(String username, Rol userRole) {
        if (userRole != Rol.NORMAL) return null;
        UsuarioNormal user = usuarioNormalRepository.findById(username).orElse(null);
        return (user != null && user.getFechaNacimiento() != null) ? validationService.calculateAge(user.getFechaNacimiento()) : null;
    }

    private void updateExpiredVideos(List<ContenidosVideo> videos) {
        videos.forEach(video -> {
            if (video.getFechaDisponibleHasta() != null && video.getFechaDisponibleHasta().before(new Date())) {
                video.setVisibilidad(false);
                contenidoVideoRepository.save(video);
            }
        });
    }

    private boolean isVideoAccessible(ContenidosVideo video, Rol userRole, Integer userAge) {
        if (userRole != Rol.NORMAL) return true;
        if (!video.isVisibilidad()) return false;
        if (video.getRestriccionEdad() != null && userAge != null && userAge < video.getRestriccionEdad().getValor()) return false;
        return true;
    }

    private ContentVideoUploadDTO mapVideoToDTO(ContenidosVideo video, String username) {
        ContentVideoUploadDTO dto = new ContentVideoUploadDTO();
        dto.setId(video.getId());
        dto.setTitulo(video.getTitulo());
        dto.setDescripcion(video.getDescripcion());
        dto.setRestriccionEdad(video.getRestriccionEdad() != null ? video.getRestriccionEdad().getValor() : null);
        dto.setDuracion(video.getDuracion());
        dto.setUrlArchivo(video.getUrlArchivo());
        dto.setResolucion(video.getResolucion().toString());
        dto.setEsVIP(video.isEsVIP());
        dto.setEspecialidad(video.getEspecialidad());
        dto.setValoracionMedia(video.getValoracionMedia());
        dto.setVisualizaciones(video.getVisualizaciones());

        // Obtener valoración del usuario
        ValoracionContenido userRating = valoracionRepository.findByIdContenidoAndIdUsuario(video.getId(), username);
        dto.setValoracionUsuario(userRating != null ? userRating.getValoracion() : null);

        // Formatear fechas en formato yyyy-MM-dd (consistente con fecha de expiración)
        if (video.getFechaSubida() != null) {
            dto.setFechaSubida(ContentProcessingUtil.formatDate(video.getFechaSubida()));
        } 
        else {
            logger.warn("Video {} tiene fechaSubida null", video.getId());
            dto.setFechaSubida(null);
        }
        
        if (video.getFechaDisponibleHasta() != null) {
            dto.setFechaExpiracion(ContentProcessingUtil.formatDate(video.getFechaDisponibleHasta()));
        }

        // Obtener tags del contenido
        List<ContenidoVideoTag> videoTags = contenidoVideoTagRepository.findByIdContenido(video.getId());
        List<String> tagIds = videoTags.stream()
            .map(ContenidoVideoTag::getIdTag)
            .toList();
        
        List<Tags> tags = tagsRepository.findByIdTagIn(tagIds);
        List<String> tagNames = tags.stream()
            .map(Tags::getNombre)
            .toList();
        dto.setTags(tagNames);

        // Convertir miniatura binaria a data URI
        String miniaturaDataUri = ContentUtil.createDataUri(video.getMiniatura(), video.getFormatoMiniatura());
        dto.setMiniatura(miniaturaDataUri);
        dto.setFormatoMiniatura(video.getFormatoMiniatura());
        
        dto.setVisibilidad(video.isVisibilidad());

        return dto;
    }

    /**
     * Obtiene un video por ID y lo devuelve como DTO con data URIs completos.
     * @param authHeader Header de autorización con JWT
     * @param id ID del video
     * @return DTO con campos de contenido como data URIs
     */
    public ContentVideoUploadDTO getVideoByIdAsDTO(String authHeader, String id) {
        // Validar autorización para acceder a contenido de video
        String username = jwtValidationService.validateContentAccess(authHeader, TipoContenido.VIDEO);
        ContenidosVideo video = getVideoById(id);
        if (video == null) {
            return null;
        }

        ContentVideoUploadDTO dto = new ContentVideoUploadDTO();
        dto.setId(video.getId());
        dto.setTitulo(video.getTitulo());
        dto.setDescripcion(video.getDescripcion());
        dto.setRestriccionEdad(video.getRestriccionEdad() != null ? video.getRestriccionEdad().getValor() : null);
        dto.setDuracion(video.getDuracion());
        dto.setUrlArchivo(video.getUrlArchivo());
        dto.setResolucion(video.getResolucion().toString());
        dto.setEsVIP(video.isEsVIP());
        dto.setEspecialidad(video.getEspecialidad());
        dto.setValoracionMedia(video.getValoracionMedia());
        dto.setVisualizaciones(video.getVisualizaciones());

        // Obtener valoración del usuario
        ValoracionContenido userRating = valoracionRepository.findByIdContenidoAndIdUsuario(id, username);
        dto.setValoracionUsuario(userRating != null ? userRating.getValoracion() : null);

        // Formatear fechas en formato yyyy-MM-dd (consistente con fecha de expiración)
        dto.setFechaSubida(ContentProcessingUtil.formatDate(video.getFechaSubida()));

        if (video.getFechaDisponibleHasta() != null) {
            dto.setFechaExpiracion(ContentProcessingUtil.formatDate(video.getFechaDisponibleHasta()));
        }

        // Obtener tags del contenido
        List<ContenidoVideoTag> videoTags = contenidoVideoTagRepository.findByIdContenido(id);

        // Extraer los IDs de las tags
        List<String> tagIds = videoTags.stream()
            .map(ContenidoVideoTag::getIdTag)
            .toList();
        
        // Buscar las tags por sus IDs y extraer los nombres
        List<Tags> tags = tagsRepository.findByIdTagIn(tagIds);
        List<String> tagNames = tags.stream()
            .map(Tags::getNombre)
            .toList();
        dto.setTags(tagNames);

        // Convertir miniatura binaria a data URI
        String miniaturaDataUri = ContentUtil.createDataUri(video.getMiniatura(), video.getFormatoMiniatura());
        dto.setMiniatura(miniaturaDataUri);
        dto.setFormatoMiniatura(video.getFormatoMiniatura());
        
        return dto;
    }

    /**
     * Actualiza un contenido de video existente.
     * Solo creadores de VIDEO pueden editar contenido de video.
     * @param authHeader Header de autorización con JWT
     * @param contentId ID del contenido a actualizar
     * @param updateDTO DTO con los campos a actualizar
     * @return Mensaje de resultado
     */
    public String updateVideoContent(String authHeader, String contentId, ContentUpdateDTO updateDTO) {
        try {
            // Validar autorización para subida (solo creadores de VIDEO)
            String username = jwtValidationService.validateContentUpload(authHeader, TipoContenido.VIDEO);

            // Obtener el creador
            CreadorContenido creador = creadorContenidoRepository.findById(username).orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Creador no encontrado"));

            // Validar permisos de edición
            validationService.validateContentEditPermission(creador, TipoContenido.VIDEO);

            // Obtener contenido existente
            ContenidosVideo existingContent = contenidoVideoRepository.findById(contentId).orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, CONTENIDO_VIDEO_NO_ENCONTRADO));

            // Validar datos del DTO
            String validationResult = validationService.validateContentUpdate(updateDTO);
            if (validationResult != null) {
                return validationResult;
            }

            // Actualizar campos
            if (updateDTO.getTitulo() != null) {
                existingContent.setTitulo(updateDTO.getTitulo());
            }
            if (updateDTO.getDescripcion() != null) {
                existingContent.setDescripcion(updateDTO.getDescripcion());
            }
            if (updateDTO.getEsVIP() != null) {
                existingContent.setEsVIP(updateDTO.getEsVIP());
            }
            // Si se cambia flagVip a false y la resolución era 4K, cambiarla a 1080 por defecto
            if (updateDTO.getEsVIP() != null && !updateDTO.getEsVIP() && existingContent.getResolucion() == Resolucion.UHD_2160) {
                existingContent.setResolucion(Resolucion.FHD_1080);
            }
            if (updateDTO.getVisibilidad() != null) {
                existingContent.setVisibilidad(updateDTO.getVisibilidad());
            }
            if (updateDTO.getFechaExpiracion() != null) {
                existingContent.setFechaDisponibleHasta(ContentProcessingUtil.parseFechaExpiracion(updateDTO.getFechaExpiracion()));
            }
            if (updateDTO.getRestriccionEdad() != null) {
                existingContent.setRestriccionEdad(getRestriccionEdadFromValue(updateDTO.getRestriccionEdad()));
            }

            // Actualizar tags
            tagProcessor.updateContentTags(
                contentId,
                updateDTO.getTags(),
                () -> contenidoVideoTagRepository.findByIdContenido(contentId)
                         .stream().map(ContenidoVideoTag::getIdTag).toList(),
                tagId -> contenidoVideoTagRepository.deleteByIdContenidoAndIdTag(contentId, tagId),
                relation -> contenidoVideoTagRepository.save((ContenidoVideoTag) relation)
            );

            // Guardar cambios
            contenidoVideoRepository.save(existingContent);

            logger.info("Contenido de video {} actualizado exitosamente por {}", contentId, username);
            return "SUCCESS:Contenido de video actualizado exitosamente";

        }
        catch (ResponseStatusException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Error actualizando contenido de video {}: {}", contentId, e.getMessage());
            return ERROR_INTERNO_SERVIDOR;
        }
    }

    public void incrementarVisualizaciones(String contentId) {
        try {
            ContenidosVideo content = contenidoVideoRepository.findById(contentId).orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, CONTENIDO_VIDEO_NO_ENCONTRADO));
            content.setVisualizaciones(content.getVisualizaciones() + 1);
            contenidoVideoRepository.save(content);
            logger.info("Visualizaciones incrementadas para video {}", contentId);
        } 
        catch (Exception e) {
            logger.error("Error incrementando visualizaciones para video {}: {}", contentId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO_SERVIDOR);
        }
    }

    /**
     * Elimina un contenido de video tomando un id
     * @param authHeader Header de autorización con JWT
     * @param contentId ID del contenido de video a eliminar
     */
    public void deleteVideoContent(String authHeader, String contentId) {
        try {
            // Validar autorización para eliminar
            contentAuthorizationService.validateContentDeletion(authHeader, contentId);
            
            // Verificar que el contenido existe (lanzar NOT_FOUND si no existe)
            contenidoVideoRepository.findById(contentId).orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, CONTENIDO_VIDEO_NO_ENCONTRADO));
            
            // Eliminar relaciones con tags
            contenidoVideoTagRepository.deleteByIdContenido(contentId);
            
            // Eliminar valoraciones
            List<ValoracionContenido> valoraciones = valoracionRepository.findByIdContenido(contentId);
            valoracionRepository.deleteAll(valoraciones);
            
            // Eliminar el contenido
            contenidoVideoRepository.deleteById(contentId);
            
            logger.info("Contenido de video {} eliminado exitosamente", contentId);
        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error("Error eliminando contenido de video {}: {}", contentId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO_SERVIDOR);
        }
    }
}
