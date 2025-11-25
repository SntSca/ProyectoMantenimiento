package com.esimedia.features.content.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.entity.ContenidoAudioTag;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.entity.Tags;
import com.esimedia.features.content.entity.ValoracionContenido;
import com.esimedia.features.content.repository.ContenidoAudioTagRepository;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.TagsRepository;
import com.esimedia.features.content.repository.ValoracionContenidoRepository;
import com.esimedia.shared.util.ContentProcessingUtil;
import com.esimedia.shared.util.ContentUtil;
import com.esimedia.shared.util.JwtValidationUtil;

import java.util.Date;
import java.util.List;

@Service
public class AudioContentRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(AudioContentRetrievalService.class);

    private final JwtValidationUtil jwtValidationService;
    private final UsuarioNormalRepository usuarioNormalRepository;
    private final ValidationService validationService;
    private final ContenidosAudioRepository contenidoAudioRepository;
    private final ContenidoAudioTagRepository contenidoAudioTagRepository;
    private final TagsRepository tagsRepository;
    private final ValoracionContenidoRepository valoracionRepository;

    public AudioContentRetrievalService(
        JwtValidationUtil jwtValidationService,
        UsuarioNormalRepository usuarioNormalRepository,
        ValidationService validationService,
        ContenidosAudioRepository contenidoAudioRepository,
        ContenidoAudioTagRepository contenidoAudioTagRepository,
        TagsRepository tagsRepository,
        ValoracionContenidoRepository valoracionRepository
    ) {
        this.jwtValidationService = jwtValidationService;
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.validationService = validationService;
        this.contenidoAudioRepository = contenidoAudioRepository;
        this.contenidoAudioTagRepository = contenidoAudioTagRepository;
        this.tagsRepository = tagsRepository;
        this.valoracionRepository = valoracionRepository;
    }

    public List<ContentAudioUploadDTO> getAllAudiosAsDTO(String authHeader) {
        String username = jwtValidationService.validateContentAccess(authHeader, TipoContenido.AUDIO);
        Rol userRole = jwtValidationService.getRolFromToken(authHeader);
        logger.debug("Usuario {} con rol {} autorizado para ver lista de audios", username, userRole);

        Integer userAge = getUserAge(username, userRole);

        List<ContenidosAudio> audios = contenidoAudioRepository.findAll();
        updateExpiredVisibility(audios);

        return audios.stream()
            .filter(audio -> isAudioVisibleToUser(audio, userRole, userAge))
            .map(audio -> mapAudioToDTO(audio, username))
            .toList();
    }

    public ContenidosAudio getAudioById(String id) {
        return contenidoAudioRepository.findById(id).orElse(null);
    }

    public ContentAudioUploadDTO getAudioByIdAsDTO(String authHeader, String id) {
        String username = jwtValidationService.validateContentAccess(authHeader, TipoContenido.AUDIO);
        ContenidosAudio audio = contenidoAudioRepository.findById(id).orElse(null);
        if (audio == null) {
            return null;
        }
        return mapAudioToDTO(audio, username);
    }

    private Integer getUserAge(String username, Rol userRole) {
        if (userRole != Rol.NORMAL) {
            return null;
        }
        UsuarioNormal user = usuarioNormalRepository.findById(username).orElse(null);
        Integer userAge = (user != null && user.getFechaNacimiento() != null) ?
            validationService.calculateAge(user.getFechaNacimiento()) : null;
        logger.debug("Usuario NORMAL encontrado: {}, edad calculada: {}", user != null, userAge);
        return userAge;
    }

    private void updateExpiredVisibility(List<ContenidosAudio> audios) {
        audios.forEach(audio -> {
            if (audio.getFechaDisponibleHasta() != null && audio.getFechaDisponibleHasta().before(new Date())) {
                audio.setVisibilidad(false);
                contenidoAudioRepository.save(audio);
            }
        });
    }

    private boolean isAudioVisibleToUser(ContenidosAudio audio, Rol userRole, Integer userAge) {
        if (userRole != Rol.NORMAL) {
            return true;
        }

        if (!audio.isVisibilidad()) {
            logger.debug("Audio {} filtrado por visibilidad", audio.getId());
            return false;
        }

        if (audio.getRestriccionEdad() != null && userAge != null) {
            if (userAge < audio.getRestriccionEdad().getValor()) {
                logger.debug("Audio {} filtrado: edad {} < restricción {}",
                    audio.getId(), userAge, audio.getRestriccionEdad().getValor());
                return false;
            }
        }

        return true;
    }

    private ContentAudioUploadDTO mapAudioToDTO(ContenidosAudio audio, String username) {
        ContentAudioUploadDTO dto = new ContentAudioUploadDTO();
        dto.setId(audio.getId());
        dto.setTitulo(audio.getTitulo());
        dto.setDescripcion(audio.getDescripcion());
        dto.setDuracion(audio.getDuracion());
        dto.setEsVIP(audio.isEsVIP());
        dto.setEspecialidad(audio.getEspecialidad());
        dto.setRestriccionEdad(audio.getRestriccionEdad() != null ? audio.getRestriccionEdad().getValor() : null);
        dto.setValoracionMedia(audio.getValoracionMedia());
        dto.setVisualizaciones(audio.getVisualizaciones());

        ValoracionContenido userRating = valoracionRepository.findByIdContenidoAndIdUsuario(audio.getId(), username);
        dto.setValoracionUsuario(userRating != null ? userRating.getValoracion() : null);

        // Formatear fechas en formato yyyy-MM-dd (consistente con fecha de expiración)
        if (audio.getFechaSubida() != null) {
            dto.setFechaSubida(ContentProcessingUtil.formatDate(audio.getFechaSubida()));
        } else {
            logger.warn("Audio {} tiene fechaSubida null", audio.getId());
            dto.setFechaSubida(null);
        }

        if (audio.getFechaDisponibleHasta() != null) {
            dto.setFechaExpiracion(ContentProcessingUtil.formatDate(audio.getFechaDisponibleHasta()));
        }

        List<ContenidoAudioTag> audioTags = contenidoAudioTagRepository.findByIdContenido(audio.getId());
        List<String> tagIds = audioTags.stream()
            .map(ContenidoAudioTag::getIdTag)
            .toList();
        List<Tags> tags = tagsRepository.findByIdTagIn(tagIds);
        List<String> tagNames = tags.stream()
            .map(Tags::getNombre)
            .toList();
        dto.setTags(tagNames);

        String ficheroDataUri = ContentUtil.createDataUriFromBinary(audio.getFichero(), audio.getFicheroExtension());
        dto.setFichero(ficheroDataUri);
        dto.setFicheroExtension(audio.getFicheroExtension());

        String miniaturaDataUri = ContentUtil.createDataUri(audio.getMiniatura(), audio.getFormatoMiniatura());
        dto.setMiniatura(miniaturaDataUri);
        dto.setFormatoMiniatura(audio.getFormatoMiniatura());

        dto.setVisibilidad(audio.isVisibilidad());

        return dto;
    }
}