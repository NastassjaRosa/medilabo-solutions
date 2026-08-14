package com.medilabo.notesservice.dto;

import java.time.Instant;

/**
 * Représentation d'une note renvoyée par l'API, sans exposer l'entité Mongo.
 *
 * @since 1.0
 */
public class NoteResponseDTO {

    private final String id;
    private final Long patientId;
    private final String contenu;
    private final Instant dateCreation;

    /**
     * Construit une réponse note.
     *
     * @param id            identifiant technique de la note
     * @param patientId     identifiant du patient concerné
     * @param contenu       texte libre de la note
     * @param dateCreation  date de rédaction de la note
     */
    public NoteResponseDTO(String id, Long patientId, String contenu, Instant dateCreation) {
        this.id = id;
        this.patientId = patientId;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
    }

    public String getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getContenu() {
        return contenu;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }
}
