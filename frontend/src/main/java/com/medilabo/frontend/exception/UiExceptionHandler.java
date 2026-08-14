package com.medilabo.frontend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Traduit les erreurs d'acces au patient-service (via la gateway) en pages d'erreur sobres,
 * sans exposer de detail technique (pas de stacktrace) au navigateur.
 *
 * @since 1.0
 */
@ControllerAdvice
public class UiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(UiExceptionHandler.class);

    /**
     * @param exception patient introuvable
     * @param model     modele de la vue d'erreur
     * @return la vue d'erreur, avec un statut 404
     */
    @ExceptionHandler(PatientNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlePatientNotFound(PatientNotFoundException exception, Model model) {
        model.addAttribute("message", "Ce patient n'existe pas.");
        return "error";
    }

    /**
     * Echec explicite (jamais silencieux) lorsque la gateway ou un service en aval est
     * indisponible : la panne est journalisee sans donnee patient, et l'utilisateur voit un
     * message generique plutot qu'une liste vide trompeuse.
     *
     * @param exception panne d'acces a la gateway
     * @param model     modele de la vue d'erreur
     * @return la vue d'erreur, avec un statut 503
     */
    @ExceptionHandler(GatewayUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleGatewayUnavailable(GatewayUnavailableException exception, Model model) {
        log.error("Appel a la gateway indisponible ou en echec", exception);
        model.addAttribute("message", "Le service est temporairement indisponible. Veuillez réessayer.");
        return "error";
    }
}
