package com.medilabo.frontend.dto;

import java.time.Instant;

/**
 * Note d'observation medicale telle que renvoyee par le notes-service (via la gateway),
 * pour affichage dans l'historique du patient.
 *
 * @since 1.0
 */
public class NoteDTO {

    private String id;
    private Long patientId;
    private String contenu;
    private Instant dateCreation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }
}
