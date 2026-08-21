package com.medilabo.riskservice.client.dto;

import java.time.LocalDate;

/**
 * Représentation d'un patient telle que renvoyée par {@code GET /patients/{id}}
 * de patient-service (miroir de son {@code PatientResponseDTO}, sans dépendance
 * de compilation entre les deux microservices).
 *
 * @param id            identifiant technique du patient
 * @param prenom        prénom du patient
 * @param nom           nom du patient
 * @param dateNaissance date de naissance, utilisée pour calculer l'âge au moment de l'évaluation
 * @param genre         genre administratif
 * @param adresse       adresse postale, peut être {@code null}
 * @param telephone     numéro de téléphone, peut être {@code null}
 * @since 1.0
 */
public record PatientDTO(Long id, String prenom, String nom, LocalDate dateNaissance, Genre genre,
                          String adresse, String telephone) {
}
