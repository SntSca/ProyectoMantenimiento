package com.esimedia.features.content.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentVideoUploadDTO extends ContentUploadDTO {
    
    @NotBlank(message = "La URL del archivo no puede estar vacía")
    @Pattern(regexp = "^(http|https|ftp)://.*$", message = "La URL debe tener un formato válido")
    private String urlArchivo;
    
    private String resolucion;
    
    @Min(value = 0, message = "La restricción de edad no puede ser negativa")
    @Max(value = 18, message = "La restricción de edad no puede ser mayor a 18")
    private Integer restriccionEdad;
}