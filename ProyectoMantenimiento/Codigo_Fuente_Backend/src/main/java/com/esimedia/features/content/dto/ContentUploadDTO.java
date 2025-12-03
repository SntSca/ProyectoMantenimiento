package com.esimedia.features.content.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentUploadDTO {
    
    private String id;
    
    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    private String titulo;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    private List<String> tags;
    
    @Positive(message = "La duración debe ser un número positivo")
    private Integer duracion;
    
    private String fechaSubida;
    private String fechaExpiracion;
    
    @NotNull(message = "Debe especificar si el contenido es VIP o no")
    private Boolean esVIP;
    
    private String especialidad;
    
    private Boolean visibilidad;
    
    private Double valoracionMedia;
    
    private Integer visualizaciones;
    
    private Double valoracionUsuario;
    
    private String miniatura;
    private String formatoMiniatura;
}