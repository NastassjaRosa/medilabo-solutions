package com.medilabo.notesservice.service;

import com.medilabo.notesservice.dto.NoteRequestDTO;
import com.medilabo.notesservice.dto.NoteResponseDTO;
import com.medilabo.notesservice.entity.Note;
import com.medilabo.notesservice.exception.NoteNotFoundException;
import com.medilabo.notesservice.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Implémentation par défaut de {@link NoteService}, adossée à {@link NoteRepository}.
 *
 * @since 1.0
 */
@Service
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    /**
     * @param noteRepository accès aux données des notes médicales
     */
    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public List<NoteResponseDTO> findByPatientId(Long patientId) {
        return noteRepository.findByPatientIdOrderByDateCreationAsc(patientId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public NoteResponseDTO findById(String id) {
        return toResponseDTO(getNoteOrThrow(id));
    }

    @Override
    public NoteResponseDTO create(NoteRequestDTO request) {
        Note note = new Note(request.getPatientId(), request.getContenu(), Instant.now());
        return toResponseDTO(noteRepository.save(note));
    }

    private Note getNoteOrThrow(String id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    private NoteResponseDTO toResponseDTO(Note note) {
        return new NoteResponseDTO(
                note.getId(),
                note.getPatientId(),
                note.getContenu(),
                note.getDateCreation());
    }
}
