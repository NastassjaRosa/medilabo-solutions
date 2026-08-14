package com.medilabo.notesservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

/**
 * Note d'observation médicale rédigée par un praticien (base MongoDB {@code notesdb},
 * collection {@code note}).
 *
 * <p>Le contenu est du texte libre multi-lignes ; le format d'origine (sauts de ligne)
 * est conservé tel quel, sans transformation.</p>
 *
 * @since 1.0
 */
@Document(collection = "note")
public class Note {

    @Id
    private String id;

    private Long patientId;

    private String contenu;

    private Instant dateCreation;

    /** Constructeur requis par Spring Data MongoDB. */
    protected Note() {
    }

    /**
     * Construit une note.
     *
     * @param patientId    identifiant du patient concerné
     * @param contenu      texte libre de la note
     * @param dateCreation date de rédaction de la note
     */
    public Note(Long patientId, String contenu, Instant dateCreation) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Note note)) {
            return false;
        }
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
