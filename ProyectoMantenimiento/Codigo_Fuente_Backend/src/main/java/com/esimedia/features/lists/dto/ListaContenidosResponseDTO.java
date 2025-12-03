package com.esimedia.features.lists.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO para la respuesta de consulta de lista con contenidos completos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListaContenidosResponseDTO {

    private String idLista;
    private String nombre;
    private String descripcion;
    private String idCreadorUsuario;
    private Boolean visibilidad;
    
    /**
     * Lista de contenidos completos (videos y audios) que están en la lista
     */
    private List<ContenidoListaResponseDTO> contenidos;
}