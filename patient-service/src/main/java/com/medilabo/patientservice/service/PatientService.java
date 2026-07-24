package com.medilabo.patientservice.service;

import com.medilabo.patientservice.dto.PatientRequestDTO;
import com.medilabo.patientservice.dto.PatientResponseDTO;
import com.medilabo.patientservice.exception.PatientNotFoundException;

import java.util.List;

/**
 * Opérations métier du dossier démographique patient.
 *
 * @since 1.0
 */
public interface PatientService {

    /**
     * Liste tous les patients.
     *
     * @return la liste des patients, éventuellement vide
     */
    List<PatientResponseDTO> findAll();

    /**
     * Récupère un patient par son identifiant, pour en vérifier l'identité.
     *
     * @param id identifiant du patient
     * @return le patient trouvé
     * @throws PatientNotFoundException si aucun patient ne correspond à l'id
     */
    PatientResponseDTO findById(Long id);

    /**
     * Ajoute un nouveau patient.
     *
     * @param request données du patient à créer
     * @return le patient créé, avec son identifiant généré
     */
    PatientResponseDTO create(PatientRequestDTO request);

    /**
     * Met à jour les informations personnelles d'un patient existant.
     *
     * @param id      identifiant du patient à mettre à jour
     * @param request nouvelles données du patient
     * @return le patient mis à jour
     * @throws PatientNotFoundException si aucun patient ne correspond à l'id
     */
    PatientResponseDTO update(Long id, PatientRequestDTO request);
}
