package com.esimedia.shared.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidades comunes para el procesamiento de contenido multimedia.
 */
public class ContentProcessingUtil {

    private static final Logger logger = LoggerFactory.getLogger(ContentProcessingUtil.class);
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    // Private constructor to prevent instantiation
    private ContentProcessingUtil() {}

    /**
     * Parsea una fecha de expiración desde una cadena en formato yyyy-MM-dd.
     * @param fechaExpiracionStr La fecha como cadena.
     * @return La fecha parseada o null si hay error o es nula/vacía.
     */
    public static Date parseFechaExpiracion(String fechaExpiracionStr) {
        if (fechaExpiracionStr != null && !fechaExpiracionStr.trim().isEmpty()) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                return formatter.parse(fechaExpiracionStr);
            }
            catch (ParseException e) {
                logger.warn("Error parseando fecha de expiración: {}", fechaExpiracionStr);
                return null;
            }
        }
        return null;
    }

    /**
     * Formatea una fecha en formato yyyy-MM-dd para las respuestas de la API.
     * @param date La fecha a formatear.
     * @return La fecha como cadena o null si la fecha es nula.
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        try {
            return DATE_FORMATTER.get().format(date);
        } finally {
            DATE_FORMATTER.remove();
        }
    }
}