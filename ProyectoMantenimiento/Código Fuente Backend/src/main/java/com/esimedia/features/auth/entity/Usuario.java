package com.esimedia.features.auth.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.esimedia.features.auth.enums.Rol;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SuperBuilder
@Document(collection = "usuarios")
public class Usuario {
    
    @Id
    private String idUsuario;

    @Field("nombre")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s'-]{2,}$", message = "El nombre contiene caracteres inválidos")
    private String nombre;

    @Field("apellidos")
    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s'-]{2,}$", message = "Los apellidos contienen caracteres inválidos")
    private String apellidos;

    @Indexed(unique = true)
    @Field("email")
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    @Size(max = 254, message = "El email no puede exceder 254 caracteres")
    private String email;

    @Field("alias")
    @Size(min = 3, max = 12, message = "El alias debe tener entre 3 y 12 caracteres")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z\\d_-]{2,}$", message = "El alias debe comenzar con una letra")
    private String alias;

    @Field("password")
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
    private String password;

    @Field("passwordHistory")
    @Builder.Default
    private List<String> passwordHistory = new ArrayList<>();

    @Field("passwordChangedAt")
    private java.time.LocalDateTime passwordChangedAt;

    @Field("fotoPerfil")
    protected String fotoPerfil;

    @Field("fechaRegistro")
    @NotNull(message = "La fecha de registro no puede ser nula")
    @PastOrPresent(message = "La fecha de registro no puede ser futura")
    private Date fechaRegistro;

    @Field("credentialsVersion")
    @Min(value = 1, message = "La versión de credenciales debe ser al menos 1")
    private long credentialsVersion;

    @Field("twoFactorEnabled")
    @Builder.Default
    private boolean twoFactorEnabled = false;
    
    @Field("thirdFactorEnabled")
    @Builder.Default
    private boolean thirdFactorEnabled = false;
    
    @Field("totpSecret")
    @Size(max = 50, message = "El secreto TOTP no puede exceder 50 caracteres")
    private String totpSecret;
    
    @Field("backupCodes")
    @Size(max = 10, message = "No puede tener más de 10 códigos de respaldo")
    private List<String> backupCodes;
    
    @NotNull(message = "El rol no puede ser nulo")
    private Rol rol;

    @Field("gustosTags")
    @Builder.Default
    private java.util.List<String> gustosTags = new ArrayList<>();



    // Constructor para uso en createUser (sin validaciones - ahora se valida en ValidationService)
    public Usuario(String nombre, String apellidos, String email, String alias, String password, Rol rol) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.alias = alias;
        this.password = password;
        this.passwordHistory = new ArrayList<>();
        this.passwordChangedAt = null;
        this.rol = rol;
        this.fechaRegistro = new Date();
        this.credentialsVersion = 1L;
        this.gustosTags = new ArrayList<>();

    }
}
