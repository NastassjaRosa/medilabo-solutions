package com.medilabo.patientservice.dto;

import com.medilabo.patientservice.entity.Genre;

import java.time.LocalDate;

/**
 * Représentation d'un patient renvoyée par l'API, sans exposer l'entité JPA.
 *
 * @since 1.0
 */
public class PatientResponseDTO {

    private final Long id;
    private final String prenom;
    private final String nom;
    private final LocalDate dateNaissance;
    private final Genre genre;
    private final String adresse;
    private final String telephone;

    /**
     * Construit une réponse patient.
     *
     * @param id            identifiant technique du patient
     * @param prenom        prénom du patient
     * @param nom           nom du patient
     * @param dateNaissance date de naissance
     * @param genre         genre administratif
     * @param adresse       adresse postale, peut être {@code null}
     * @param telephone     numéro de téléphone, peut être {@code null}
     */
    public PatientResponseDTO(Long id, String prenom, String nom, LocalDate dateNaissance, Genre genre,
                               String adresse, String telephone) {
        this.id = id;
        this.prenom = prenom;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
        this.genre = genre;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    public Long getId() {
        return id;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNom() {
        return nom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public Genre getGenre() {
        return genre;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }
}
