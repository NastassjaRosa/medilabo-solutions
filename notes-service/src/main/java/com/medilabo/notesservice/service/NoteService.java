package com.medilabo.notesservice.service;

import com.medilabo.notesservice.dto.NoteRequestDTO;
import com.medilabo.notesservice.dto.NoteResponseDTO;
import com.medilabo.notesservice.exception.NoteNotFoundException;

import java.util.List;

/**
 * Opérations métier des notes médicales.
 *
 * @since 1.0
 */
public interface NoteService {

    /**
     * Liste les notes d'un patient, pour reconstituer son historique.
     *
     * @param patientId identifiant du patient
     * @return les notes du patient, éventuellement vide
     */
    List<NoteResponseDTO> findByPatientId(Long patientId);

    /**
     * Récupère le détail d'une note.
     *
     * @param id identifiant de la note
     * @return la note trouvée
     * @throws NoteNotFoundException si aucune note ne correspond à l'id
     */
    NoteResponseDTO findById(String id);

    /**
     * Ajoute une note d'observation.
     *
     * @param request données de la note à créer
     * @return la note créée, avec son identifiant généré
     */
    NoteResponseDTO create(NoteRequestDTO request);
}
