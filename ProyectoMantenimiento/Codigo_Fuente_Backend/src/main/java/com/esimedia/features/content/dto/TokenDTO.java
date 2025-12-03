package com.esimedia.features.content.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.esimedia.features.auth.enums.EstadoToken;
import com.esimedia.features.auth.enums.TipoToken;

/**
 * DTO para la transferencia de datos de Token
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenDTO {
    
    private String id;
    
    @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$", 
             message = "La IP del cliente debe tener un formato válido")
    private String ipCliente;
    
    @NotBlank(message = "El email del usuario no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    private String emailUsuario;
    
    @NotNull(message = "La fecha de inicio no puede ser nula")
    private LocalDateTime fechaInicio;
    
    private LocalDateTime fechaUltimaActividad;
    
    @NotNull(message = "El estado del token no puede ser nulo")
    private EstadoToken estado;
    
    @NotBlank(message = "El ID del JWT no puede estar vacío")
    private String jwtTokenId;
    
    @NotNull(message = "El tipo de token no puede ser nulo")
    private TipoToken tipoToken;
    
    private LocalDateTime fechaCreacionToken;

    // Constructor privado para uso del Builder
    private TokenDTO(Builder builder) {
        this.id = builder.id;
        this.ipCliente = builder.ipCliente;
        this.emailUsuario = builder.emailUsuario;
        this.fechaInicio = builder.fechaInicio;
        this.fechaUltimaActividad = builder.fechaUltimaActividad;
        this.estado = builder.estado;
        this.jwtTokenId = builder.jwtTokenId;
        this.tipoToken = builder.tipoToken;
        this.fechaCreacionToken = builder.fechaCreacionToken;
    }



    // Método estático para iniciar el builder
    public static Builder builder() {
        return new Builder();
    }

    // Getters, setters y toString generados automáticamente por @Data

    /**
     * Clase Builder para TokenDTO
     */
    public static class Builder {
        private String id;
        private String ipCliente;
        private String emailUsuario;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaUltimaActividad;
        private EstadoToken estado;
        private String jwtTokenId;
        private TipoToken tipoToken;
        private LocalDateTime fechaCreacionToken;

        // Constructor privado
        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder ipCliente(String ipCliente) {
            this.ipCliente = ipCliente;
            return this;
        }
        
        public Builder emailUsuario(String emailUsuario) {
            this.emailUsuario = emailUsuario;
            return this;
        }

        public Builder fechaInicio(LocalDateTime fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public Builder fechaUltimaActividad(LocalDateTime fechaUltimaActividad) {
            this.fechaUltimaActividad = fechaUltimaActividad;
            return this;
        }

        public Builder estado(EstadoToken estado) {
            this.estado = estado;
            return this;
        }

        public Builder jwtTokenId(String jwtTokenId) {
            this.jwtTokenId = jwtTokenId;
            return this;
        }

        public Builder tipoToken(TipoToken tipoToken) {
            this.tipoToken = tipoToken;
            return this;
        }

        // Métodos de conveniencia para crear tokens comunes
        public Builder tokenRegistro(String jwtId, String ip, String email) {
            return this
                    .tipoToken(TipoToken.REGISTRO)
                    .estado(EstadoToken.SIN_CONFIRMAR)
                    .jwtTokenId(jwtId)
                    .ipCliente(ip)
                    .emailUsuario(email)
                    .fechaInicio(LocalDateTime.now())
                    .fechaUltimaActividad(LocalDateTime.now());
        }

        public Builder tokenRecuperacion(String jwtId, String ip, String email) {
            return this
                    .tipoToken(TipoToken.RECUPERACION_PASSWORD)
                    .estado(EstadoToken.SIN_CONFIRMAR)
                    .jwtTokenId(jwtId)
                    .ipCliente(ip)
                    .emailUsuario(email)
                    .fechaInicio(LocalDateTime.now())
                    .fechaUltimaActividad(LocalDateTime.now());
        }

        public Builder tokenAcceso(String jwtId, String ip, String email) {
            return this
                    .tipoToken(TipoToken.ACCESO)
                    .estado(EstadoToken.SIN_CONFIRMAR)
                    .jwtTokenId(jwtId)
                    .ipCliente(ip)
                    .emailUsuario(email)
                    .fechaInicio(LocalDateTime.now())
                    .fechaUltimaActividad(LocalDateTime.now());
        }

        // Método para construir el TokenDTO
        public TokenDTO build() {
            return new TokenDTO(this);
        }
    }
}