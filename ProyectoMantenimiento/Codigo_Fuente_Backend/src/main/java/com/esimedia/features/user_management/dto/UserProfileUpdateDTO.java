package com.esimedia.features.user_management.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.util.Date;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileUpdateDTO {
    
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s'-]{2,}$", message = "El nombre contiene caracteres inválidos")
    private String nombre;
    
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s'-]{2,}$", message = "Los apellidos contienen caracteres inválidos")
    private String apellidos;
    
    @Size(min = 3, max = 12, message = "El alias debe tener entre 3 y 12 caracteres")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z\\d_-]{2,}$", message = "El alias debe comenzar con una letra y tener al menos 3 caracteres")
    private String alias;
    
    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    private Date fechaNacimiento;
    
    private Boolean flagVIP;
    
    private String fotoPerfil;
    
    @Pattern(regexp = "^image/(jpeg|jpg|png|gif|webp)$", message = "El formato de imagen no es válido")
    private String formatoFotoPerfil;
}