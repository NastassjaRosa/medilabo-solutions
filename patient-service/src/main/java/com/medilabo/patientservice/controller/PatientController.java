package com.medilabo.patientservice.controller;

import com.medilabo.patientservice.dto.PatientRequestDTO;
import com.medilabo.patientservice.dto.PatientResponseDTO;
import com.medilabo.patientservice.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expose le dossier démographique patient : consultation, création et mise à
 * jour des informations personnelles.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    /**
     * @param patientService opérations métier du dossier patient
     */
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Liste tous les patients, pour vérifier l'identité d'un patient donné.
     *
     * @return la liste des patients
     */
    @GetMapping
    public List<PatientResponseDTO> getAll() {
        return patientService.findAll();
    }

    /**
     * Récupère les informations personnelles d'un patient.
     *
     * @param id identifiant du patient
     * @return le patient correspondant
     */
    @GetMapping("/{id}")
    public PatientResponseDTO getById(@PathVariable Long id) {
        return patientService.findById(id);
    }

    /**
     * Ajoute un patient.
     *
     * @param request informations personnelles du patient à créer
     * @return le patient créé
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponseDTO create(@Valid @RequestBody PatientRequestDTO request) {
        return patientService.create(request);
    }

    /**
     * Met à jour les informations personnelles d'un patient.
     *
     * @param id      identifiant du patient à mettre à jour
     * @param request nouvelles informations personnelles
     * @return le patient mis à jour
     */
    @PutMapping("/{id}")
    public PatientResponseDTO update(@PathVariable Long id, @Valid @RequestBody PatientRequestDTO request) {
        return patientService.update(id, request);
    }
}
