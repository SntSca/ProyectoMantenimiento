package com.esimedia.features.content.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "valoracionesContenido")
public class ValoracionContenido {
    
    @Id
    private String idValoracion;
    
    @Field("idContenido")
    @NotBlank(message = "El ID del contenido no puede estar vacío")
    @Indexed
    private String idContenido;
    
    @Field("idUsuario")
    @NotBlank(message = "El ID del usuario no puede estar vacío")
    @Indexed
    private String idUsuario;
    
    @Field("valoracion")
    @Min(value = 1, message = "La valoración debe ser al menos 1")
    @Max(value = 5, message = "La valoración no puede ser mayor a 5")
    private int valoracion;
    
    @Field("fechaValoracion")
    @NotNull(message = "La fecha de valoración no puede ser nula")
    private Date fechaValoracion;
    
    // Constructor
    public ValoracionContenido(String idContenido, String idUsuario, int valoracion) {
        this.idContenido = idContenido;
        this.idUsuario = idUsuario;
        this.valoracion = valoracion;
        this.fechaValoracion = new Date();
    }
}