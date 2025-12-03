package com.esimedia.features.lists.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.esimedia.features.lists.dto.ContenidoListaResponseDTO;
import com.esimedia.features.lists.dto.ListaPrivadaResponseDTO;
import com.esimedia.features.lists.entity.ListaContenidoPrivada;
import com.esimedia.features.lists.entity.ListaPrivada;
import com.esimedia.features.lists.repository.ListaContenidoPrivadaRepository;
import com.esimedia.features.lists.repository.ListaPrivadaRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrivateListQueryService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateListQueryService.class);

    private final ListaPrivadaRepository listaPrivadaRepository;
    private final ListaContenidoPrivadaRepository listaContenidoPrivadaRepository;
    private final PrivateListHelper privateListHelper;

    public PrivateListQueryService(
            ListaPrivadaRepository listaPrivadaRepository,
            ListaContenidoPrivadaRepository listaContenidoPrivadaRepository,
            PrivateListHelper privateListHelper) {
        this.listaPrivadaRepository = listaPrivadaRepository;
        this.listaContenidoPrivadaRepository = listaContenidoPrivadaRepository;
        this.privateListHelper = privateListHelper;
    }

    /**
     * Obtener todas las listas privadas de un usuario con contenidos completos
     * @param idUsuario ID del usuario
     * @return Lista de DTOs con información de cada lista y contenidos completos
     */
    public List<ListaPrivadaResponseDTO> obtenerTodasListasPrivadasConContenidos(String idUsuario) {
        logger.info("Obteniendo todas las listas privadas con contenidos del usuario: {}", idUsuario);
        List<ListaPrivada> listas = listaPrivadaRepository.findByIdCreadorUsuario(idUsuario);
        List<ListaPrivadaResponseDTO> listasDTO = new ArrayList<>();
        for (ListaPrivada lista : listas) {
            List<ListaContenidoPrivada> contenidos = listaContenidoPrivadaRepository.findByIdLista(lista.getIdLista());
            // Ahora tenemos los pares con idContenido e idLista, necesitamos los contenidos completos
            List<String> idsContenidos = contenidos.stream()
                .map(ListaContenidoPrivada::getIdContenido)
                .toList();

            // Llamamos a un método que busca por el id en ambas bd de contenidos
            List<ContenidoListaResponseDTO> contenidosCompletos = privateListHelper.obtenerContenidosCompletos(idsContenidos);

            // Construimos el DTO de respuesta
            ListaPrivadaResponseDTO listaDTO = ListaPrivadaResponseDTO.builder()
                .idLista(lista.getIdLista())
                .nombre(lista.getNombre())
                .descripcion(lista.getDescripcion())
                .idCreadorUsuario(lista.getIdCreadorUsuario())
                .contenidos(contenidosCompletos)
                .build();
            listasDTO.add(listaDTO);
        }

        return listasDTO;
    }
}