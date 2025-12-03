package com.esimedia.features.lists.services;

import org.springframework.stereotype.Service;

import com.esimedia.features.lists.dto.AgregarContenidoDTO;
import com.esimedia.features.lists.dto.AgregarContenidoPublicoDTO;
import com.esimedia.features.lists.dto.EliminarContenidoPublicoDTO;
import com.esimedia.features.lists.dto.ListaContenidosResponseDTO;
import com.esimedia.features.lists.dto.ListaPublicaReproduccionDTO;
import com.esimedia.features.lists.dto.ListaResponseDTO;
import com.esimedia.features.lists.dto.ListaUpdateFieldsPublicasDTO;

import java.util.List;

@Service
public class PublicListService {

    private final PublicListCreationService publicListCreationService;
    private final PublicListContentService publicListContentService;
    private final PublicListQueryService publicListQueryService;
    private final PublicListUpdateService publicListUpdateService;

    public PublicListService(
            PublicListCreationService publicListCreationService,
            PublicListContentService publicListContentService,
            PublicListQueryService publicListQueryService,
            PublicListUpdateService publicListUpdateService) {
        this.publicListCreationService = publicListCreationService;
        this.publicListContentService = publicListContentService;
        this.publicListQueryService = publicListQueryService;
        this.publicListUpdateService = publicListUpdateService;
    }

    /**
     * Crear una lista de reproducción pública
     */
    public String crearListaPublica(ListaPublicaReproduccionDTO listaDTO) {
        return publicListCreationService.crearListaPublica(listaDTO);
    }

    /**
     * Agregar contenido a una lista pública
     */
    public String agregarContenidoListaPublica(AgregarContenidoDTO agregarDTO) {
        return publicListContentService.agregarContenidoListaPublica(agregarDTO);
    }
    
    /**
     * Agregar contenido a una lista pública (versión simplificada sin idUsuario)
     */
    public String agregarContenidoListaPublica(AgregarContenidoPublicoDTO agregarDTO) {
        return publicListContentService.agregarContenidoListaPublica(agregarDTO);
    }

    /**
     * Eliminar lista pública
     */
    public String eliminarListaPublica(String idLista, String idUsuario) {
        return publicListUpdateService.eliminarListaPublica(idLista, idUsuario);
    }

    /**
     * Eliminar contenido de una lista pública
     */
    public String eliminarContenidoListaPublica(EliminarContenidoPublicoDTO eliminarDTO) {
        return publicListContentService.eliminarContenidoListaPublica(eliminarDTO);
    }

    /**
     * Obtener todas las listas públicas
     */
    public List<ListaResponseDTO> obtenerListasPublicas() {
        return publicListQueryService.obtenerListasPublicas();
    }

    /**
     * Obtener una lista pública por ID con contenidos completos
     * @param idLista ID de la lista
     * @return DTO con información de la lista y contenidos completos
     */
    public ListaContenidosResponseDTO obtenerListaPublicaConContenidos(String idLista) {
        return publicListQueryService.obtenerListaPublicaConContenidos(idLista);
    }

    /**
     * Obtener todas las listas públicas con contenidos completos
     * @return Lista de todas las listas públicas con sus contenidos
     */
    public List<ListaResponseDTO> obtenerTodasListasPublicasConContenidos() {
        return publicListQueryService.obtenerTodasListasPublicasConContenidos();
    }

    /**
     * Actualizar campos principales de una lista pública (nombre, descripción, visibilidad)
     * @param updateDTO DTO con los nuevos valores
     * @param idUsuario ID del usuario (debe ser el propietario)
     * @return Mensaje de confirmación
     */
    public String actualizarCamposListaPublica(ListaUpdateFieldsPublicasDTO updateDTO) {
        return publicListUpdateService.actualizarCamposListaPublica(updateDTO);
    }
}