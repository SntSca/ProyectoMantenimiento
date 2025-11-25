package com.esimedia.features.content.enums;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;


public class DominiosVideoPermitidos {
    
    // Plataformas de video principales
    public static final String YOUTUBE = "youtube.com";
    public static final String YOUTUBE_WWW = "www.youtube.com";
    public static final String YOUTUBE_SHORT = "youtu.be";
    public static final String YOUTUBE_EMBED = "youtube-nocookie.com";
    
    // Vimeo
    public static final String VIMEO = "vimeo.com";
    public static final String VIMEO_WWW = "www.vimeo.com";
    public static final String VIMEO_PLAYER = "player.vimeo.com";
    
    private static final Set<String> DOMINIOS_VIDEO_PERMITIDOS = Set.of(
        // YouTube
        YOUTUBE, YOUTUBE_WWW, YOUTUBE_SHORT, YOUTUBE_EMBED,
        // Vimeo
        VIMEO, VIMEO_WWW, VIMEO_PLAYER
    );
    
    private DominiosVideoPermitidos() {}


    /**
     * Verifica si un dominio está permitido para videos
     * @param domain El dominio de la URL del video a verificar
     * @return true si el dominio está permitido, false en caso contrario
     */
    public static boolean isDominioVideoPermitido(String domain) {
        return domain != null && DOMINIOS_VIDEO_PERMITIDOS.contains(domain.toLowerCase());
    }
    
    /**
     * Extrae el dominio de una URL
     * @param url La URL de la cual extraer el dominio
     * @return El dominio de la URL, o cadena vacía si no es válido
     */
    public static String extractDomainFromUrl(String url) {
        String response = "";
        try {
            // Normalizar la URL agregando protocolo si no lo tiene
            String normalizedUrl = url.trim();
            if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
                normalizedUrl = "https://" + normalizedUrl;
            }
            
            URL parsedUrl = new URL(normalizedUrl);
            response = parsedUrl.getHost().toLowerCase();
        } 
        catch (MalformedURLException e) {
            response = "";
        }
        return response;
    }
    
    /**
     * Valida si una URL de video es válida y de un dominio permitido
     * @param url La URL del video a validar
     * @return true si la URL es válida y el dominio está permitido, false en caso contrario
     */
    public static boolean isUrlVideoValida(String url) {
        String domain = extractDomainFromUrl(url);
        return !domain.isEmpty() && isDominioVideoPermitido(domain);
    }
}