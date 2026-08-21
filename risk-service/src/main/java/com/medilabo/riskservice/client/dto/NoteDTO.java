package com.medilabo.riskservice.client.dto;

import java.time.Instant;

/**
 * Représentation d'une note médicale telle que renvoyée par
 * {@code GET /notes/patient/{patientId}} de notes-service (miroir de son
 * {@code NoteResponseDTO}, sans dépendance de compilation entre microservices).
 *
 * @param id           identifiant technique de la note
 * @param patientId    identifiant du patient concerné
 * @param contenu      texte libre de la note, analysé par {@link com.medilabo.riskservice.trigger.TriggerDetector}
 * @param dateCreation date de rédaction de la note
 * @since 1.0
 */
public record NoteDTO(String id, Long patientId, String contenu, Instant dateCreation) {
}
