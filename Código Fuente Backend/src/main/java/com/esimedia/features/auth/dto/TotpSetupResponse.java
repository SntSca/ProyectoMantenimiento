package com.esimedia.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TotpSetupResponse {

    @NotBlank(message = "La URL del código QR no puede estar vacía")
    @Pattern(regexp = "^data:image/.*$", message = "La URL del código QR debe ser una imagen en formato data URI")
    private String qrCodeUrl;
    
    @NotBlank(message = "La clave secreta no puede estar vacía")
    @Size(min = 16, max = 32, message = "La clave secreta debe tener entre 16 y 32 caracteres")
    private String secretKey;
}