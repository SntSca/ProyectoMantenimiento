package com.esimedia.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponseDTO {
    
    @NotBlank(message = "El token no puede estar vacío")
    private String token;
    
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @NotBlank(message = "El alias no puede estar vacío")
    private String alias;
    
    @NotBlank(message = "El rol no puede estar vacío")
    private String rol;
}
