package com.medilabo.notesservice.repository;

import com.medilabo.notesservice.entity.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Accès aux données des notes médicales (collection {@code note}).
 *
 * @since 1.0
 */
public interface NoteRepository extends MongoRepository<Note, String> {

    /**
     * Recherche les notes d'un patient, pour reconstituer son historique.
     *
     * @param patientId identifiant du patient
     * @return les notes du patient, triées de la plus ancienne à la plus récente
     */
    List<Note> findByPatientIdOrderByDateCreationAsc(Long patientId);
}
