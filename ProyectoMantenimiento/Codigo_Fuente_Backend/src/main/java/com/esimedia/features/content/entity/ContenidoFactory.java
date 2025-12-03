package com.esimedia.features.content.entity;

import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.content.dto.ContentAudioUploadDTO;
import com.esimedia.features.content.dto.ContentUploadDTO;
import com.esimedia.features.content.dto.ContentVideoUploadDTO;
import com.esimedia.shared.util.ContentUtil;

/**
 * Factory para crear instancias de Contenido a partir de DTOs.
 * Utiliza el patrón Factory Method para encapsular la lógica de creación
 * y mantener el código desacoplado de las implementaciones concretas.
 */
public class ContenidoFactory {

    private ContenidoFactory() {
            }


    /**
     * Crea una instancia de Contenido a partir de un ContentUploadDTO.
     * 
     * @param dto el DTO de contenido
     * @return una instancia de Contenido del tipo apropiado
     * @throws IllegalArgumentException si el tipo de DTO no es soportado
     * @throws NullPointerException si el dto es null
     */
    public static Contenido crearContenido(ContentUploadDTO dto) throws IllegalArgumentException, NullPointerException {
        if (dto == null) {
            throw new NullPointerException("El DTO no puede ser null");
        }

        if (dto instanceof ContentAudioUploadDTO contentAudioUploadDTO) {
            return crearContenidoAudio(contentAudioUploadDTO);
        } 
        else if (dto instanceof ContentVideoUploadDTO contentVideoUploadDTO) {
            return crearContenidoVideo(contentVideoUploadDTO);
        }
        else {
            throw new IllegalArgumentException("Tipo de DTO no soportado: " + dto.getClass().getSimpleName());
        }
    }

    /**
     * Crea un ContenidosAudio a partir de un ContentAudioUploadDTO
     */
    private static ContenidosAudio crearContenidoAudio(ContentAudioUploadDTO audioDTO) {
        byte[] imagenBytes = null;
        String imagenExtension = null;
        
        // Solo procesar miniatura si se proporciona
        if (audioDTO.getMiniatura() != null && !audioDTO.getMiniatura().trim().isEmpty()) {
            imagenBytes = ContentUtil.decodeImage(audioDTO.getMiniatura());
            imagenExtension = audioDTO.getFormatoMiniatura();
        }
        return ContenidosAudio.builder()
            .titulo(audioDTO.getTitulo())
            .descripcion(audioDTO.getDescripcion())
            .duracion(audioDTO.getDuracion())
            .esVIP(Boolean.TRUE.equals(audioDTO.getEsVIP()))
            .miniatura(imagenBytes)
            .formatoMiniatura(imagenExtension)
            .fichero(ContentUtil.decodeAudio(audioDTO.getFichero()))
            .ficheroExtension(audioDTO.getFicheroExtension())
            .build();
    }

    /**
     * Crea un ContenidosVideo a partir de un ContentVideoUploadDTO
     */
    private static ContenidosVideo crearContenidoVideo(ContentVideoUploadDTO videoDTO) {
        byte[] imagenBytes = null;
        String imagenExtension = null;
        
        // Solo procesar miniatura si se proporciona
        if (videoDTO.getMiniatura() != null && !videoDTO.getMiniatura().trim().isEmpty()) {
            imagenBytes = ContentUtil.decodeImage(videoDTO.getMiniatura());
            imagenExtension = videoDTO.getFormatoMiniatura();
        }
        
        return ContenidosVideo.builder()
            .titulo(videoDTO.getTitulo())
            .descripcion(videoDTO.getDescripcion())
            .duracion(videoDTO.getDuracion())
            .esVIP(Boolean.TRUE.equals(videoDTO.getEsVIP()))
            .restriccionEdad(videoDTO.getRestriccionEdad() != null ? 
                getRestriccionEdadFromValue(videoDTO.getRestriccionEdad()) : null)
            .miniatura(imagenBytes)
            .formatoMiniatura(imagenExtension)
            .urlArchivo(videoDTO.getUrlArchivo())
            .resolucion(videoDTO.getResolucion() != null ? 
                com.esimedia.features.content.enums.Resolucion.valueOf(videoDTO.getResolucion()) : null)
            .build();
    }

    /**
     * Crea un ContenidosAudio con parámetros básicos
     */
    public static ContenidosAudio crearContenidoAudio(String titulo, String descripcion, int duracion, String idCreador) {
        return ContenidosAudio.builder()
            .titulo(titulo)
            .descripcion(descripcion)
            .duracion(duracion)
            .idCreador(idCreador)
            .build();
    }

    /**
     * Crea un ContenidosVideo con parámetros básicos
     */
    public static ContenidosVideo crearContenidoVideo(String titulo, String descripcion, int duracion, String idCreador, String urlArchivo) {
        return ContenidosVideo.builder()
            .titulo(titulo)
            .descripcion(descripcion)
            .duracion(duracion)
            .idCreador(idCreador)
            .urlArchivo(urlArchivo)
            .build();
    }
    
    /**
     * Convierte un valor entero a RestriccionEdad
     */
    private static RestriccionEdad getRestriccionEdadFromValue(Integer valor) {
        if (valor == null) return null;
        
        for (RestriccionEdad restriccion : RestriccionEdad.values()) {
            if (restriccion.getValor() == valor) {
                return restriccion;
            }
        }
        throw new IllegalArgumentException("Valor de restricción de edad no válido: " + valor);
    }
}