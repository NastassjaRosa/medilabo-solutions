package com.medilabo.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Donnees saisies dans le formulaire d'ajout d'une note d'observation. Le
 * {@code patientId} n'est pas saisi : il est pris depuis l'URL par le controleur.
 *
 * @since 1.0
 */
public class NoteFormDTO {

    @NotBlank(message = "Le contenu de la note est obligatoire")
    @Size(max = 100_000, message = "Le contenu de la note ne doit pas dépasser 100 000 caractères")
    private String contenu;

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}
