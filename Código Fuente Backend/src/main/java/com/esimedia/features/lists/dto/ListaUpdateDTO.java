package com.esimedia.features.lists.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListaUpdateDTO {

    @NotBlank(message = "El ID de la lista no puede estar vacío")
    private String idLista;

    @NotBlank(message = "El nombre de la lista no puede estar vacío")
    @Size(max = 100, message = "El nombre de la lista no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "La visibilidad debe estar definida")
    private Boolean visibilidad;
}