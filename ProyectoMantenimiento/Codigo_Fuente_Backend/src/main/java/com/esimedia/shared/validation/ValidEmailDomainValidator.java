package com.esimedia.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para la anotación @ValidEmailDomain
 * Verifica que el email completo esté en la lista de dominios permitidos
 */
public class ValidEmailDomainValidator extends BaseEmailValidator implements ConstraintValidator<ValidEmailDomain, String> {

    @Override
    public void initialize(ValidEmailDomain constraintAnnotation) {
        // No se necesita inicialización específica
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            // Dejar que @NotBlank maneje los valores nulos/vacíos
            return true;
        }

        // Usar la validación centralizada
        return isEmailValid(email);
    }

    @Override
    protected boolean isEmailValid(String email) {
        // Validar email para usuarios generales (cualquier dominio permitido)
        return isValidAnyEmail(email);
    }
}