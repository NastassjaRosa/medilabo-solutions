package com.medilabo.riskservice.dto;

import com.medilabo.riskservice.engine.RiskLevel;

/**
 * Réponse de l'API pour {@code GET /risk/{patientId}}.
 *
 * @param patientId  identifiant du patient évalué
 * @param prenom     prénom du patient, pour affichage direct côté front
 * @param nom        nom du patient, pour affichage direct côté front
 * @param riskLevel  niveau de risque calculé
 * @since 1.0
 */
public record RiskResponseDTO(Long patientId, String prenom, String nom, RiskLevel riskLevel) {
}
