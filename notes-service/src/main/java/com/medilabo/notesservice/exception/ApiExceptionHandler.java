package com.medilabo.notesservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduit les exceptions métier et de validation en réponses HTTP homogènes,
 * sans jamais renvoyer de stacktrace au client.
 *
 * @since 1.0
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Traduit une note introuvable en réponse 404.
     *
     * @param ex exception levée par la couche service
     * @return réponse 404 avec message d'erreur
     */
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ApiError> handleNoteNotFound(NoteNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    /**
     * Traduit un échec de validation Bean Validation en réponse 400 détaillant
     * le champ en défaut.
     *
     * @param ex exception levée lors de la validation du corps de requête
     * @return réponse 400 avec le détail des champs invalides
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.ofValidation(HttpStatus.BAD_REQUEST.value(), "Données invalides", fieldErrors));
    }
}
