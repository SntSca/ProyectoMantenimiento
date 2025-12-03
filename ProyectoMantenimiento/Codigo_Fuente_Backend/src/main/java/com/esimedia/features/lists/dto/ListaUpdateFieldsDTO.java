package com.esimedia.features.lists.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO base para actualizar campos principales de una lista de reproducción
 * (idLista, nombre, descripción) - NO incluye modificación de contenidos ni visibilidad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListaUpdateFieldsDTO {

    @NotBlank(message = "El ID de la lista no puede estar vacío")
    private String idLista;

    @NotBlank(message = "El nombre de la lista no puede estar vacío")
    @Size(min = 1, max = 100, message = "El nombre de la lista debe tener entre 1 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
}