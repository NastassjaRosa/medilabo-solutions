package com.medilabo.riskservice.service;

import com.medilabo.riskservice.engine.RiskLevel;

/**
 * Résultat de l'évaluation du risque d'un patient, prêt à être exposé par l'API.
 *
 * @param patientId  identifiant du patient évalué
 * @param prenom     prénom du patient
 * @param nom        nom du patient
 * @param riskLevel  niveau de risque calculé
 * @since 1.0
 */
public record RiskAssessment(Long patientId, String prenom, String nom, RiskLevel riskLevel) {
}
