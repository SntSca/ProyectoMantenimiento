package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.AgregarContenidoDTO;
import com.esimedia.features.lists.dto.EliminarContenidoDTO;
import com.esimedia.features.lists.entity.ListaContenidoPrivada;
import com.esimedia.features.lists.entity.ListaPrivada;
import com.esimedia.features.lists.repository.ListaContenidoPrivadaRepository;
import com.esimedia.features.lists.repository.ListaPrivadaRepository;
import com.esimedia.shared.util.ContentValidationUtil;

import java.util.Optional;

@Service
public class PrivateListContentService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateListContentService.class);

    private static final String LISTA_PRIVADA_NO_ENCONTRADA = "Lista privada no encontrada";

    private final ListaPrivadaRepository listaPrivadaRepository;
    private final ListaContenidoPrivadaRepository listaContenidoPrivadaRepository;
    private final ContentValidationUtil contentValidationUtil;

    public PrivateListContentService(
            ListaPrivadaRepository listaPrivadaRepository,
            ListaContenidoPrivadaRepository listaContenidoPrivadaRepository,
            ContentValidationUtil contentValidationUtil) {
        this.listaPrivadaRepository = listaPrivadaRepository;
        this.listaContenidoPrivadaRepository = listaContenidoPrivadaRepository;
        this.contentValidationUtil = contentValidationUtil;
    }

    /**
     * Agregar contenido a una lista privada
     */
    public String agregarContenidoListaPrivada(AgregarContenidoDTO agregarDTO) {
        logger.info("Agregando contenidos a lista privada {}", agregarDTO.getIdLista());

        // Verificar que la lista existe
        Optional<ListaPrivada> listaOpt = listaPrivadaRepository.findById(agregarDTO.getIdLista());
        if (listaOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LISTA_PRIVADA_NO_ENCONTRADA);
        }

        ListaPrivada lista = listaOpt.get();

        // Verificar que el usuario es el propietario de la lista
        if (!lista.getIdCreadorUsuario().equals(agregarDTO.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No tienes permiso para modificar esta lista");
        }

        for (String idContenido : agregarDTO.getIdsContenido()) {
            // Verificar que el contenido no está ya en la lista
            if (listaContenidoPrivadaRepository.existsByIdListaAndIdContenido(
                    agregarDTO.getIdLista(), idContenido)) {
                logger.warn("Contenido {} ya existe en la lista privada {}", idContenido, agregarDTO.getIdLista());
                continue;
            }

            // Validar que el contenido existe (audio o video)
            contentValidationUtil.validarContenidoExistente(idContenido);

            // Agregar el contenido
            ListaContenidoPrivada contenido = ListaContenidoPrivada.builder()
                .idLista(agregarDTO.getIdLista())
                .idContenido(idContenido)
                .build();
            listaContenidoPrivadaRepository.save(contenido);
        }

        return "Contenidos agregados a la lista privada exitosamente";
    }

    /**
     * Eliminar contenido de una lista privada
     */
    public String eliminarContenidoListaPrivada(EliminarContenidoDTO eliminarDTO) {
        logger.info("Eliminando contenidos de lista privada {}", eliminarDTO.getIdLista());

        // Verificar que la lista existe
        Optional<ListaPrivada> listaOpt = listaPrivadaRepository.findById(eliminarDTO.getIdLista());
        if (listaOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LISTA_PRIVADA_NO_ENCONTRADA);
        }

        ListaPrivada lista = listaOpt.get();

        // Verificar que el usuario es el propietario de la lista
        if (!lista.getIdCreadorUsuario().equals(eliminarDTO.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No tienes permiso para modificar esta lista");
        }

        for (String idContenido : eliminarDTO.getIdsContenido()) {
            // Verificar que el contenido existe en la lista
            if (!listaContenidoPrivadaRepository.existsByIdListaAndIdContenido(
                    eliminarDTO.getIdLista(), idContenido)) {
                logger.warn("Contenido {} no encontrado en la lista privada {}", idContenido, eliminarDTO.getIdLista());
                continue;
            }

            // Eliminar el contenido
            listaContenidoPrivadaRepository.deleteByIdListaAndIdContenido(
                eliminarDTO.getIdLista(), idContenido);
        }

        return "Contenidos eliminados de la lista privada exitosamente";
    }
}