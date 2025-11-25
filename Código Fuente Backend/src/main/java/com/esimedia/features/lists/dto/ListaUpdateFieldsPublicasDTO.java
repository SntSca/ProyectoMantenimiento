package com.esimedia.features.lists.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * DTO para actualizar campos principales de una lista de reproducción pública
 * (nombre, descripción, visibilidad) - NO incluye modificación de contenidos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class ListaUpdateFieldsPublicasDTO extends ListaUpdateFieldsDTO {

    @NotNull(message = "La visibilidad debe estar definida")
    private Boolean visibilidad;
}