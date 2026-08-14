package com.medilabo.patientservice.service;

import com.medilabo.patientservice.dto.PatientRequestDTO;
import com.medilabo.patientservice.dto.PatientResponseDTO;
import com.medilabo.patientservice.entity.Patient;
import com.medilabo.patientservice.exception.PatientNotFoundException;
import com.medilabo.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implémentation par défaut de {@link PatientService}, adossée à {@link PatientRepository}.
 *
 * @since 1.0
 */
@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    /**
     * @param patientRepository accès aux données du dossier patient
     */
    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public List<PatientResponseDTO> findAll() {
        return patientRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public PatientResponseDTO findById(Long id) {
        return toResponseDTO(getPatientOrThrow(id));
    }

    @Override
    public PatientResponseDTO create(PatientRequestDTO request) {
        Patient patient = new Patient(
                request.getPrenom(),
                request.getNom(),
                request.getDateNaissance(),
                request.getGenre(),
                request.getAdresse(),
                request.getTelephone());
        return toResponseDTO(patientRepository.save(patient));
    }

    @Override
    public PatientResponseDTO update(Long id, PatientRequestDTO request) {
        Patient patient = getPatientOrThrow(id);
        patient.setPrenom(request.getPrenom());
        patient.setNom(request.getNom());
        patient.setDateNaissance(request.getDateNaissance());
        patient.setGenre(request.getGenre());
        patient.setAdresse(request.getAdresse());
        patient.setTelephone(request.getTelephone());
        return toResponseDTO(patientRepository.save(patient));
    }

    private Patient getPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    private PatientResponseDTO toResponseDTO(Patient patient) {
        return new PatientResponseDTO(
                patient.getId(),
                patient.getPrenom(),
                patient.getNom(),
                patient.getDateNaissance(),
                patient.getGenre(),
                patient.getAdresse(),
                patient.getTelephone());
    }
}
