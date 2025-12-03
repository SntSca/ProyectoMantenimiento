package com.esimedia.features.content.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "contenidoAudioTags")
@Data
@NoArgsConstructor
public class ContenidoAudioTag {
    @Id
    private String id;

    // FK a ContenidoAudio
    @Field("idContenido")
    @NotBlank(message = "El ID del contenido no puede estar vacío")
    private String idContenido; 

    // FK a Tag
    @Field("idTag")
    @NotBlank(message = "El ID del tag no puede estar vacío")
    private String idTag;

    public ContenidoAudioTag(String idContenido, String idTag) {
        this.idContenido = idContenido;
        this.idTag = idTag;
    }
}