package com.esimedia.features.auth.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Document(collection = "administradores")
public class Administrador extends Usuario {
    
    @Field("departamento")
    @NotBlank(message = "El departamento no puede estar vacío")
    @Size(min = 2, max = 100, message = "El departamento debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s&.-]{2,}$", message = "El departamento contiene caracteres inválidos")
    private String departamento;
}