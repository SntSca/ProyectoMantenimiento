package com.esimedia.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.esimedia.features.auth.enums.Rol;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioDTO {
    
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s'-]{2,}$", message = "El nombre contiene caracteres inválidos")
    private String nombre;
    
    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s'-]{2,}$", message = "Los apellidos contienen caracteres inválidos")
    private String apellidos;
    
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    @Size(max = 254, message = "El email no puede exceder 254 caracteres")
    private String email;
    
    @Size(min = 3, max = 12, message = "El alias debe tener entre 3 y 12 caracteres")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z\\d_-]{2,}$", message = "El alias debe comenzar con una letra y tener al menos 3 caracteres")
    private String alias;
    
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$", 
             message = "La contraseña debe incluir mayúsculas, minúsculas, dígitos y caracteres especiales")
    private String password;
    
    @NotNull(message = "El rol no puede ser nulo")
    private Rol rol;
    
    private String fotoPerfil;
    private String formatoFotoPerfil;

}