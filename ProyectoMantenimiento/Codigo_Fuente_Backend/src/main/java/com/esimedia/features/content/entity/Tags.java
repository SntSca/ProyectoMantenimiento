package com.esimedia.features.content.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tags")
public class Tags {
    
    @Id
    private String idTag;
    
    @Indexed(unique = true)
    @Field("nombre")
    @NotBlank(message = "El nombre del tag no puede estar vacío")
    @Size(min = 2, max = 30, message = "El nombre del tag debe tener entre 2 y 30 caracteres")
    @Pattern(regexp = "^[\\p{L}\\d\\s.-]{2,}$", message = "El nombre del tag contiene caracteres inválidos")
    private String nombre;
    
    // Constructor personalizado para compatibilidad
    public Tags(String nombre) {
        this.nombre = nombre;
    }

}