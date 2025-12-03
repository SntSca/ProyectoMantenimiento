package com.esimedia.features.auth.entity;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;

import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tokens")
public class Token {

    @Id
    private String id;

    @Field("token")
    @Indexed(unique = true, sparse = true, name = "token_sparse_unique")
    @NotBlank(message = "El token no puede estar vacío")
    private String tokenCreado;
    
    @Field("ipCliente")
    @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$", message = "La IP del cliente debe tener un formato válido")
    private String ipCliente;

    @Field("fechaInicio")
    @NotNull(message = "La fecha de inicio no puede ser nula")
    private LocalDateTime fechaInicio;

    @Field("fechaUltimaActividad")
    private LocalDateTime fechaUltimaActividad;

    @Field("estado")
    @NotNull(message = "El estado del token no puede ser nulo")
    private EstadoToken estado;

    @Field("jwtTokenId")
    @NotBlank(message = "El ID del JWT no puede estar vacío")
    private String jwtTokenId;

    @Field("tipoToken")
    @NotNull(message = "El tipo de token no puede ser nulo")
    private TipoToken tipoToken;
    
    @Field("usuarioEmail")
    @NotBlank(message = "El email del usuario no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    private String usuarioEmail;

    // Método para obtener el usuario asociado (basado en el token que parece ser un email)
    // Nota: Este método debería ser implementado en el servicio, no en la entidad
    // Para mantener la separación de responsabilidades
    public UsuarioNormal getUser() {
        // Este método retorna null por diseño.
        // La búsqueda del usuario debe hacerse en el servicio usando el token
        return null;
    }

    public boolean isValido() {
        return estado != EstadoToken.EXPIRADA && estado != EstadoToken.REVOCADA && estado != EstadoToken.UTILIZADA;
    }

    public boolean isExpirado() {
        return estado == EstadoToken.EXPIRADA;
    }
}