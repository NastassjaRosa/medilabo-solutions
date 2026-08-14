package com.medilabo.frontend.exception;

import java.util.Map;

/**
 * Levee lorsque le patient-service rejette une creation/modification pour cause de validation
 * (400), avec le detail des erreurs par champ afin de les reafficher dans le formulaire.
 *
 * @since 1.0
 */
public class GatewayValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    /**
     * @param fieldErrors erreurs de validation par champ renvoyees par le patient-service
     */
    public GatewayValidationException(Map<String, String> fieldErrors) {
        super("Validation refusee par le patient-service");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
