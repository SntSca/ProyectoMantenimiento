package com.esimedia.shared.validation;

import com.esimedia.features.auth.enums.DominiosEmailPermitidos;

/**
 * Clase base para validadores de email que proporciona funcionalidad común
 */
public abstract class BaseEmailValidator {

    /**
     * Método abstracto que deben implementar las subclases para definir
     * qué tipo de validación de email realizar
     * @param email El email a validar
     * @return true si el email es válido según las reglas específicas, false en caso contrario
     */
    protected abstract boolean isEmailValid(String email);

    /**
     * Método de conveniencia para validar emails generales usando DominiosEmailPermitidos
     * @param email El email a validar
     * @return true si es válido para usuarios generales
     */
    protected boolean isValidGeneralEmail(String email) {
        return DominiosEmailPermitidos.isEmailGeneralValido(email);
    }

    /**
     * Método de conveniencia para validar emails administrativos usando DominiosEmailPermitidos
     * @param email El email a validar
     * @return true si es válido para administradores
     */
    protected boolean isValidAdminEmail(String email) {
        return DominiosEmailPermitidos.isEmailAdministrativoValido(email);
    }

    /**
     * Método de conveniencia para validar cualquier email permitido usando DominiosEmailPermitidos
     * @param email El email a validar
     * @return true si es válido para cualquier tipo de usuario
     */
    protected boolean isValidAnyEmail(String email) {
        return DominiosEmailPermitidos.isEmailValido(email);
    }
}