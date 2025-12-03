package com.esimedia.features.auth.entity;

import com.esimedia.features.auth.dto.AdministradorDTO;
import com.esimedia.features.auth.dto.CreadorContenidoDTO;
import com.esimedia.features.auth.dto.UsuarioDTO;
import com.esimedia.features.auth.dto.UsuarioNormalDTO;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.services.ValidationService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UsuarioFactory {

    @Value("${app.security.pepper}")
    private String pepper;

    private static final Logger logger = LoggerFactory.getLogger(UsuarioFactory.class);

    private UsuarioFactory() {}
    /**
     * Crea una instancia de Usuario a partir de un UsuarioDTO.
     * 
     * @param dto el DTO de usuario
     * @param validationService el servicio de validación para validar los datos
     * @param passwordService el servicio de codificación de contraseñas
     * @return una instancia de Usuario
     * @throws IllegalArgumentException si el tipo de DTO no es soportado
     * @throws NullPointerException si el dto es null
     * @throws org.springframework.web.server.ResponseStatusException si la validación falla
     */
    public Usuario crearUsuario(UsuarioDTO dto, ValidationService validationService) throws IllegalArgumentException, NullPointerException {
        
        // Validaciones básicas ahora van en DTO con Bean Validation (@ValidEmailDomain, etc.)
        // La validación del dominio de email ya se hace automáticamente en el DTO
        
        // Procesar fotoPerfil si está presente
        String fotoUri = null;
        if (dto.getFotoPerfil() != null && !dto.getFotoPerfil().trim().isEmpty() && dto.getFotoPerfil().startsWith("data:")) {
                // Ya viene como data URI completa
                fotoUri = dto.getFotoPerfil();
        }
        
        
        if (dto instanceof UsuarioNormalDTO normalDTO) {

                // Preservar el valor enviado por el cliente si no es null; por defecto false
                boolean flagVIP = normalDTO.getFlagVIP() != null && normalDTO.getFlagVIP();
                logger.debug("[UsuarioFactory] flagVIP recibido en DTO: {} -> usando: {}", normalDTO.getFlagVIP(), flagVIP);
            
            return UsuarioNormal.builder()
                .nombre(normalDTO.getNombre())
                .apellidos(normalDTO.getApellidos())
                .email(normalDTO.getEmail())
                .alias(normalDTO.getAlias())
                .password(encodePassword(normalDTO.getPassword()))
                .fotoPerfil(fotoUri)
                .fechaNacimiento(normalDTO.getFechaNacimiento())
                .flagVIP(flagVIP)
                .rol(Rol.NORMAL)
                .build();
        } 
        else if (dto instanceof CreadorContenidoDTO creadorDTO) {

            return CreadorContenido.builder()
                .nombre(creadorDTO.getNombre())
                .apellidos(creadorDTO.getApellidos())
                .email(creadorDTO.getEmail())
                .alias(creadorDTO.getAlias())
                .password(encodePassword(creadorDTO.getPassword()))
                .fotoPerfil(fotoUri)
                .aliasCreador(creadorDTO.getAliasCreador())
                .descripcion(creadorDTO.getDescripcion())
                .tipoContenido(creadorDTO.getTipoContenido())
                .especialidad(creadorDTO.getEspecialidad())
                .rol(Rol.CREADOR)
                .build();
        } 
        else if (dto instanceof AdministradorDTO adminDTO) {
            return Administrador.builder()
                .nombre(adminDTO.getNombre())
                .apellidos(adminDTO.getApellidos())
                .email(adminDTO.getEmail())
                .alias(adminDTO.getAlias())
                .password(encodePassword(adminDTO.getPassword()))
                .fotoPerfil(fotoUri)
                .departamento(adminDTO.getDepartamento())
                .rol(Rol.ADMINISTRADOR)
                .build();
        }
        
        throw new IllegalArgumentException("Tipo de DTO no soportado: " + dto.getClass().getSimpleName());
    }

    /**
     * Codifica la contraseña usando Argon2 con pepper.
     * 
     * @param password la contraseña en texto plano
     * @return la contraseña codificada en Base64
     */
    private String encodePassword(String password) throws SecurityException {
        String passwordWithPepper = password + pepper;
        de.mkammerer.argon2.Argon2 argon2 = de.mkammerer.argon2.Argon2Factory.create(de.mkammerer.argon2.Argon2Factory.Argon2Types.ARGON2id);
        int memory = 65536;
        int iterations = 3;
        int parallelism = 1;
        String hash = argon2.hash(iterations, memory, parallelism, passwordWithPepper.toCharArray());
        argon2.wipeArray(passwordWithPepper.toCharArray());
        return hash;
    }
}