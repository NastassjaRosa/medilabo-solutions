package com.medilabo.frontend.exception;

/**
 * Levee lorsque le patient-service (via la gateway) ne trouve pas le patient demande.
 *
 * @since 1.0
 */
public class PatientNotFoundException extends RuntimeException {

    /**
     * @param patientId identifiant du patient introuvable
     */
    public PatientNotFoundException(Long patientId) {
        super("Patient introuvable : " + patientId);
    }
}
