package com.esimedia.features.lists.entity;

import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ListaBase {
    
    @Id
    private String idLista;
    
    @NotBlank(message = "El nombre de la lista no puede estar vacío")
    @Size(max = 100, message = "El nombre de la lista no puede exceder 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotBlank(message = "El ID del creador no puede estar vacío")
    private String idCreadorUsuario;
    
    @NotNull(message = "La visibilidad debe estar definida")
    private Boolean visibilidad;
}