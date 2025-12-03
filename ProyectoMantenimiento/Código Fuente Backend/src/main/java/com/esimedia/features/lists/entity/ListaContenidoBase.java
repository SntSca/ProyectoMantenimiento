package com.esimedia.features.lists.entity;

import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ListaContenidoBase {
    
    @Id
    private String id;
    
    @NotBlank(message = "El ID de la lista no puede estar vacío")
    private String idLista;
    
    @NotBlank(message = "El ID del contenido no puede estar vacío")
    private String idContenido;
}