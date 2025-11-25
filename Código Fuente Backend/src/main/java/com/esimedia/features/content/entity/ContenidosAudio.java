package com.esimedia.features.content.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Document(collection = "contenidosAudio")
public class ContenidosAudio extends Contenido {
    
    @Field("fichero")
    @NotNull(message = "El fichero no puede ser nulo")
    @Size(min = 1, message = "El fichero no puede estar vacío")
    private byte[] fichero;

    @Field("ficheroExtension")
    @NotBlank(message = "La extensión del fichero no puede estar vacía")
    @Pattern(regexp = "^\\.(mp3|wav|flac|aac|ogg|m4a)$", message = "La extensión debe ser un formato de audio válido (.mp3, .wav, .flac, .aac, .ogg, .m4a)")
    private String ficheroExtension;

}