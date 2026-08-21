package com.medilabo.riskservice.exception;

/**
 * Levée lorsque le patient demandé n'existe pas dans patient-service. Un patient
 * introuvable produit une réponse 404 explicite — à ne pas confondre avec une
 * dépendance indisponible ({@link DependencyUnavailableException}), qui doit
 * rester distincte du "risque None" par mesure de fail-safe.
 *
 * @since 1.0
 */
public class PatientNotFoundException extends RuntimeException {

    /**
     * Construit l'exception pour un identifiant de patient introuvable.
     *
     * @param id identifiant recherché
     */
    public PatientNotFoundException(Long id) {
        super("Patient introuvable pour l'id " + id);
    }
}
