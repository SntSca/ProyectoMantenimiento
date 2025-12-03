package com.esimedia.features.auth.entity;

import java.util.Date;

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
@Document(collection = "usuariosNormales")
public class UsuarioNormal extends Usuario {
    
    @Field("flagVIP")
    private boolean flagVIP;
    
    @Field("fechaNacimiento")
    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    private Date fechaNacimiento;
    
    @Field("bloqueado")
    private boolean bloqueado;
    
    @Field("confirmado")
    private boolean confirmado;
}