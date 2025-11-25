package com.esimedia.features.auth.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.esimedia.features.auth.enums.TipoVerificacion;

import java.time.LocalDateTime;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "codigos_verificacion")
public class CodigoVerificacion {

    @Id
    private String id;
    
    @NotBlank(message = "El ID del usuario no puede estar vacío")
    private String userId;
    
    @NotBlank(message = "El código no puede estar vacío")
    @Pattern(regexp = "^\\d{6}$", message = "El código debe tener exactamente 6 dígitos")
    private String codigo;
    
    @NotNull(message = "El tipo de verificación no puede ser nulo")
    private TipoVerificacion tipo;
    
    @NotNull(message = "La fecha de creación no puede ser nula")
    @PastOrPresent(message = "La fecha de creación no puede ser futura")
    private LocalDateTime fechaCreacion;
    
    @NotNull(message = "La fecha de expiración no puede ser nula")
    @Future(message = "La fecha de expiración debe ser futura")
    private LocalDateTime fechaExpiracion;
    
    private boolean usado;
    
    public CodigoVerificacion(String userId, String codigo, TipoVerificacion tipo, int minutosExpiracion) {
        this.userId = userId;
        this.codigo = codigo;
        this.tipo = tipo;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = this.fechaCreacion.plusMinutes(minutosExpiracion);
        this.usado = false;
    }

    /**
     * Verifica si el código ha expirado
     */
    public boolean hasExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
    
    /**
     * Verifica si el código es válido (no usado y no expirado)
     */
    public boolean isValido() {
        return !usado && !hasExpirado();
    }
}
