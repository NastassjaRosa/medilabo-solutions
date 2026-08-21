package com.medilabo.riskservice.service;

import com.medilabo.riskservice.client.NotesClient;
import com.medilabo.riskservice.client.PatientClient;
import com.medilabo.riskservice.client.dto.Genre;
import com.medilabo.riskservice.client.dto.NoteDTO;
import com.medilabo.riskservice.client.dto.PatientDTO;
import com.medilabo.riskservice.engine.RiskEvaluator;
import com.medilabo.riskservice.engine.RiskLevel;
import com.medilabo.riskservice.exception.DependencyUnavailableException;
import com.medilabo.riskservice.exception.PatientNotFoundException;
import com.medilabo.riskservice.trigger.TriggerCatalogProperties;
import com.medilabo.riskservice.trigger.TriggerDetector;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Vérifie l'orchestration de {@link RiskServiceImpl} : les clients Feign vers
 * patient-service et notes-service sont simulés (Mockito). Point critique testé :
 * une dépendance en panne ne produit jamais {@link RiskLevel#NONE} par défaut,
 * mais une {@link DependencyUnavailableException} explicite (fail-safe).
 */
@ExtendWith(MockitoExtension.class)
class RiskServiceImplTest {

    @Mock
    private PatientClient patientClient;

    @Mock
    private NotesClient notesClient;

    private RiskServiceImpl riskService;

    @BeforeEach
    void setUp() {
        TriggerDetector detector = new TriggerDetector(catalogueDesOnzeDeclencheurs());
        RiskEvaluator evaluator = new RiskEvaluator();
        riskService = new RiskServiceImpl(patientClient, notesClient, detector, evaluator);
    }

    private static TriggerCatalogProperties catalogueDesOnzeDeclencheurs() {
        return new TriggerCatalogProperties(List.of(
                new TriggerCatalogProperties.TriggerDefinition("Anormal", List.of("anormal")),
                new TriggerCatalogProperties.TriggerDefinition("Réaction", List.of("reaction"))
        ));
    }

    private PatientDTO patient(Long id, Genre genre, LocalDate dateNaissance) {
        return new PatientDTO(id, "Test", "TestPatient", dateNaissance, genre, null, null);
    }

    private NoteDTO note(Long patientId, String contenu) {
        return new NoteDTO("note-" + patientId, patientId, contenu, Instant.now());
    }

    @Test
    void patientIntrouvable_levePatientNotFoundException() {
        given(patientClient.getById(99L)).willThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> riskService.evaluateRisk(99L))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void patientServiceIndisponible_leveDependencyUnavailableException_jamaisNone() {
        given(patientClient.getById(1L)).willThrow(mock(FeignException.class));

        assertThatThrownBy(() -> riskService.evaluateRisk(1L))
                .isInstanceOf(DependencyUnavailableException.class)
                .hasMessageContaining("patient-service");
    }

    @Test
    void notesServiceIndisponible_leveDependencyUnavailableException_jamaisNone() {
        given(patientClient.getById(1L)).willReturn(patient(1L, Genre.F, LocalDate.of(1966, 12, 31)));
        given(notesClient.getByPatientId(1L)).willThrow(mock(FeignException.class));

        assertThatThrownBy(() -> riskService.evaluateRisk(1L))
                .isInstanceOf(DependencyUnavailableException.class)
                .hasMessageContaining("notes-service");
    }

    @Test
    void casNominal_calculeLeNiveauDeRisqueAttendu() {
        given(patientClient.getById(2L)).willReturn(patient(2L, Genre.M, LocalDate.of(1945, 6, 24)));
        given(notesClient.getByPatientId(2L)).willReturn(List.of(
                note(2L, "audition anormale"),
                note(2L, "réaction aux médicaments et audition anormale")));

        RiskAssessment result = riskService.evaluateRisk(2L);

        assertThat(result.riskLevel()).isEqualTo(RiskLevel.BORDERLINE);
        assertThat(result.patientId()).isEqualTo(2L);
    }
}
