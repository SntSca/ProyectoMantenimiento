package com.esimedia.shared.exception;

/**
 * Excepción lanzada cuando un token ha expirado
 */
public class TokenHasExpiredException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto
     */
    public TokenHasExpiredException() {
        super("El token ha expirado");
    }

    /**
     * Constructor con mensaje personalizado
     * @param message Mensaje de la excepción
     */
    public TokenHasExpiredException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa
     * @param message Mensaje de la excepción
     * @param cause Causa de la excepción
     */
    public TokenHasExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor con causa
     * @param cause Causa de la excepción
     */
    public TokenHasExpiredException(Throwable cause) {
        super("El token ha expirado", cause);
    }
}