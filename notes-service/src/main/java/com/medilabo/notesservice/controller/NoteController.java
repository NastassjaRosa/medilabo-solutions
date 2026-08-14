package com.medilabo.notesservice.controller;

import com.medilabo.notesservice.dto.NoteRequestDTO;
import com.medilabo.notesservice.dto.NoteResponseDTO;
import com.medilabo.notesservice.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expose les notes d'observation médicale : consultation de l'historique d'un
 * patient, détail d'une note et ajout d'une note.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    /**
     * @param noteService opérations métier des notes médicales
     */
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Liste les notes d'un patient, pour afficher son historique.
     *
     * @param patientId identifiant du patient
     * @return les notes du patient
     */
    @GetMapping("/patient/{patientId}")
    public List<NoteResponseDTO> getByPatientId(@PathVariable Long patientId) {
        return noteService.findByPatientId(patientId);
    }

    /**
     * Récupère le détail d'une note.
     *
     * @param id identifiant de la note
     * @return la note correspondante
     */
    @GetMapping("/{id}")
    public NoteResponseDTO getById(@PathVariable String id) {
        return noteService.findById(id);
    }

    /**
     * Ajoute une note d'observation.
     *
     * @param request données de la note à créer
     * @return la note créée
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponseDTO create(@Valid @RequestBody NoteRequestDTO request) {
        return noteService.create(request);
    }
}
