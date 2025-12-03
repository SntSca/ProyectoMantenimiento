package com.esimedia.features.favoritos.services;

import java.util.Optional;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.esimedia.features.favoritos.entity.ContenidoFavorito;
import com.esimedia.features.favoritos.repository.ContenidoFavoritoRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.favoritos.dto.FavoritoDTO;

@Service
public class FavoritoService {

    private static final Logger logger = LoggerFactory.getLogger(FavoritoService.class);

    private final ContenidoFavoritoRepository favoritoRepository;
    private final ContenidosVideoRepository videoRepository;
    private final ContenidosAudioRepository audioRepository;

    public FavoritoService(ContenidoFavoritoRepository favoritoRepository,
                           ContenidosVideoRepository videoRepository,
                           ContenidosAudioRepository audioRepository) {
        this.favoritoRepository = favoritoRepository;
        this.videoRepository = videoRepository;
        this.audioRepository = audioRepository;
    }

    /**
     * Añadir contenido a favoritos
     */
    public String agregarFavorito(String idUsuario, FavoritoDTO favoritoDTO) {
        try {
            logger.info("Intentando agregar favorito: usuario={}, contenido={}",
                    idUsuario, favoritoDTO.getIdContenido());
            
            if (!videoRepository.existsById(favoritoDTO.getIdContenido()) &&
                        !audioRepository.existsById(favoritoDTO.getIdContenido())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenido no encontrado");
            }

            if (favoritoRepository.existsByIdUsuarioAndIdContenido(idUsuario, favoritoDTO.getIdContenido())) {
                logger.warn("El contenido ya estaba en favoritos");
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El contenido ya está en favoritos");
            }
            
            ContenidoFavorito nuevoFavorito = ContenidoFavorito.builder()
                .idUsuario(idUsuario)
                .idContenido(favoritoDTO.getIdContenido())
                .build();

            favoritoRepository.save(nuevoFavorito);

            logger.info("Favorito agregado correctamente para usuario {}", idUsuario);
            return "SUCCESS: Contenido agregado a favoritos";
        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error("Error agregando favorito: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al agregar favorito");
        }
    }

    /**
     * Eliminar contenido de favoritos
     */
    public String eliminarFavorito(String idUsuario, FavoritoDTO favoritoDTO) {
        try {
            logger.info("Eliminando favorito: usuario={}, contenido={}",
                    idUsuario, favoritoDTO.getIdContenido());

            Optional<ContenidoFavorito> favorito = favoritoRepository.findByIdUsuarioAndIdContenido(idUsuario,
                            favoritoDTO.getIdContenido());

            if (favorito.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorito no encontrado");
            }

            favoritoRepository.delete(favorito.get());
            return "SUCCESS: Favorito eliminado correctamente";

        } 
        catch (ResponseStatusException e) {
            throw e;
        } 
        catch (Exception e) {
            logger.error("Error eliminando favorito: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar favorito");
        }
    }

    /**
     * Obtener lista de favoritos del usuario
     */
    public List<String> obtenerFavoritosPorUsuario(String idUsuario) {
        logger.info("Consultando favoritos del usuario {}", idUsuario);

        return favoritoRepository.findByIdUsuario(idUsuario)
                .stream()
                .map(ContenidoFavorito::getIdContenido)
                .toList();
    }
}
