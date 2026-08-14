package com.medilabo.notesservice.config;

import com.medilabo.notesservice.entity.Note;
import com.medilabo.notesservice.repository.NoteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Alimente la collection {@code note} avec le jeu de données de test du sujet
 * OpenClassrooms (docs/sujet-openclassrooms.md, §Sprint 2), au démarrage et
 * uniquement si la collection est vide (idempotent, équivalent du
 * {@code INSERT IGNORE} utilisé par {@code patient-service}).
 *
 * <p>Le sujet ne fournit pas de date pour chaque note : des dates fixes et
 * croissantes sont utilisées à titre indicatif, sans portée métier.</p>
 *
 * @since 1.0
 */
@Component
public class NoteDataInitializer implements CommandLineRunner {

    private final NoteRepository noteRepository;

    /**
     * @param noteRepository accès aux données des notes médicales
     */
    public NoteDataInitializer(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public void run(String... args) {
        if (noteRepository.count() > 0) {
            return;
        }

        Instant base = Instant.parse("2026-01-01T09:00:00Z");
        List<Note> notes = List.of(
                new Note(1L, "Le patient déclare qu'il se sent très bien. Poids égal ou inférieur au poids recommandé.",
                        base),

                new Note(2L, "Le patient déclare qu'il ressent beaucoup de stress au travail. Il se plaint également "
                        + "que son audition est anormale dernièrement.", base.plus(1, ChronoUnit.DAYS)),
                new Note(2L, "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois. "
                        + "Il remarque également que son audition continue d'être anormale.", base.plus(30, ChronoUnit.DAYS)),

                new Note(3L, "Le patient déclare qu'il fume depuis peu.", base.plus(1, ChronoUnit.DAYS)),
                new Note(3L, "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière. Il se "
                        + "plaint également de crises d'apnée respiratoire anormales. Tests de laboratoire indiquant "
                        + "un taux de cholestérol LDL élevé.", base.plus(30, ChronoUnit.DAYS)),

                new Note(4L, "Le patient déclare qu'il lui est devenu difficile de monter les escaliers. Il se plaint "
                        + "également d'être essoufflé. Tests de laboratoire indiquant que les anticorps sont élevés. "
                        + "Réaction aux médicaments.", base.plus(1, ChronoUnit.DAYS)),
                new Note(4L, "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps.",
                        base.plus(15, ChronoUnit.DAYS)),
                new Note(4L, "Le patient déclare avoir commencé à fumer depuis peu. Hémoglobine A1C supérieure au "
                        + "niveau recommandé.", base.plus(30, ChronoUnit.DAYS)),
                new Note(4L, "Taille, Poids, Cholestérol, Vertige et Réaction.", base.plus(45, ChronoUnit.DAYS)));

        noteRepository.saveAll(notes);
    }
}
