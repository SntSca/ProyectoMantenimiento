package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.ListaUpdateDTO;
import com.esimedia.features.lists.dto.ListaUpdateFieldsPrivadasDTO;
import com.esimedia.features.lists.entity.ListaPrivada;
import com.esimedia.features.lists.repository.ListaContenidoPrivadaRepository;
import com.esimedia.features.lists.repository.ListaPrivadaRepository;

import java.util.Optional;

@Service
public class PrivateListUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateListUpdateService.class);

    private static final String LISTA_PRIVADA_NO_ENCONTRADA = "Lista privada no encontrada";

    private final ListaPrivadaRepository listaPrivadaRepository;
    private final ListaContenidoPrivadaRepository listaContenidoPrivadaRepository;

    public PrivateListUpdateService(
            ListaPrivadaRepository listaPrivadaRepository,
            ListaContenidoPrivadaRepository listaContenidoPrivadaRepository) {
        this.listaPrivadaRepository = listaPrivadaRepository;
        this.listaContenidoPrivadaRepository = listaContenidoPrivadaRepository;
    }

    /**
     * Actualizar una lista privada
     */
    public String actualizarListaPrivada(ListaUpdateDTO updateDTO, String idUsuario) {
        logger.info("Actualizando lista privada: {} por usuario: {}", updateDTO.getIdLista(), idUsuario);

        // Verificar que la lista existe
        Optional<ListaPrivada> listaOpt = listaPrivadaRepository.findById(updateDTO.getIdLista());
        if (listaOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LISTA_PRIVADA_NO_ENCONTRADA);
        }

        ListaPrivada lista = listaOpt.get();

        // Verificar que el usuario es el propietario
        if (!lista.getIdCreadorUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No tienes permiso para actualizar esta lista");
        }

        // Actualizar campos
        lista.setNombre(updateDTO.getNombre());
        lista.setDescripcion(updateDTO.getDescripcion());
        lista.setVisibilidad(updateDTO.getVisibilidad());

        listaPrivadaRepository.save(lista);

        logger.info("Lista privada actualizada exitosamente: {}", updateDTO.getIdLista());
        return "Lista privada actualizada exitosamente";
    }

    /**
     * Actualizar campos principales de una lista privada (nombre, descripción, visibilidad)
     * @param updateDTO DTO con los nuevos valores
     * @param idUsuario ID del usuario (debe ser el propietario)
     * @return Mensaje de confirmación
     */
    public String actualizarCamposListaPrivada(ListaUpdateFieldsPrivadasDTO updateDTO, String idUsuario) {
        ListaPrivada lista = listaPrivadaRepository.findById(updateDTO.getIdLista())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));

        // Verificar que el usuario es el propietario
        if (!lista.getIdCreadorUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No tienes permisos para modificar esta lista privada");
        }

        // Actualizar campos
        lista.setNombre(updateDTO.getNombre());
        lista.setDescripcion(updateDTO.getDescripcion());
        // Las listas privadas siempre mantienen su visibilidad (false)

        listaPrivadaRepository.save(lista);

        return "Lista privada actualizada exitosamente";
    }

    /**
     * Eliminar lista privada
     */
    public String eliminarListaPrivada(String idLista, String idUsuario) {
        logger.info("Eliminando lista privada: {} por usuario: {}", idLista, idUsuario);

        // Verificar que la lista existe
        Optional<ListaPrivada> listaOpt = listaPrivadaRepository.findById(idLista);
        if (listaOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LISTA_PRIVADA_NO_ENCONTRADA);
        }

        ListaPrivada lista = listaOpt.get();

        // Verificar que el usuario es el propietario de la lista
        if (!lista.getIdCreadorUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No tienes permiso para eliminar esta lista");
        }

        // Eliminar todos los contenidos de la lista
        listaContenidoPrivadaRepository.deleteByIdLista(idLista);

        // Eliminar la lista
        listaPrivadaRepository.delete(lista);

        logger.info("Lista privada eliminada exitosamente: {}", idLista);
        return "Lista privada eliminada exitosamente";
    }
}