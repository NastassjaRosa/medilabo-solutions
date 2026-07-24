package com.medilabo.patientservice.exception;

/**
 * Levée lorsqu'un patient demandé n'existe pas dans la base.
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
