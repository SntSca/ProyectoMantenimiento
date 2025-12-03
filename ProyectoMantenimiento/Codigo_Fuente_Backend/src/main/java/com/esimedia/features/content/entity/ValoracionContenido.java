package com.esimedia.features.content.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Min(value = 1, message = "La valoración debe ser al menos 0")
    @Max(value = 5, message = "La valoración no puede ser mayor a 5")
    private Double valoracion;
    
    @Field("fechaValoracion")
    @NotNull(message = "La fecha de valoración no puede ser nula")
    private Date fechaValoracion;
    
    // Constructor
    public ValoracionContenido(String idContenido, String idUsuario, Double valoracion) {
        this.idContenido = idContenido;
        this.idUsuario = idUsuario;
        this.valoracion = valoracion;
        this.fechaValoracion = new Date();
    }
}