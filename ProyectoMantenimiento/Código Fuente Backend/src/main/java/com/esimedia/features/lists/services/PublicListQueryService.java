package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.lists.dto.ContenidoListaResponseDTO;
import com.esimedia.features.lists.dto.ListaContenidosResponseDTO;
import com.esimedia.features.lists.dto.ListaResponseDTO;
import com.esimedia.features.lists.entity.ListaContenidoPublica;
import com.esimedia.features.lists.entity.ListaPublica;
import com.esimedia.features.lists.repository.ListaContenidoPublicaRepository;
import com.esimedia.features.lists.repository.ListaPublicaRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class PublicListQueryService {

    private static final Logger logger = LoggerFactory.getLogger(PublicListQueryService.class);


    private final ListaPublicaRepository listaPublicaRepository;
    private final ListaContenidoPublicaRepository listaContenidoPublicaRepository;
    private final PublicListHelper publicListHelper;

    public PublicListQueryService(
            ListaPublicaRepository listaPublicaRepository,
            ListaContenidoPublicaRepository listaContenidoPublicaRepository,
            PublicListHelper publicListHelper) {
        this.listaPublicaRepository = listaPublicaRepository;
        this.listaContenidoPublicaRepository = listaContenidoPublicaRepository;
        this.publicListHelper = publicListHelper;
    }

    /**
     * Obtener todas las listas públicas
     */
    public List<ListaResponseDTO> obtenerListasPublicas() {
        logger.info("Obteniendo todas las listas públicas");

        List<ListaPublica> listas = listaPublicaRepository.findAll();
        return listas.stream()
            .map(this::convertirListaPublicaAResponseDTO)
            .toList();
    }

    /**
     * Obtener una lista pública por ID con contenidos completos
     * @param idLista ID de la lista
     * @return DTO con información de la lista y contenidos completos
     */
    public ListaContenidosResponseDTO obtenerListaPublicaConContenidos(String idLista) {
        ListaPublica lista = listaPublicaRepository.findById(idLista)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
        
        // Obtener IDs de contenidos de la lista
        List<String> idsContenidos = listaContenidoPublicaRepository
            .findByIdLista(lista.getIdLista())
            .stream()
            .map(ListaContenidoPublica::getIdContenido)
            .toList();
        
        // Obtener contenidos completos
        List<ContenidoListaResponseDTO> contenidosCompletos = publicListHelper.obtenerContenidosCompletos(idsContenidos);
        
        return ListaContenidosResponseDTO.builder()
            .idLista(lista.getIdLista())
            .nombre(lista.getNombre())
            .descripcion(lista.getDescripcion())
            .idCreadorUsuario(lista.getIdCreadorUsuario())
            .visibilidad(lista.getVisibilidad())
            .contenidos(contenidosCompletos)
            .build();
    }

    /**
     * Obtener todas las listas públicas con contenidos completos
     * @return Lista de todas las listas públicas con sus contenidos
     */
    public List<ListaResponseDTO> obtenerTodasListasPublicasConContenidos() {
        logger.info("Obteniendo todas las listas públicas con contenidos");
        List<ListaPublica> listas = listaPublicaRepository.findAll();
        List<ListaResponseDTO> listasDTO = new ArrayList<>();
        
        for (ListaPublica lista : listas) {
            List<ListaContenidoPublica> contenidos = listaContenidoPublicaRepository.findByIdLista(lista.getIdLista());
            // Obtener IDs de contenidos
            List<String> idsContenidos = contenidos.stream()
                .map(ListaContenidoPublica::getIdContenido)
                .toList();

            // Obtener contenidos completos
            List<ContenidoListaResponseDTO> contenidosCompletos = publicListHelper.obtenerContenidosCompletos(idsContenidos);
            
            // Construir el DTO de respuesta
            ListaResponseDTO listaDTO = ListaResponseDTO.builder()
                .idLista(lista.getIdLista())
                .nombre(lista.getNombre())
                .descripcion(lista.getDescripcion())
                .idCreadorUsuario(lista.getIdCreadorUsuario())
                .visibilidad(lista.getVisibilidad())
                .contenidos(contenidosCompletos)
                .build();
            listasDTO.add(listaDTO);
        }
        
        logger.info("Encontradas {} listas públicas con contenidos", listasDTO.size());
        return listasDTO;
    }

    /**
     * Convertir ListaPublica a ListaResponseDTO
     */
    private ListaResponseDTO convertirListaPublicaAResponseDTO(ListaPublica lista) {
        List<String> idsContenidos = listaContenidoPublicaRepository
            .findByIdLista(lista.getIdLista())
            .stream()
            .map(ListaContenidoPublica::getIdContenido)
            .toList();

        List<ContenidoListaResponseDTO> contenidosCompletos = publicListHelper.obtenerContenidosCompletos(idsContenidos);

        return ListaResponseDTO.builder()
            .idLista(lista.getIdLista())
            .nombre(lista.getNombre())
            .descripcion(lista.getDescripcion())
            .idCreadorUsuario(lista.getIdCreadorUsuario())
            .visibilidad(lista.getVisibilidad())
            .contenidos(contenidosCompletos)
            .build();
    }
}