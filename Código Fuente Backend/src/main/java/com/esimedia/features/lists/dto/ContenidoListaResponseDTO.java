package com.esimedia.features.lists.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para representar un contenido dentro de una lista
 * Incluye solo la información necesaria para mostrar en una lista
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenidoListaResponseDTO {
    
    private String id;
    private String titulo;
    private String descripcion;
    private int duracion;
    private String especialidad;
    private boolean visibilidad;
    private List<String> tags;
    
    // Campos adicionales que se incluyen en getAllContent
    private boolean esVIP;
    private String miniatura;
    private String formatoMiniatura;
    private String fechaSubida;
    private String fechaExpiracion;
    private Double valoracionMedia;
    private Integer restriccionEdad;
    
    // Para videos
    private String urlArchivo;
    private String resolucion;
    
    // Para audios
    private String fichero;
    private String ficheroExtension;
}
