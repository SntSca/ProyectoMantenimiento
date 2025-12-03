package com.esimedia.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.esimedia.features.auth.enums.TipoContenido;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreadorContenidoDTO extends UsuarioDTO {
    
    @NotBlank(message = "El alias de creador no puede estar vacío")
    @Size(min = 3, max = 20, message = "El alias de creador debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z\\d_-]{2,}$", message = "El alias de creador debe comenzar con una letra y contener solo letras, números, guiones y guiones bajos")
    private String aliasCreador;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de contenido no puede ser nulo")
    private TipoContenido tipoContenido;
    
    @NotBlank(message = "La especialidad no puede estar vacía")
    @Size(min = 2, max = 100, message = "La especialidad debe tener entre 2 y 100 caracteres")
    @Pattern(
        regexp = "^(Música|Deporte|Acción|Tecnología|Cocina|Viajes|Salud y Bienestar|Arte y Diseño|Negocios y Emprendimiento|Educación y Aprendizaje|Deporte y Fitness|Moda y Belleza|Cine, Música y Entretenimiento)$",
        flags = {Pattern.Flag.CANON_EQ},
        message = "La especialidad debe ser una de: Música, Deporte, Acción, Tecnología, Cocina, Viajes, Salud y Bienestar, Arte y Diseño, Negocios y Emprendimiento, Educación y Aprendizaje, Deporte y Fitness, Moda y Belleza, Cine, Música y Entretenimiento"
    )
    @Pattern(regexp = "^[\\p{L}\\s&.-]{2,}$", message = "La especialidad contiene caracteres inválidos")
    private String especialidad;

}