package com.esimedia.features.content.services;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.esimedia.features.content.entity.Contenido;
import com.esimedia.features.content.entity.ValoracionContenido;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.features.content.repository.ValoracionContenidoRepository;

import java.util.List;

@Service
public class ValoracionService {
    
    private final ValoracionContenidoRepository valoracionRepository;
    private final ContenidosAudioRepository audioRepository;
    private final ContenidosVideoRepository videoRepository;
    
    public ValoracionService(ValoracionContenidoRepository valoracionRepository,
                           ContenidosAudioRepository audioRepository,
                           ContenidosVideoRepository videoRepository) {
        this.valoracionRepository = valoracionRepository;
        this.audioRepository = audioRepository;
        this.videoRepository = videoRepository;
    }
    
    public void valorarContenido(String idContenido, String idUsuario, double valoracion) {
        if (valoracion < 0 || valoracion > 5) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La valoración debe estar entre 0 y 5"
            );
        }
        double resto = valoracion * 10 % 5;
        if (resto != 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La valoración solo puede tener pasos de 0.5 (0, 0.5, 1, 1.5, ..., 5)"
            );
        }

        Contenido contenido = audioRepository.findById(idContenido).orElse(null);
        if (contenido == null) {
            contenido = videoRepository.findById(idContenido).orElse(null);
        }
        if (contenido == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenido no encontrado");
        }


        ValoracionContenido existingValoracion =
            valoracionRepository.findByIdContenidoAndIdUsuario(idContenido, idUsuario);

        if (existingValoracion != null) {
            
            existingValoracion.setValoracion(valoracion);
            existingValoracion.setFechaValoracion(new java.util.Date());
            valoracionRepository.save(existingValoracion);
        } else {
            
            ValoracionContenido nuevaValoracion =
                new ValoracionContenido(idContenido, idUsuario, valoracion);
            valoracionRepository.save(nuevaValoracion);
        }

        actualizarValoracionMedia(idContenido);
    }

    
    private void actualizarValoracionMedia(String idContenido) {
        List<ValoracionContenido> valoraciones = valoracionRepository.findByIdContenido(idContenido);

        double media = valoraciones.stream()
            .mapToDouble(ValoracionContenido::getValoracion)  
            .average()
            .orElse(0.0);

        
        Contenido contenido = audioRepository.findById(idContenido).orElse(null);
        if (contenido != null) {
            contenido.setValoracionMedia(media);
            audioRepository.save((com.esimedia.features.content.entity.ContenidosAudio) contenido);
        } else {
            contenido = videoRepository.findById(idContenido).orElse(null);
            if (contenido != null) {
                contenido.setValoracionMedia(media);
                videoRepository.save((com.esimedia.features.content.entity.ContenidosVideo) contenido);
            }
        }
    }

    
    public double getValoracionMedia(String idContenido) {
        Contenido contenido = audioRepository.findById(idContenido).orElse(null);
        if (contenido == null) {
            contenido = videoRepository.findById(idContenido).orElse(null);
        }
        return contenido != null ? contenido.getValoracionMedia() : 0.0;
    }

    
    /**
     * Elimina todas las valoraciones de un usuario y actualiza las valoraciones medias
     * de todos los contenidos afectados.
     * @param idUsuario ID del usuario cuyas valoraciones se eliminarán
     */
    public void eliminarValoracionesDeUsuario(String idUsuario) {
        // Obtener todas las valoraciones del usuario antes de eliminarlas
        List<ValoracionContenido> valoracionesUsuario = valoracionRepository.findByIdUsuario(idUsuario);
        
        // Obtener los IDs de contenidos únicos que tienen valoraciones de este usuario
        List<String> contenidosAfectados = valoracionesUsuario.stream()
            .map(ValoracionContenido::getIdContenido)
            .distinct()
            .toList();
        
        // Eliminar todas las valoraciones del usuario
        valoracionRepository.deleteByIdUsuario(idUsuario);
        
        // Actualizar la valoración media de cada contenido afectado
        for (String idContenido : contenidosAfectados) {
            actualizarValoracionMedia(idContenido);
        }
    }
}