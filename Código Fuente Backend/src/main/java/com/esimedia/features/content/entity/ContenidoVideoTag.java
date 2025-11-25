package com.esimedia.features.content.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "contenidoVideoTags")
@Data
@NoArgsConstructor
public class ContenidoVideoTag {
    @Id
    private String id;

    @Field("idContenido")
    @NotBlank(message = "El ID del contenido no puede estar vacío")
    private String idContenido;

    @Field("idTag")
    @NotBlank(message = "El ID del tag no puede estar vacío")
    private String idTag; 
}