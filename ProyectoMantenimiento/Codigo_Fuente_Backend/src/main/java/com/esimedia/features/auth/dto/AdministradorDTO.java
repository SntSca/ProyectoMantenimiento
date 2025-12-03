package com.esimedia.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdministradorDTO extends UsuarioDTO {
    
    @NotBlank(message = "El departamento no puede estar vacío")
    @Size(min = 2, max = 100, message = "El departamento debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s&.-]{2,}$", message = "El departamento contiene caracteres inválidos")
    private String departamento;
}