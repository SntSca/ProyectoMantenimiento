package com.esimedia.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.enums.TipoContenido;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponseDTO {
    private String idUsuario;
    private String nombre;
    private String apellidos;
    private String email;
    private String alias;
    private String fotoPerfil;
    private Date fechaRegistro;
    private boolean twoFactorEnabled;
    private boolean thirdFactorEnabled;
    private Rol rol;

    // Campos específicos de UsuarioNormal
    private Boolean flagVIP;
    private Date fechaNacimiento;
    private Boolean bloqueadoUsuarioNormal;
    private Boolean confirmado;

    // Campos específicos de CreadorContenido
    private String aliasCreador;
    private String descripcion;
    private Boolean bloqueadoCreador;
    private String especialidad;
    private TipoContenido tipoContenido;
    private Boolean validado;

    // Campos específicos de Administrador
    private String departamento;
}