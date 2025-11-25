package com.esimedia.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada para validar que el dominio del email esté en la lista de dominios permitidos
 */
@Constraint(validatedBy = ValidEmailDomainValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmailDomain {

    String message() default "El dominio del email no está permitido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}