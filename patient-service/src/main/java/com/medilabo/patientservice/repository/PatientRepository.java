package com.medilabo.patientservice.repository;

import com.medilabo.patientservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux données du dossier patient (table {@code patient}).
 *
 * @since 1.0
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {
}