package com.esimedia.features.lists.services;

import org.springframework.stereotype.Service;

import com.esimedia.features.lists.dto.AgregarContenidoDTO;
import com.esimedia.features.lists.dto.EliminarContenidoDTO;
import com.esimedia.features.lists.dto.ListaPrivadaReproduccionDTO;
import com.esimedia.features.lists.dto.ListaPrivadaResponseDTO;
import com.esimedia.features.lists.dto.ListaUpdateDTO;
import com.esimedia.features.lists.dto.ListaUpdateFieldsPrivadasDTO;

import java.util.List;

@Service
public class PrivateListService {

    private final PrivateListCreationService privateListCreationService;
    private final PrivateListContentService privateListContentService;
    private final PrivateListQueryService privateListQueryService;
    private final PrivateListUpdateService privateListUpdateService;

    public PrivateListService(
            PrivateListCreationService privateListCreationService,
            PrivateListContentService privateListContentService,
            PrivateListQueryService privateListQueryService,
            PrivateListUpdateService privateListUpdateService) {
        this.privateListCreationService = privateListCreationService;
        this.privateListContentService = privateListContentService;
        this.privateListQueryService = privateListQueryService;
        this.privateListUpdateService = privateListUpdateService;
    }

    /**
     * Crear una lista de reproducción privada
     */
    public String crearListaPrivada(ListaPrivadaReproduccionDTO listaDTO) {
        return privateListCreationService.crearListaPrivada(listaDTO);
    }

    /**
     * Agregar contenido a una lista privada
     */
    public String agregarContenidoListaPrivada(AgregarContenidoDTO agregarDTO) {
        return privateListContentService.agregarContenidoListaPrivada(agregarDTO);
    }

    /**
     * Eliminar contenido de una lista privada
     */
    public String eliminarContenidoListaPrivada(EliminarContenidoDTO eliminarDTO) {
        return privateListContentService.eliminarContenidoListaPrivada(eliminarDTO);
    }

    /**
     * Eliminar lista privada
     */
    public String eliminarListaPrivada(String idLista, String idUsuario) {
        return privateListUpdateService.eliminarListaPrivada(idLista, idUsuario);
    }

    /**
     * Obtener todas las listas privadas de un usuario con contenidos completos
     * @param idUsuario ID del usuario
     * @return Lista de DTOs con información de cada lista y contenidos completos
     */
    public List<ListaPrivadaResponseDTO> obtenerTodasListasPrivadasConContenidos(String idUsuario) {
        return privateListQueryService.obtenerTodasListasPrivadasConContenidos(idUsuario);
    }

    /**
     * Actualizar una lista privada
     */
    public String actualizarListaPrivada(ListaUpdateDTO updateDTO, String idUsuario) {
        return privateListUpdateService.actualizarListaPrivada(updateDTO, idUsuario);
    }

    /**
     * Actualizar campos principales de una lista privada (nombre, descripción, visibilidad)
     * @param updateDTO DTO con los nuevos valores
     * @param idUsuario ID del usuario (debe ser el propietario)
     * @return Mensaje de confirmación
     */
    public String actualizarCamposListaPrivada(ListaUpdateFieldsPrivadasDTO updateDTO, String idUsuario) {
        return privateListUpdateService.actualizarCamposListaPrivada(updateDTO, idUsuario);
    }
}