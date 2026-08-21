package com.medilabo.riskservice.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Corps de réponse d'erreur uniforme renvoyé par l'API, sans détail technique
 * (pas de stacktrace) afin de ne pas divulguer d'information sensible.
 *
 * @param timestamp   horodatage de l'erreur
 * @param status      code HTTP
 * @param message     message d'erreur destiné au client
 * @param fieldErrors erreurs de validation par champ, ou {@code null} si non applicable
 * @since 1.0
 */
public record ApiError(Instant timestamp, int status, String message, Map<String, String> fieldErrors) {

    /**
     * Construit une erreur sans détail de validation par champ.
     *
     * @param status  code HTTP
     * @param message message d'erreur
     * @return l'erreur construite
     */
    public static ApiError of(int status, String message) {
        return new ApiError(Instant.now(), status, message, null);
    }
}
