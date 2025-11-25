package com.esimedia.shared.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;

/**
 * Utilidad para validaciones comunes de contenido
 */
@Component
public class ContentValidationUtil {

    private final ContenidosAudioRepository contenidoAudioRepository;
    private final ContenidosVideoRepository contenidoVideoRepository;

    public ContentValidationUtil(ContenidosAudioRepository contenidoAudioRepository,
                                ContenidosVideoRepository contenidoVideoRepository) {
        this.contenidoAudioRepository = contenidoAudioRepository;
        this.contenidoVideoRepository = contenidoVideoRepository;
    }

    /**
     * Valida que un contenido existe en la base de datos (audio o video)
     * @param idContenido ID del contenido a validar
     * @throws ResponseStatusException si el contenido no existe
     */
    public void validarContenidoExistente(String idContenido) {
        if (!contenidoAudioRepository.existsById(idContenido) &&
            !contenidoVideoRepository.existsById(idContenido)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenido no encontrado");
        }
    }
}