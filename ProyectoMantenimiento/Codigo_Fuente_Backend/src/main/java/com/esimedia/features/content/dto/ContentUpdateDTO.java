package com.esimedia.features.content.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentUpdateDTO {

    @Size(min = 1, max = 100, message = "El título debe tener entre 1 y 100 caracteres")
    private String titulo;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotEmpty(message = "Debe incluir al menos un tag")
    @Size(max = 25, message = "No puede tener más de 25 tags")
    private List<@NotBlank(message = "Los tags no pueden estar vacíos") String> tags;

    private Boolean esVIP;

    @Min(value = 3, message = "La restricción de edad mínima es 3")
    @Max(value = 18, message = "La restricción de edad máxima es 18")
    private Integer restriccionEdad;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha de expiración debe tener formato yyyy-MM-dd")
    private String fechaExpiracion;

    private Boolean visibilidad;
}