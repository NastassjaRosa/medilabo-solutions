package com.medilabo.riskservice.client.dto;

/**
 * Genre administratif d'un patient, tel que renvoyé par {@code patient-service}.
 * Copie locale volontaire (pas de dépendance de compilation entre microservices) :
 * utilisé par {@link com.medilabo.riskservice.engine.RiskEvaluator} pour appliquer
 * les règles de risque différentes selon le genre chez les patients de moins de 30 ans.
 *
 * @since 1.0
 */
public enum Genre {
    /** Homme. */
    M,
    /** Femme. */
    F
}
