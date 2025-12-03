package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.ListaUpdateFieldsPublicasDTO;
import com.esimedia.features.lists.entity.ListaPublica;
import com.esimedia.features.lists.repository.ListaContenidoPublicaRepository;
import com.esimedia.features.lists.repository.ListaPublicaRepository;

import java.util.Optional;

@Service
public class PublicListUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(PublicListUpdateService.class);

    private static final String LISTA_PUBLICA_NO_ENCONTRADA = "Lista pública no encontrada";

    private final ListaPublicaRepository listaPublicaRepository;
    private final ListaContenidoPublicaRepository listaContenidoPublicaRepository;

    public PublicListUpdateService(
            ListaPublicaRepository listaPublicaRepository,
            ListaContenidoPublicaRepository listaContenidoPublicaRepository) {
        this.listaPublicaRepository = listaPublicaRepository;
        this.listaContenidoPublicaRepository = listaContenidoPublicaRepository;
    }

    /**
     * Eliminar lista pública
     */
    public String eliminarListaPublica(String idLista, String idUsuario) {
        logger.info("Eliminando lista pública: {} por usuario: {}", idLista, idUsuario);
        
        // Verificar que la lista existe
        Optional<ListaPublica> listaOpt = listaPublicaRepository.findById(idLista);
        if (listaOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, LISTA_PUBLICA_NO_ENCONTRADA);
        }
        
        ListaPublica lista = listaOpt.get();
        
        // Verificar que el usuario es el creador de la lista
        if (!lista.getIdCreadorUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "No tienes permiso para eliminar esta lista");
        }
        
        // Eliminar todos los contenidos de la lista
        listaContenidoPublicaRepository.deleteByIdLista(idLista);
        
        // Eliminar la lista
        listaPublicaRepository.delete(lista);
        
        logger.info("Lista pública eliminada exitosamente: {}", idLista);
        return "Lista pública eliminada exitosamente";
    }

    /**
     * Actualizar campos principales de una lista pública (nombre, descripción, visibilidad)
     * @param updateDTO DTO con los nuevos valores
     * @param idUsuario ID del usuario (debe ser el propietario)
     * @return Mensaje de confirmación
     */
    public String actualizarCamposListaPublica(ListaUpdateFieldsPublicasDTO updateDTO) {
        ListaPublica lista = listaPublicaRepository.findById(updateDTO.getIdLista())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
        
        // Actualizar campos
        lista.setNombre(updateDTO.getNombre());
        lista.setDescripcion(updateDTO.getDescripcion());
        lista.setVisibilidad(updateDTO.getVisibilidad());
        
        listaPublicaRepository.save(lista);
        
        return "Lista pública actualizada exitosamente";
    }
}