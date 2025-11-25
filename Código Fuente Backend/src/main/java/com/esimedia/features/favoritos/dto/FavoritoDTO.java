package com.esimedia.features.favoritos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoritoDTO {

    @NotBlank(message = "El id del contenido no puede estar vacío")
    private String idContenido;
}
