package com.esimedia.features.auth.enums;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utilidad para validación de dominios de email permitidos
 * Contiene listas de dominios válidos y métodos para validar emails completos
 */
public class DominiosEmailPermitidos {

    // Patrón regex para validar formato básico de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    // Dominios populares de email
    public static final String GMAIL = "gmail.com";
    public static final String OUTLOOK = "outlook.com";
    public static final String HOTMAIL = "hotmail.com";
    public static final String YAHOO = "yahoo.com";
    public static final String YAHOO_ES = "yahoo.es";
    public static final String ICLOUD = "icloud.com";
    public static final String PROTONMAIL = "protonmail.com";
    public static final String AOL = "aol.com";
    public static final String YANDEX = "yandex.com";
    public static final String MAIL_COM = "mail.com";

    // Dominios universitarios españoles comunes
    public static final String UPM = "alumnos.upm.es";
    public static final String UCM = "ucm.es";
    public static final String UAM = "uam.es";
    public static final String UPC = "upc.edu";
    public static final String US = "us.es";

    // Dominios corporativos comunes
    public static final String EMPRESA = "empresa.com";
    public static final String COMPANY = "company.es";

    // Dominios administrativos para administradores
    public static final String ESIMEDIA_ADMIN = "gmail.com";

    private DominiosEmailPermitidos() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Set<String> DOMINIOS_GENERALES_PERMITIDOS = Set.of(
        GMAIL, OUTLOOK, HOTMAIL, YAHOO, YAHOO_ES, ICLOUD, PROTONMAIL,
        AOL, YANDEX, MAIL_COM, UPM, UCM, UAM, UPC, US, EMPRESA, COMPANY
    );

    private static final Set<String> DOMINIOS_ADMINISTRATIVOS = Set.of(
        ESIMEDIA_ADMIN
    );

    /**
     * Extrae el dominio de una dirección de email
     * @param email La dirección de email
     * @return El dominio en minúsculas, o null si el formato es inválido
     */
    public static String extractDomain(String email) {
        if (email == null) {
            return null;
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            return null;
        }

        return email.substring(atIndex + 1).toLowerCase();
    }

    /**
     * Valida que un email tenga un formato básico válido
     * @param email El email a validar
     * @return true si el formato es válido, false en caso contrario
     */
    public static boolean isValidEmailFormat(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Verifica si un dominio está permitido para usuarios generales (usuarios normales y creadores)
     * @param domain El dominio del email a verificar
     * @return true si el dominio está permitido, false en caso contrario
     */
    public static boolean isDominioGeneralPermitido(String domain) {
        return domain != null && DOMINIOS_GENERALES_PERMITIDOS.contains(domain.toLowerCase());
    }

    /**
     * Verifica si un dominio está permitido para administradores
     * @param domain El dominio del email a verificar
     * @return true si el dominio está permitido para administradores, false en caso contrario
     */
    public static boolean isDominioAdministrativoPermitido(String domain) {
        return domain != null && DOMINIOS_ADMINISTRATIVOS.contains(domain.toLowerCase());
    }

    /**
     * Verifica si un dominio está permitido (tanto para usuarios generales como administradores)
     * @param domain El dominio del email a verificar
     * @return true si el dominio está permitido, false en caso contrario
     */
    public static boolean isDominioPermitido(String domain) {
        return isDominioGeneralPermitido(domain) || isDominioAdministrativoPermitido(domain);
    }

    /**
     * Valida completamente un email para usuarios generales
     * Verifica tanto el formato como que el dominio esté permitido
     * @param email El email completo a validar
     * @return true si el email es válido, false en caso contrario
     */
    public static boolean isEmailGeneralValido(String email) {
        if (!isValidEmailFormat(email)) {
            return false;
        }

        String domain = extractDomain(email);
        return isDominioGeneralPermitido(domain);
    }

    /**
     * Valida completamente un email para administradores
     * Verifica tanto el formato como que el dominio esté en la lista administrativa
     * @param email El email completo a validar
     * @return true si el email es válido para administradores, false en caso contrario
     */
    public static boolean isEmailAdministrativoValido(String email) {
        if (!isValidEmailFormat(email)) {
            return false;
        }

        String domain = extractDomain(email);
        return isDominioAdministrativoPermitido(domain);
    }

    /**
     * Valida completamente un email (para cualquier tipo de usuario)
     * Verifica tanto el formato como que el dominio esté permitido
     * @param email El email completo a validar
     * @return true si el email es válido, false en caso contrario
     */
    public static boolean isEmailValido(String email) {
        if (!isValidEmailFormat(email)) {
            return false;
        }

        String domain = extractDomain(email);
        return isDominioPermitido(domain);
    }
}