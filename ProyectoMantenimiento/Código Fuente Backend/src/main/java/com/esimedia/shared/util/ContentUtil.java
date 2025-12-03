package com.esimedia.shared.util;

import java.util.Base64;

public class ContentUtil {
    
    private static final String DATA_URI_PREFIX = "data:";
    private static final String DATA_URI_BASE64_PREFIX = ";base64,";
    
    // Private constructor to prevent instantiation
    private ContentUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Recibe una extensión MIME y un array de bytes, y devuelve la cadena en formato base64 tipo data:<extension>;base64,<contenido>
     */
    public static String createBase64StringFromBinary(String mimeType, byte[] bytes) {
        if (mimeType == null || bytes == null) {
            return null;
        }
        String base64Data = Base64.getEncoder().encodeToString(bytes);
        return DATA_URI_PREFIX + mimeType + DATA_URI_BASE64_PREFIX + base64Data;
    }

    /**
     * Decodifica una cadena base64 pura (sin prefijos) a bytes.
     * @param base64Content Contenido base64 puro
     * @return Array de bytes decodificado
     * @throws IllegalArgumentException si el base64 no es válido
     */
    public static byte[] decodeBase64(String base64Content) {
        if (base64Content == null || base64Content.trim().isEmpty()) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(base64Content.trim());
        } 
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Contenido base64 inválido", e);
        }
    }

    /**
     * Extrae el MIME type de un data URI.
     * @param dataUri Data URI en formato "data:tipo/mime" o "data:tipo/mime;base64,contenido"
     * @return El tipo MIME extraído, o null si no es válido
     */
    public static String extractMimeFromDataUri(String dataUri) {
        if (dataUri == null || !dataUri.startsWith(DATA_URI_PREFIX)) {
            // Si no es data URI, devolver como está
            return dataUri;
        }
        
        // Remover "data:"
        String withoutPrefix = dataUri.substring(5);
        int semicolonIndex = withoutPrefix.indexOf(';');
        
        if (semicolonIndex != -1) {
            return withoutPrefix.substring(0, semicolonIndex);
        } 
        else {
            return withoutPrefix;
        }
    }

    /**
     * Decodifica contenido de imagen Base64 (validaciones ya hechas en DTO)
     * @param base64Content Contenido base64 puro (puede ser null para imágenes opcionales)
     * @return Array de bytes decodificado o null si no hay contenido
     */
    public static byte[] decodeImage(String base64Content) {
        if (base64Content == null || base64Content.trim().isEmpty()) {
            return new byte[0];
        }
        return decodeBase64(base64Content);
    }

    /**
     * Decodifica contenido de audio Base64 (validaciones ya hechas en DTO)
     * @param base64Content Contenido base64 puro (ya validado como @NotBlank en DTO)
     * @return Array de bytes decodificado
     */
    public static byte[] decodeAudio(String base64Content) {
        return decodeBase64(base64Content);
    }



    /**
     * Decodifica contenido de video Base64 (validaciones ya hechas en DTO)
     * @param base64Content Contenido base64 puro (ya validado como @NotBlank en DTO)
     * @return Array de bytes decodificado
     */
    public static byte[] decodeVideo(String base64Content) {
        return decodeBase64(base64Content);
    }



    /**
     * Decodifica contenido base64 con manejo específico de errores para cualquier tipo de contenido.
     * Para contenido OBLIGATORIO (ficheros de audio/video).
     * @param contentBase64 Contenido base64 puro a decodificar
     * @param contentType Tipo de contenido para mensajes de error específicos (ej: "fichero de audio", "miniatura", "foto de perfil")
     * @return Array de bytes decodificado
     * @throws IllegalArgumentException si el base64 no es válido o está vacío
     */
    public static byte[] decodeContentBase64(String contentBase64, String contentType) {
        if (contentBase64 == null || contentBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("El " + contentType + " no puede estar vacío");
        }
        try {
            return Base64.getDecoder().decode(contentBase64.trim());
        } 
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(contentType + " no está en base64 válido", e);
        }
    }

    /**
     * Convierte bytes y MIME type a data URI completo.
     * @param bytes Array de bytes del contenido (puede ser null)
     * @param mimeType Tipo MIME (ej: "image/jpeg", "audio/wav")
     * @return Data URI completo (ej: "data:image/jpeg;base64,UklGRig...") o null si bytes es null
     */
    public static String createDataUri(byte[] bytes, String mimeType) {
        if (bytes == null || mimeType == null) {
            return null;
        }
        String base64Data = Base64.getEncoder().encodeToString(bytes);
        return DATA_URI_PREFIX + mimeType + DATA_URI_BASE64_PREFIX + base64Data;
    }

    /**
     * Convierte Binary (MongoDB) y MIME type a data URI completo.
     * @param binary MongoDB Binary object (puede ser null)
     * @param mimeType Tipo MIME
     * @return Data URI completo o null si binary es null
     */
    public static String createDataUriFromBinary(byte[] binary, String mimeType) {
        return createDataUri(binary, mimeType);
    }

    /**
     * Valida que una cadena sea base64 válido.
     * @param base64Content Contenido a validar
     * @return true si es base64 válido, false en caso contrario
     */
    public static boolean isValidBase64(String base64Content) {
        if (base64Content == null || base64Content.trim().isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(base64Content.trim());
            return true;
        } 
        catch (IllegalArgumentException e) {
            return false;
        }
    }
}