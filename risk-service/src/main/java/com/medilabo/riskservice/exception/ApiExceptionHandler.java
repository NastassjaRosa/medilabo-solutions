package com.medilabo.riskservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier en réponses HTTP homogènes, sans jamais renvoyer
 * de stacktrace au client.
 *
 * @since 1.0
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Traduit un patient introuvable en réponse 404.
     *
     * @param ex exception levée par la couche service
     * @return réponse 404 avec message d'erreur
     */
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ApiError> handlePatientNotFound(PatientNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    /**
     * Traduit une dépendance indisponible (patient-service ou notes-service) en
     * réponse 503 explicite. Ne renvoie jamais un niveau de risque par défaut.
     *
     * @param ex exception levée par la couche service
     * @return réponse 503 avec message d'erreur explicite
     */
    @ExceptionHandler(DependencyUnavailableException.class)
    public ResponseEntity<ApiError> handleDependencyUnavailable(DependencyUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage()));
    }
}
