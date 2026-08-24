package com.medilabo.frontend.dto;

/**
 * Niveau de risque de diabete d'un patient, tel que renvoye par le risk-service (via la
 * gateway). Le libelle ({@code riskLevel}) est l'un de : None, Borderline, In Danger,
 * Early onset.
 *
 * @since 1.0
 */
public class RiskDTO {

    private Long patientId;
    private String prenom;
    private String nom;
    private String riskLevel;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
