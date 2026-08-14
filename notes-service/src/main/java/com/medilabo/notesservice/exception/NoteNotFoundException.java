package com.medilabo.notesservice.exception;

/**
 * Levée lorsqu'une note demandée n'existe pas dans la base.
 *
 * @since 1.0
 */
public class NoteNotFoundException extends RuntimeException {

    /**
     * Construit l'exception pour un identifiant de note introuvable.
     *
     * @param id identifiant recherché
     */
    public NoteNotFoundException(String id) {
        super("Note introuvable pour l'id " + id);
    }
}
