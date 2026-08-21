package com.medilabo.riskservice.trigger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cas limites du comptage des déclencheurs : comptage distinct, insensibilité
 * casse/accents, variantes de genre/nombre.
 */
class TriggerDetectorTest {

    private TriggerDetector detector;

    @BeforeEach
    void setUp() {
        detector = new TriggerDetector(catalogueDesOnzeDeclencheurs());
    }

    private static TriggerCatalogProperties catalogueDesOnzeDeclencheurs() {
        return new TriggerCatalogProperties(List.of(
                new TriggerCatalogProperties.TriggerDefinition("Hémoglobine A1C", List.of("hemoglobine a1c")),
                new TriggerCatalogProperties.TriggerDefinition("Microalbumine", List.of("microalbumine")),
                new TriggerCatalogProperties.TriggerDefinition("Taille", List.of("taille")),
                new TriggerCatalogProperties.TriggerDefinition("Poids", List.of("poids")),
                new TriggerCatalogProperties.TriggerDefinition("Fumeur", List.of("fumeur", "fumeuse")),
                new TriggerCatalogProperties.TriggerDefinition("Anormal", List.of("anormal")),
                new TriggerCatalogProperties.TriggerDefinition("Cholestérol", List.of("cholesterol")),
                new TriggerCatalogProperties.TriggerDefinition("Vertiges", List.of("vertige")),
                new TriggerCatalogProperties.TriggerDefinition("Rechute", List.of("rechute")),
                new TriggerCatalogProperties.TriggerDefinition("Réaction", List.of("reaction")),
                new TriggerCatalogProperties.TriggerDefinition("Anticorps", List.of("anticorps"))
        ));
    }

    @Test
    void comptageDistinct_termeRepeteDansPlusieursNotesCompteUneSeuleFois() {
        Set<String> result = detector.detectDistinctTriggers(List.of(
                "Le patient présente une réaction. Une nouvelle réaction est constatée.",
                "La réaction persiste."));

        assertThat(result).containsExactly("Réaction");
    }

    @Test
    void insensibiliteCasseEtAccents() {
        Set<String> result = detector.detectDistinctTriggers(List.of(
                "HÉMOGLOBINE A1C élevée. CHOLESTEROL au dessus de la normale."));

        assertThat(result).containsExactlyInAnyOrder("Hémoglobine A1C", "Cholestérol");
    }

    @Test
    void varianteFumeurFumeuse_lesDeuxFormesDonnentLeMemeDeclencheurDistinct() {
        assertThat(detector.detectDistinctTriggers(List.of("Le patient est fumeur.")))
                .containsExactly("Fumeur");
        assertThat(detector.detectDistinctTriggers(List.of("La patiente est fumeuse.")))
                .containsExactly("Fumeur");
        assertThat(detector.detectDistinctTriggers(List.of("Il est fumeur.", "Elle est fumeuse.")))
                .containsExactly("Fumeur");
    }

    @Test
    void varianteVertigeVertiges_lesDeuxFormesDonnentLeMemeDeclencheurDistinct() {
        assertThat(detector.detectDistinctTriggers(List.of("Un vertige isolé a été signalé.")))
                .containsExactly("Vertiges");
        assertThat(detector.detectDistinctTriggers(List.of("Des vertiges répétés.")))
                .containsExactly("Vertiges");
    }

    @Test
    void aucunDeclencheur_renvoieUnEnsembleVide() {
        Set<String> result = detector.detectDistinctTriggers(List.of(
                "Le patient déclare qu'il se sent très bien."));

        assertThat(result).isEmpty();
    }

    @Test
    void plusieursNotesDistinctesSontCumuleesDansLeComptage() {
        Set<String> result = detector.detectDistinctTriggers(List.of("Poids normal.", "Taille mesurée."));

        assertThat(result).containsExactlyInAnyOrder("Poids", "Taille");
    }
}
