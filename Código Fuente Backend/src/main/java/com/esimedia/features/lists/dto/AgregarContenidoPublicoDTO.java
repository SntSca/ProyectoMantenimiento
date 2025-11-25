package com.esimedia.features.lists.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgregarContenidoPublicoDTO {
    
    @NotBlank(message = "El ID de la lista no puede estar vacío")
    private String idLista;
    
    @NotEmpty(message = "La lista de IDs de contenido no puede estar vacía")
    private List<@NotBlank(message = "Los IDs de contenido no pueden estar vacíos") String> idsContenido;
}