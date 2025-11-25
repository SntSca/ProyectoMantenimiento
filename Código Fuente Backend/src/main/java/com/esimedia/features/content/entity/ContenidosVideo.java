package com.esimedia.features.content.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.esimedia.features.content.enums.Resolucion;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Document(collection = "contenidosVideo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
public class ContenidosVideo extends Contenido {
    
    @Field("urlArchivo")
    @NotBlank(message = "La URL del archivo no puede estar vacía")
    @Pattern(regexp = "^(http|https|ftp)://.*$", message = "La URL debe tener un formato válido")
    private String urlArchivo;

    @Field("resolucion")
    @NotNull(message = "La resolución no puede ser nula")
    private Resolucion resolucion;

    /**
     * Valida las reglas de negocio después de la construcción del objeto.
     * La resolución 4K solo está disponible para contenido VIP.
     */
    public void validar() {
        if (this.resolucion == Resolucion.UHD_2160 && !this.isEsVIP()) {
            throw new IllegalArgumentException("La resolución 4K solo está disponible para contenido VIP");
        }
    }

}