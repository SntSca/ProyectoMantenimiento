package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.AgregarContenidoDTO;
import com.esimedia.features.lists.dto.AgregarContenidoPublicoDTO;
import com.esimedia.features.lists.dto.EliminarContenidoPublicoDTO;
import com.esimedia.features.lists.entity.ListaContenidoPublica;
import com.esimedia.features.lists.entity.ListaPublica;
import com.esimedia.features.lists.repository.ListaContenidoPublicaRepository;
import com.esimedia.features.lists.repository.ListaPublicaRepository;
import com.esimedia.shared.util.ContentValidationUtil;

import java.util.Optional;

@Service
public class PublicListContentService {

    private static final Logger logger = LoggerFactory.getLogger(PublicListContentService.class);

    private static final String LISTA_PUBLICA_NO_ENCONTRADA = "Lista pública no encontrada";

    private final ListaPublicaRepository listaPublicaRepository;
    private final ListaContenidoPublicaRepository listaContenidoPublicaRepository;
    private final ContentValidationUtil contentValidationUtil;

    public PublicListContentService(
            ListaPublicaRepository listaPublicaRepository,
            ListaContenidoPublicaRepository listaContenidoPublicaRepository,
            ContentValidationUtil contentValidationUtil) {
        this.listaPublicaRepository = listaPublicaRepository;
        this.listaContenidoPublicaRepository = listaContenidoPublicaRepository;
        this.contentValidationUtil = contentValidationUtil;
    }

    /**
     * Agregar contenido a una lista pública
     */
    public String agregarContenidoListaPublica(AgregarContenidoDTO agregarDTO) {
        logger.info("Agregando contenidos a lista pública {}", agregarDTO.getIdLista());
        
        // Verificar que la lista existe
        Optional<ListaPublica> listaOpt = listaPublicaRepository.findById(agregarDTO.getIdLista());
        if (listaOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LISTA_PUBLICA_NO_ENCONTRADA);
        }
        
        for (String idContenido : agregarDTO.getIdsContenido()) {
            // Verificar que el contenido no está ya en la lista
            if (listaContenidoPublicaRepository.existsByIdListaAndIdContenido(
                    agregarDTO.getIdLista(), idContenido)) {
                logger.warn("Contenido {} ya existe en la lista pública {}", idContenido, agregarDTO.getIdLista());
                continue;
            }
            
            // Validar que el contenido existe
            contentValidationUtil.validarContenidoExistente(idContenido);

            // Agregar el contenido
            ListaContenidoPublica contenido = ListaContenidoPublica.builder()
                .idLista(agregarDTO.getIdLista())
                .idContenido(idContenido)
                .build();
            listaContenidoPublicaRepository.save(contenido);
        }
        
        return "Contenidos agregados a la lista pública exitosamente";
    }
    
    /**
     * Agregar contenido a una lista pública (versión simplificada sin idUsuario)
     */
    public String agregarContenidoListaPublica(AgregarContenidoPublicoDTO agregarDTO) {
        logger.info("Agregando contenidos a lista pública {}", agregarDTO.getIdLista());
        
        // Verificar que la lista existe
        Optional<ListaPublica> listaOpt = listaPublicaRepository.findById(agregarDTO.getIdLista());
        if (listaOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LISTA_PUBLICA_NO_ENCONTRADA);
        }
        
        for (String idContenido : agregarDTO.getIdsContenido()) {
            // Verificar que el contenido no está ya en la lista
            if (listaContenidoPublicaRepository.existsByIdListaAndIdContenido(
                    agregarDTO.getIdLista(), idContenido)) {
                logger.warn("Contenido {} ya existe en la lista pública {}", idContenido, agregarDTO.getIdLista());
                continue;
            }
            
            // Validar que el contenido existe
            contentValidationUtil.validarContenidoExistente(idContenido);

            // Agregar el contenido
            ListaContenidoPublica contenido = ListaContenidoPublica.builder()
                .idLista(agregarDTO.getIdLista())
                .idContenido(idContenido)
                .build();
            listaContenidoPublicaRepository.save(contenido);
        }
        
        return "Contenidos agregados a la lista pública exitosamente";
    }

    /**
     * Eliminar contenido de una lista pública
     */
    public String eliminarContenidoListaPublica(EliminarContenidoPublicoDTO eliminarDTO) {
        logger.info("Eliminando contenidos {} de lista pública {}", eliminarDTO.getIdsContenido(), eliminarDTO.getIdLista());

        // Verificar que la lista existe
        Optional<ListaPublica> listaOpt = listaPublicaRepository.findById(eliminarDTO.getIdLista());
        if (listaOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LISTA_PUBLICA_NO_ENCONTRADA);
        }

        for (String idContenido : eliminarDTO.getIdsContenido()) {
            // Verificar que el contenido existe en la lista
            if (!listaContenidoPublicaRepository.existsByIdListaAndIdContenido(
                    eliminarDTO.getIdLista(), idContenido)) {
                logger.warn("Contenido {} no encontrado en la lista pública {}", idContenido, eliminarDTO.getIdLista());
                continue;
            }

            // Eliminar el contenido
            listaContenidoPublicaRepository.deleteByIdListaAndIdContenido(
                eliminarDTO.getIdLista(), idContenido);
        }

        return "Contenidos eliminados de la lista pública exitosamente";
    }
}