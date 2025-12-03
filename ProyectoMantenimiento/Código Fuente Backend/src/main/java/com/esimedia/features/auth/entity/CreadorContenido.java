package com.esimedia.features.auth.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.esimedia.features.auth.enums.TipoContenido;

import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Document(collection = "creadoresContenido")
public class CreadorContenido extends Usuario {

    @Indexed(unique = true)
    @Field("aliasCreador")
    @NotBlank(message = "El alias de creador no puede estar vacío")
    @Size(min = 3, max = 20, message = "El alias de creador debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z\\d_-]{2,}$", message = "El alias de creador debe comenzar con una letra")
    private String aliasCreador;

    @Field("descripcion")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @Field("bloqueado")
    private boolean bloqueado;

    @Field("especialidad")
    @NotBlank(message = "La especialidad no puede estar vacía")
    @Size(min = 2, max = 100, message = "La especialidad debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s&.-]{2,}$", message = "La especialidad contiene caracteres inválidos")
    private String especialidad;

    @Field("tipoContenido")
    @NotNull(message = "El tipo de contenido no puede ser nulo")
    private TipoContenido tipoContenido;

    @Field("validado")
    private boolean validado;

}