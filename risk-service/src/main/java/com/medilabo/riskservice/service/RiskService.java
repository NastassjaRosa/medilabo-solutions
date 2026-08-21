package com.medilabo.riskservice.service;

/**
 * Évalue le risque de diabète de type 2 d'un patient à partir de son dossier
 * démographique et de ses notes médicales.
 *
 * @since 1.0
 */
public interface RiskService {

    /**
     * Évalue le risque de diabète d'un patient.
     *
     * @param patientId identifiant du patient
     * @return le résultat de l'évaluation
     * @throws com.medilabo.riskservice.exception.PatientNotFoundException      si le patient n'existe pas
     * @throws com.medilabo.riskservice.exception.DependencyUnavailableException si patient-service ou
     *         notes-service est indisponible (le risque reste alors indéterminé, jamais {@code None})
     */
    RiskAssessment evaluateRisk(Long patientId);
}
