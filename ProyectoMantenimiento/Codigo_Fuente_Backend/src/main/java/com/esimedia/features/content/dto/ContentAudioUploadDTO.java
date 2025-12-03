package com.esimedia.features.content.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentAudioUploadDTO extends ContentUploadDTO {
    
    @NotBlank(message = "El fichero no puede estar vacío")
    private String fichero;
    
    @NotBlank(message = "El MIME type del fichero no puede estar vacío")
    @Pattern(regexp = "^audio/(mp3|mpeg|wav|flac|aac|ogg|x-m4a|webm)$", message = "El MIME type debe ser un formato de audio válido (audio/mpeg, audio/wav, audio/flac, audio/aac, audio/ogg, audio/x-m4a, audio/webm)")
    private String ficheroExtension;
    
    @Min(value = 0, message = "La restricción de edad no puede ser negativa")
    @Max(value = 18, message = "La restricción de edad no puede ser mayor a 18")
    private Integer restriccionEdad;
}