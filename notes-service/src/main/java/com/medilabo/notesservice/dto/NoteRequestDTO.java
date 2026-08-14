package com.medilabo.notesservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Données saisies par le praticien pour ajouter une note d'observation.
 *
 * <p>Le sujet ne fixe pas de limite de taille métier pour le contenu (texte libre,
 * format conservé). {@link #contenu} porte néanmoins une limite technique haute
 * (100 000 caractères) en garde-fou contre une saisie démesurée : voir
 * {@code docs/SECURITE_TESTS_QUALITE.md} §A.8 (résilience, plafond BSON 16 Mo par
 * document MongoDB). Cette limite reste très au-dessus de toute note réelle.</p>
 *
 * @since 1.0
 */
public class NoteRequestDTO {

    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientId;

    @NotBlank(message = "Le contenu de la note est obligatoire")
    @Size(max = 100_000, message = "Le contenu de la note ne doit pas dépasser 100 000 caractères")
    private String contenu;

    /** Constructeur requis pour la désérialisation JSON. */
    public NoteRequestDTO() {
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
}
