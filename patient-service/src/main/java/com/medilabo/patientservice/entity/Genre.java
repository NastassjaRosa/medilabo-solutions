package com.medilabo.patientservice.entity;

/**
 * Genre administratif d'un patient, utilisé notamment par le calcul du risque
 * de diabète (règles différentes selon le genre pour les patients de moins de 30 ans).
 *
 * @since 1.0
 */
public enum Genre {
    /** Homme. */
    M,
    /** Femme. */
    F
}