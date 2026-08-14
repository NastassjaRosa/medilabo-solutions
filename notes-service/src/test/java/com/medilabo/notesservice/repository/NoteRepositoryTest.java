package com.medilabo.notesservice.repository;

import com.medilabo.notesservice.entity.Note;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration de {@link NoteRepository} sur un MongoDB embarqué
 * (Flapdoodle), démarré automatiquement par Spring Boot pour ce test.
 */
@DataMongoTest
class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Test
    void save_shouldPersistNoteAndGenerateId() {
        Note note = new Note(1L, "Le patient se sent bien.", Instant.now());

        Note saved = noteRepository.save(note);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_whenNoteExists_shouldReturnIt() {
        Note saved = noteRepository.save(new Note(2L, "Note de suivi.", Instant.now()));

        Optional<Note> found = noteRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getContenu()).isEqualTo("Note de suivi.");
    }

    @Test
    void findById_whenNoteMissing_shouldReturnEmpty() {
        Optional<Note> found = noteRepository.findById("000000000000000000000000");

        assertThat(found).isEmpty();
    }

    @Test
    void findByPatientIdOrderByDateCreationAsc_shouldReturnOnlyMatchingNotesInOrder() {
        Instant base = Instant.now();
        noteRepository.save(new Note(3L, "Note la plus récente.", base.plus(2, ChronoUnit.DAYS)));
        noteRepository.save(new Note(3L, "Note la plus ancienne.", base));
        noteRepository.save(new Note(4L, "Note d'un autre patient.", base));

        List<Note> notes = noteRepository.findByPatientIdOrderByDateCreationAsc(3L);

        assertThat(notes).hasSize(2);
        assertThat(notes.get(0).getContenu()).isEqualTo("Note la plus ancienne.");
        assertThat(notes.get(1).getContenu()).isEqualTo("Note la plus récente.");
    }

    @Test
    void findByPatientIdOrderByDateCreationAsc_whenNoNotes_shouldReturnEmptyList() {
        List<Note> notes = noteRepository.findByPatientIdOrderByDateCreationAsc(-1L);

        assertThat(notes).isEmpty();
    }

    @Test
    void save_shouldPreserveMultilineContenu() {
        Note note = new Note(5L, "Ligne 1\nLigne 2\nLigne 3", Instant.now());

        Note saved = noteRepository.save(note);
        Optional<Note> found = noteRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getContenu()).isEqualTo("Ligne 1\nLigne 2\nLigne 3");
    }
}
