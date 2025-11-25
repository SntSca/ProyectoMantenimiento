package com.esimedia.features.lists.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO para la respuesta de listas privadas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListaPrivadaResponseDTO {

    private String idLista;
    private String nombre;
    private String descripcion;
    private String idCreadorUsuario;
    private Boolean visibilidad;
    
    /**
     * Lista de contenidos completos (videos y audios) que están en la lista con tags
     */
    private List<ContenidoListaResponseDTO> contenidos;
}