package com.esimedia.features.auth.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.esimedia.features.auth.enums.EstadoSesion;

import jakarta.validation.constraints.*;
import lombok.Data;

@Document(collection = "sesiones")
@Data
public class Sesion {
    @Id
    private String idSesion;

    @Field("idUsuario")
    @Indexed
    @NotBlank(message = "El ID del usuario no puede estar vacío")
    private String idUsuario;

    @Field("ipCliente")
    @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$", message = "La IP del cliente debe tener un formato válido")
    private String ipCliente;

    @Field("jwtTokenId")
    @Indexed(unique = true)
    @NotBlank(message = "El ID del JWT no puede estar vacío")
    private String jwtTokenId;

    @Field("fechaInicio")
    @NotNull(message = "La fecha de inicio no puede ser nula")
    @PastOrPresent(message = "La fecha de inicio no puede ser futura")
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Field("fechaUltimaActividad")
    @NotNull(message = "La fecha de última actividad no puede ser nula")
    @PastOrPresent(message = "La fecha de última actividad no puede ser futura")
    private LocalDateTime fechaUltimaActividad = LocalDateTime.now();

    @Field("estado")
    @NotNull(message = "El estado de la sesión no puede ser nulo")
    private EstadoSesion estado;
    
    @Field("resetToken")
    private String resetToken;
    
    @Field("resetTokenExpiry")
    @Future(message = "La fecha de expiración del token de reset debe ser futura")
    private LocalDateTime resetTokenExpiry;
    


    // Constructores
    public Sesion() {
        this.fechaInicio = LocalDateTime.now();
        this.fechaUltimaActividad = LocalDateTime.now();
        this.estado = EstadoSesion.ACTIVA;
    }

    // Métodos de utilidad
    public void actualizarUltimaActividad() {
        this.fechaUltimaActividad = LocalDateTime.now();
    }

    public void expirarSesion() {
        this.estado = EstadoSesion.EXPIRADA;
    }

    public void bloquearSesion() {
        this.estado = EstadoSesion.BLOQUEADA;
    }

    public boolean isActiva() {
        return this.estado == EstadoSesion.ACTIVA;
    }
}