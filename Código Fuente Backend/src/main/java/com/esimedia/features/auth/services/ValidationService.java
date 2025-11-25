package com.esimedia.features.auth.services;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.apache.tika.Tika;

import com.esimedia.features.content.enums.ArchivosAudioSoportados;
import com.esimedia.features.content.enums.DominiosVideoPermitidos;
import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.auth.enums.TipoContenido;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.dto.ContentUpdateDTO;
import com.esimedia.features.content.dto.ContentUploadDTO;
import com.esimedia.features.content.dto.ContentVideoUploadDTO;
import com.esimedia.shared.util.ContentUtil;


@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    private static final long MAX_AUDIO_SIZE = 1024L * 1024;

    // Solo patrones para validaciones de negocio específicas (básicas van en DTOs)

    // Constantes de longitud máxima
    public static final int MAX_NOMBRE_LENGTH = 50;
    public static final int MAX_APELLIDOS_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 254;
    public static final int MAX_ALIAS_LENGTH = 12;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MIN_EDAD_ANIOS = 4;

    private final CreadorContenidoRepository creadorContenidoRepository;
    private final UsuarioNormalRepository usuarioNormalRepository;
    private final AdminRepository adminRepository;
    private final PasswordDictionaryService passwordDictionaryService;

    public ValidationService(CreadorContenidoRepository creadorContenidoRepository, 
                           UsuarioNormalRepository usuarioNormalRepository,
                           AdminRepository adminRepository,
                           PasswordDictionaryService passwordDictionaryService) {
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.adminRepository = adminRepository;
        this.passwordDictionaryService = passwordDictionaryService;
    }
    
    
    // -------------------- VALIDACIONES DE USUARIOS --------------------


    /**
     * Valida que un contenido solo pueda asociarse a un creador del mismo tipo
     * @param contenido El contenido a validar
     * @param creadorId ID del creador al que se quiere asociar
     * @throws ResponseStatusException si los tipos no son compatibles
     */

    /**
     * Valida que un contenido de video solo pueda asociarse a un creador de video
     * @param tipoContenido Tipo del contenido
     * @param tipoCreador Tipo del creador
     * @throws ResponseStatusException si los tipos no son compatibles
     */
    
    


    // -------------------- VALIDACIONES DE NEGOCIO (básicas eliminadas - ahora van en DTOs) --------------------

    public void validatePasswordStrength(String password, String alias, String email) throws ResponseStatusException {
        if (password.toLowerCase().contains(alias.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña no debe contener el alias de usuario");
        }
        if (email != null && password.toLowerCase().contains(email.split("@")[0].toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña no debe contener el nombre del correo electrónico");
        }
        
        // Verificar si la contraseña está en el diccionario de contraseñas típicas
        if (passwordDictionaryService.isPasswordInDictionary(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña seleccionada es muy común. Por favor, elige una contraseña más fuerte y única");
        }
    }

    /**
     * Valida la fecha de nacimiento
     */
    public void validateFechaNacimiento(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento es obligatoria");
        }
        
        LocalDate fechaNac = fechaNacimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hoy = LocalDate.now();
        
        // No puede ser futura
        if (fechaNac.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento no puede ser futura");
        }
        
        // Debe tener al menos 4 años
        LocalDate fechaMinima = hoy.minusYears(MIN_EDAD_ANIOS);
        if (fechaNac.isAfter(fechaMinima)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "El usuario debe tener al menos " + MIN_EDAD_ANIOS + " años");
        }
    }

    /**
     * Calcula la edad actual de un usuario basado en su fecha de nacimiento
     * @param fechaNacimiento Fecha de nacimiento del usuario
     * @return Edad en años
     */
    public int calculateAge(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return 0;
        }
        LocalDate fechaNac = fechaNacimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hoy = LocalDate.now();
        return Period.between(fechaNac, hoy).getYears();
    }

    /**
     * Valida reglas de negocio específicas para usuarios (validaciones básicas van en DTOs)
     */
    public void validateBusinessRulesForUser(String email, String alias, String password) throws ResponseStatusException {
        logger.info("[VALIDATION] Validando reglas de negocio para: {}", email);
        
        // validateEmailDomain(email); // Eliminado - ahora se hace automáticamente en el DTO con @ValidEmailDomain
        validatePasswordStrength(password, alias, email);
        
        logger.info("[VALIDATION] Reglas de negocio validadas");
    }

    /**
     * Valida la longitud máxima de un campo de texto
     */
    public void validateFieldLength(String value, String fieldName, int maxLength) {
        if (value.length() > maxLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                String.format("El campo %s no puede tener más de %d caracteres", fieldName, maxLength));
        }
    }

    /**
     * Valida datos específicos para un usuario normal
     */
    public void validateNormalUserSpecific(String email) throws ResponseStatusException {
        logger.info("[VALIDATION] Verificando unicidad global del email para usuario normal: {}", email);

        // Validar unicidad global del email en todas las tablas de usuarios
        validateGlobalEmailUniqueness(email);

        logger.info("[VALIDATION] Email disponible globalmente: {}", email);
    }

    /**
     * Valida datos específicos para un usuario normal incluyendo fecha de nacimiento
     */
    public void validateUsuarioNormalData(String email, String alias, String password, Date fechaNacimiento) {
        logger.info("[VALIDATION] Iniciando validaciones específicas para UsuarioNormal");
        validateBusinessRulesForUser(email, alias, password);
        validateFechaNacimiento(fechaNacimiento);
        validateNormalUserSpecific(email);
        logger.info("[VALIDATION] Validaciones específicas completadas para UsuarioNormal");
    }

    /**
     * Valida datos específicos para un creador de contenido
     */
    public void validateCreatorSpecific(String email, String aliasCreador) throws ResponseStatusException {
        logger.info("[VALIDATION] Validando unicidad de creador - email: {}, alias: {}", email, aliasCreador);

        // Validar unicidad global del email
        validateGlobalEmailUniqueness(email);

        // Validar unicidad del alias del creador
        if (aliasCreador == null || aliasCreador.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alias del creador es obligatorio");
        }
        if (creadorContenidoRepository.existsByAliasCreador(aliasCreador)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un creador con ese alias");
        }

        logger.info("[VALIDATION] Validaciones de creador completadas - email: {}, alias: {}", email, aliasCreador);
    }

    /**
     * Valida la unicidad global del email en todas las tablas de usuarios
     */
    public void validateGlobalEmailUniqueness(String email) throws ResponseStatusException {
        logger.info("[VALIDATION] Validando unicidad global del email: {}", email);

        if (usuarioNormalRepository.existsByemail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un usuario registrado con este email");
        }

        if (creadorContenidoRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un creador registrado con este email");
        }

        if (adminRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un administrador registrado con este email");
        }

        logger.info("[VALIDATION] Email disponible globalmente: {}", email);
    }


    /**
     * Valida tipo de creador, lanzando excepción si no es válido
     */
    public void validarTipoCreador(String tipoContenido) throws ResponseStatusException {
        if (tipoContenido == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe especificar tipoContenido");
        }
        
        try {
            TipoContenido.valueOf(tipoContenido.toUpperCase());
        } 
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Tipo de contenido inválido. Debe ser VIDEO o AUDIO");
        }
        catch (Exception e) {
            logger.error("Unexpected validation error", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Error inesperado en validación de tipo");
        }
    }
    
    /**
     * Valida que un creador puede subir contenido del tipo especificado
     * @param creador El creador de contenido
     * @param tipoContenido El tipo de contenido a subir
     * @throws ResponseStatusException si no hay compatibilidad
     */
    public void validateCreatorContentTypeCompatibility(CreadorContenido creador, TipoContenido tipoContenido) 
            throws ResponseStatusException {
        if (creador == null || tipoContenido == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Creador y tipo de contenido son requeridos");
        }
        
        if (creador.getTipoContenido() != tipoContenido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                String.format("El creador de tipo %s no puede subir contenido de tipo %s", 
                    creador.getTipoContenido().getValor(), tipoContenido.getValor()));
        }
    }
    
    // -------------------- VALIDACIONES DE CONTENIDO --------------------
    public String validateContentUpload(ContentUploadDTO contentDTO) throws IllegalArgumentException {
        String[] errores = new String[] {
            validateBasicFields(contentDTO),
            validateExpirationDate(contentDTO.getFechaExpiracion()),
            validateImageFormat(contentDTO.getMiniatura())
        };
        for (String error : errores) {
            if (error != null) {
                return error;
            }
        }
        return null;
    }
    
    private String validateBasicFields(ContentUploadDTO contentDTO) throws IllegalArgumentException {
        String[] errores = new String[] {
            (contentDTO.getTags() == null || contentDTO.getTags().isEmpty() || contentDTO.getTags().size() > 25) ? "Debe incluir entre 1 y 25 tags" : null,
        };
        String result = null;
        for (String error : errores) {
            if (error != null) {
                result = error;
                break;
            }
        }
        return result;
    }
    
    private String validateExpirationDate(String fechaExpiracion) throws IllegalArgumentException {
        String result = null;
        if (fechaExpiracion != null && !fechaExpiracion.trim().isEmpty()) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                formatter.parse(fechaExpiracion);
            } 
            catch (ParseException e) {
                result = "Formato de fecha inválido. Use yyyy-MM-dd";
            }
        }
        return result;
    }
    
    private String validateImageFormat(String imagen) throws IllegalArgumentException {
        // Si la imagen es null o vacía, permitir (es opcional)
        if (imagen == null || imagen.trim().isEmpty()) {
            return null;
        }

        String[] errores = new String[] {
            (imagen.startsWith("http://") || imagen.startsWith("https://")) ? "La imagen debe ser enviada en formato base64 o data URI, no como URL" : null,
            (imagen.length() > 1024 * 1024 * 2) ? "La imagen es demasiado grande (máximo 2MB)" : null
        };
        String result = null;
        for (String error : errores) {
            if (error != null) {
                result = error;
                break;
            }
        }
        return result;
    }


    public String validateAudioContent(ContentAudioUploadDTO audioDTO) throws IllegalArgumentException {
        String contenido = audioDTO.getFichero();

        String[] errores = new String[] {
            validateContentUpload(audioDTO),
            (!ContentUtil.isValidBase64(contenido)) ? "El archivo de audio debe estar en formato base64 válido" : null,
            getAudioProcessingError(contenido)
        };
        String result = null;
        for (String error : errores) {
            if (error != null) {
                result = error;
                break;
            }
        }
        return result;
    }

    private String getAudioProcessingError(String contenido) throws IllegalArgumentException {
        String result = null;

        byte[] audioBytes = ContentUtil.decodeBase64(contenido);
        String[] errores = new String[] {
            (audioBytes == null) ? "Error al procesar el archivo de audio" : null,
            (audioBytes != null && !esAudioSoportado(audioBytes)) ? "Formato de audio no soportado" : null,
            (audioBytes != null && audioBytes.length > MAX_AUDIO_SIZE) ? "El archivo de audio debe ser menor a 1MB" : null
        };
        for (String error : errores) {
            if (error != null) {
                return error;
            }
        } 
        
        return result;
    }

    public String validateVideoContent(ContentVideoUploadDTO videoDTO) throws IllegalArgumentException {
        String[] errores = new String[] {
            validateContentUpload(videoDTO),
            (!isValidResolution(videoDTO.getResolucion())) ? "Resolución inválida. Use: 720, 1080 o 4k" : null,
            (!isValidVideoUrl(videoDTO.getUrlArchivo())) ? "La URL del video debe ser de un dominio soportado (YouTube, Vimeo, Twitch, etc.)" : null
        };
        String result = null;
        for (String error : errores) {
            if (error != null) {
                result = error;
                break;
            }
        }
        return result;
    }

    // Método auxiliar para validar resolución
    private boolean isValidResolution(String resolucion) {
        boolean result = false;
        result = resolucion.equals("720") || resolucion.equals("1080") || resolucion.equals("4K");
        return result;
    }

    // Método auxiliar para validar URL de video
    private boolean isValidVideoUrl(String url) {
        boolean result = false;
        result = DominiosVideoPermitidos.isUrlVideoValida(url);
        return result;
    }

    public boolean esAudioSoportado(byte[] audioBytes) {
        Tika tika = new Tika();
        String mimeType = tika.detect(audioBytes);
        return ArchivosAudioSoportados.contains(mimeType);
    }

    /**
     * Valida la unicidad del email y alias para administradores
     */
    public void validateAdminUniqueness(String email, String alias) throws ResponseStatusException {
        logger.info("[VALIDATION] Validando unicidad de administrador - email: {}, alias: {}", email, alias);

        // Verificar unicidad del email contra usuarios normales y creadores (no contra administradores)
        if (usuarioNormalRepository.existsByemail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un usuario registrado con este email");
        }

        if (creadorContenidoRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un creador registrado con este email");
        }

        // Verificar unicidad del alias contra usuarios normales y creadores
        if (usuarioNormalRepository.existsByalias(alias)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un usuario registrado con este alias");
        }

        if (creadorContenidoRepository.existsByAlias(alias)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un creador registrado con este alias");
        }

        logger.info("[VALIDATION] Unicidad de administrador validada - email: {}, alias: {}", email, alias);
    }

    /**
     * Valida datos para actualizar contenido.
     * Campos opcionales, pero si se proporcionan, deben ser válidos.
     * @param updateDTO DTO con datos de actualización
     * @return null si válido, mensaje de error si no
     */
    public String validateContentUpdate(ContentUpdateDTO updateDTO) throws IllegalArgumentException {

        // Validar fecha de expiración si se proporciona
        if (updateDTO.getFechaExpiracion() != null && !updateDTO.getFechaExpiracion().trim().isEmpty()) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                formatter.parse(updateDTO.getFechaExpiracion());
            }
            catch (ParseException e) {
                    return "Formato de fecha inválido. Use yyyy-MM-dd";
                }
        }

        // Validar restricción de edad si se proporciona
        if (updateDTO.getRestriccionEdad() != null && !RestriccionEdad.isValidValue(updateDTO.getRestriccionEdad())) {
            return "El valor de restricción de edad no es válido. Valores permitidos: 3, 7, 12, 16, 18";
        }

        return null;
    }

    /**
     * Valida que un gestor de contenido pueda editar o eliminar contenido específico.
     * Los gestores solo pueden editar/eliminar contenido de su mismo tipo.
     * @param creador El creador que intenta editar/eliminar
     * @param tipoContenido El tipo de contenido que se quiere editar/eliminar
     * @throws ResponseStatusException si no está autorizado
     */
    public void validateContentEditPermission(CreadorContenido creador, TipoContenido tipoContenido) throws ResponseStatusException {
        if (creador == null || tipoContenido == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creador y tipo de contenido son requeridos");
        }
        
        if (creador.getTipoContenido() != tipoContenido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                String.format("El gestor de tipo %s no puede editar/eliminar contenido de tipo %s", 
                    creador.getTipoContenido().getValor(), tipoContenido.getValor()));
        }
        
        logger.info("[VALIDATION] Edición/eliminación permitida para creador {} en contenido de tipo {}", creador.getAliasCreador(), tipoContenido);
    }
}