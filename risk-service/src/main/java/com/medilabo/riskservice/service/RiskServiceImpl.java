package com.medilabo.riskservice.service;

import com.medilabo.riskservice.client.NotesClient;
import com.medilabo.riskservice.client.PatientClient;
import com.medilabo.riskservice.client.dto.NoteDTO;
import com.medilabo.riskservice.client.dto.PatientDTO;
import com.medilabo.riskservice.engine.RiskEvaluator;
import com.medilabo.riskservice.engine.RiskLevel;
import com.medilabo.riskservice.exception.DependencyUnavailableException;
import com.medilabo.riskservice.exception.PatientNotFoundException;
import com.medilabo.riskservice.trigger.TriggerDetector;
import feign.FeignException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

/**
 * Orchestre l'évaluation du risque : récupère le patient et ses notes via les
 * clients Feign, compte les déclencheurs distincts, puis applique les règles
 * âge/genre. Aucun chemin de cette classe ne peut renvoyer un risque "None" en
 * cas de panne d'une dépendance : seul un {@link TriggerDetector} qui trouve
 * effectivement 0 ou 1 déclencheur dans des notes réellement récupérées produit
 * ce niveau (fail-safe, voir {@code SECURITE_TESTS_QUALITE.md} A10).
 *
 * @since 1.0
 */
@Service
public class RiskServiceImpl implements RiskService {

    private final PatientClient patientClient;
    private final NotesClient notesClient;
    private final TriggerDetector triggerDetector;
    private final RiskEvaluator riskEvaluator;

    /**
     * @param patientClient   client REST vers patient-service
     * @param notesClient     client REST vers notes-service
     * @param triggerDetector composant de comptage des déclencheurs distincts
     * @param riskEvaluator   composant d'application des règles âge/genre
     */
    public RiskServiceImpl(PatientClient patientClient, NotesClient notesClient,
                            TriggerDetector triggerDetector, RiskEvaluator riskEvaluator) {
        this.patientClient = patientClient;
        this.notesClient = notesClient;
        this.triggerDetector = triggerDetector;
        this.riskEvaluator = riskEvaluator;
    }

    @Override
    public RiskAssessment evaluateRisk(Long patientId) {
        PatientDTO patient = fetchPatient(patientId);
        List<NoteDTO> notes = fetchNotes(patientId);

        int age = Period.between(patient.dateNaissance(), LocalDate.now()).getYears();
        Set<String> distinctTriggers = triggerDetector.detectDistinctTriggers(notes.stream().map(NoteDTO::contenu).toList());
        RiskLevel riskLevel = riskEvaluator.evaluate(age, patient.genre(), distinctTriggers.size());

        return new RiskAssessment(patient.id(), patient.prenom(), patient.nom(), riskLevel);
    }

    private PatientDTO fetchPatient(Long patientId) {
        try {
            return patientClient.getById(patientId);
        } catch (FeignException.NotFound e) {
            throw new PatientNotFoundException(patientId);
        } catch (FeignException e) {
            throw new DependencyUnavailableException("patient-service", e);
        }
    }

    private List<NoteDTO> fetchNotes(Long patientId) {
        try {
            return notesClient.getByPatientId(patientId);
        } catch (FeignException e) {
            throw new DependencyUnavailableException("notes-service", e);
        }
    }
}
