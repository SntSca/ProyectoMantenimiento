package com.esimedia.features.content.enums;

import lombok.Getter;

@Getter
public enum TipoImagen {
    JPEG("image/jpeg", "jpeg"),
    JPG("image/jpg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");
    
    private final String mimeType;
    private final String extension;
    
    TipoImagen(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }
    
    /**
     * Obtiene el TipoImagen basado en el MIME type
     */
    public static TipoImagen fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        
        String mimeTypeLower = mimeType.toLowerCase();
        for (TipoImagen tipo : values()) {
            if (tipo.getMimeType().equals(mimeTypeLower)) {
                return tipo;
            }
        }
        return null;
    }
    
    /**
     * Obtiene el TipoImagen basado en la extensión
     */
    public static TipoImagen fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        
        String extensionLower = extension.toLowerCase().replace(".", "");
        for (TipoImagen tipo : values()) {
            if (tipo.getExtension().equals(extensionLower)) {
                return tipo;
            }
        }
        return null;
    }
    
    /**
     * Valida si un MIME type es soportado
     */
    public static boolean isSoportado(String mimeType) {
        return fromMimeType(mimeType) != null;
    }
    
    @Override
    public String toString() {
        return mimeType;
    }
}