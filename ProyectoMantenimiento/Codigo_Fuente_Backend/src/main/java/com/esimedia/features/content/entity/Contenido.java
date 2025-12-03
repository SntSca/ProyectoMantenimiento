package com.esimedia.features.content.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.esimedia.features.content.enums.RestriccionEdad;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AccessLevel;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Document(collection = "contenidos")
public class Contenido {
    
    @Id
    private String id;
    
    @Field("titulo")
    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    private String titulo;
    
    @Field("descripcion")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    // Duración en segundos
    @Field("duracion")
    @Positive(message = "La duración debe ser un número positivo")
    @Max(value = 86400, message = "La duración no puede exceder 24 horas (86400 segundos)")
    private int duracion; 
    
    @Field("esVIP")
    private boolean esVIP;
    
    @Field("fechaEstado")
    @PastOrPresent(message = "La fecha de estado no puede ser futura")
    private Date fechaEstado;
    
    @Field("fechaDisponibleHasta")
    @Future(message = "La fecha de disponibilidad debe ser futura")
    private Date fechaDisponibleHasta;
    
    @Field("restriccionEdad")
    private RestriccionEdad restriccionEdad;
    
    @Field("idCreador")
    @NotBlank(message = "El ID del creador no puede estar vacío")
    private String idCreador;
    
    @Field("imagen")
    private byte[] miniatura;

    @Field("formatoImagen")
    @Pattern(regexp = "^image/(jpeg|jpg|png|gif|webp)$", message = "El formato de imagen no es válido")
    private String formatoMiniatura;
    
    @Field("fechaSubida")
    @NotNull(message = "La fecha de subida no puede ser nula")
    @PastOrPresent(message = "La fecha de subida no puede ser futura")
    private Date fechaSubida;

    @Field("especialidad")
    @NotBlank(message = "La especialidad no puede estar vacía")
    @Size(min = 2, max = 100, message = "La especialidad debe tener entre 2 y 100 caracteres")
    private String especialidad;

    @Field("visibilidad")
    @Builder.Default
    private boolean visibilidad = false;

    @Field("valoracionMedia")
    @Builder.Default
    private double valoracionMedia = 0.0;

    @Field("tags")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // Factory method para crear nuevos contenidos con valores por defecto
    @Field("visualizaciones")
    @Builder.Default
    private int visualizaciones = 0;
    public static Contenido createContenido(String titulo, String descripcion, int duracionSecs, String idCreador) {
        Contenido contenido = new Contenido();
        contenido.titulo = titulo;
        contenido.descripcion = descripcion;
        contenido.duracion = duracionSecs;
        contenido.idCreador = idCreador;
        contenido.fechaSubida = new Date();
        contenido.esVIP = false;
        contenido.visibilidad = true;
        contenido.valoracionMedia = 0.0;
        contenido.visualizaciones = 0;
        return contenido;
    }
}