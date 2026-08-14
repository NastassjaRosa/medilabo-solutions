package com.medilabo.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Donnees d'un patient telles que manipulees par le front : affichage (liste/detail) et
 * saisie (ajout/modification). L'{@code id} est absent lors de la creation.
 *
 * <p>Les contraintes de validation reproduisent celles du patient-service (premiere ligne de
 * defense cote formulaire) ; le patient-service reste l'autorite finale sur la validite des
 * donnees.</p>
 *
 * @since 1.0
 */
public class PatientDTO {

    private Long id;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne doit pas dépasser 50 caractères")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne doit pas dépasser 50 caractères")
    private String nom;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateNaissance;

    @NotNull(message = "Le genre est obligatoire")
    private Genre genre;

    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String adresse;

    @Pattern(regexp = "^$|^[0-9 +()-]{7,20}$", message = "Le numéro de téléphone n'a pas un format valide")
    private String telephone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
