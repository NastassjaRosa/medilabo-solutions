package com.medilabo.riskservice.engine;

import com.medilabo.riskservice.client.dto.Genre;
import com.medilabo.riskservice.trigger.TriggerCatalogProperties;
import com.medilabo.riskservice.trigger.TriggerDetector;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rejoue les 4 cas de référence du sujet OpenClassrooms ({@code docs/sujet-openclassrooms.md},
 * "Sprint 2 — Notes des médecins" / "Sprint 3 — Risques attendus") avec le texte
 * réel des notes de chaque patient de test, en combinant {@link TriggerDetector} et
 * {@link RiskEvaluator} bout en bout (pas seulement des nombres abstraits), pour
 * garantir que le moteur produit bien les 4 niveaux attendus sur des données réelles.
 *
 * <p>L'âge est calculé dynamiquement depuis la date de naissance (comme le fait
 * {@code RiskServiceImpl}) : la catégorie d'âge (plus/moins de 30 ans) de ces 4
 * patients reste stable sur toute la durée de vie raisonnable de ce projet.</p>
 */
class RiskEngineReferenceCasesTest {

    private final TriggerDetector detector = new TriggerDetector(catalogueDesOnzeDeclencheurs());
    private final RiskEvaluator evaluator = new RiskEvaluator();

    private static TriggerCatalogProperties catalogueDesOnzeDeclencheurs() {
        return new TriggerCatalogProperties(List.of(
                new TriggerCatalogProperties.TriggerDefinition("Hémoglobine A1C", List.of("hemoglobine a1c")),
                new TriggerCatalogProperties.TriggerDefinition("Microalbumine", List.of("microalbumine")),
                new TriggerCatalogProperties.TriggerDefinition("Taille", List.of("taille")),
                new TriggerCatalogProperties.TriggerDefinition("Poids", List.of("poids")),
                // Formes explicites (pas de radical "fum", qui matcherait a tort
                // "parfum"), conformement au catalogue de production (application.yml).
                new TriggerCatalogProperties.TriggerDefinition("Fumeur",
                        List.of("fumeur", "fumeuse", "fume", "fumer", "fumait")),
                new TriggerCatalogProperties.TriggerDefinition("Anormal", List.of("anormal")),
                new TriggerCatalogProperties.TriggerDefinition("Cholestérol", List.of("cholesterol")),
                new TriggerCatalogProperties.TriggerDefinition("Vertiges", List.of("vertige")),
                new TriggerCatalogProperties.TriggerDefinition("Rechute", List.of("rechute")),
                new TriggerCatalogProperties.TriggerDefinition("Réaction", List.of("reaction")),
                new TriggerCatalogProperties.TriggerDefinition("Anticorps", List.of("anticorps"))
        ));
    }

    private RiskLevel evaluerPatient(LocalDate dateNaissance, Genre genre, List<String> notes) {
        int age = Period.between(dateNaissance, LocalDate.now()).getYears();
        Set<String> declencheurs = detector.detectDistinctTriggers(notes);
        return evaluator.evaluate(age, genre, declencheurs.size());
    }

    @Test
    void testNone_unSeulDeclencheur_produitNone() {
        RiskLevel niveau = evaluerPatient(
                LocalDate.of(1966, 12, 31), Genre.F,
                List.of("Le patient déclare qu'il se sent très bien. "
                        + "Poids égal ou inférieur au poids recommandé."));

        assertThat(niveau).isEqualTo(RiskLevel.NONE);
    }

    @Test
    void testBorderline_deuxDeclencheursDistincts_plusDeTrenteAns_produitBorderline() {
        RiskLevel niveau = evaluerPatient(
                LocalDate.of(1945, 6, 24), Genre.M,
                List.of(
                        "Le patient déclare qu'il ressent beaucoup de stress au travail. "
                                + "Il se plaint également que son audition est anormale dernièrement.",
                        "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois. "
                                + "Il remarque également que son audition continue d'être anormale."));

        assertThat(niveau).isEqualTo(RiskLevel.BORDERLINE);
    }

    @Test
    void testInDanger_troisDeclencheursDistincts_hommeMoinsDeTrenteAns_produitInDanger() {
        RiskLevel niveau = evaluerPatient(
                LocalDate.of(2004, 6, 18), Genre.M,
                List.of(
                        "Le patient déclare qu'il fume depuis peu.",
                        "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière. "
                                + "Il se plaint également de crises d'apnée respiratoire anormales. "
                                + "Tests de laboratoire indiquant un taux de cholestérol LDL élevé."));

        assertThat(niveau).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    void testEarlyOnset_huitDeclencheursDistincts_femmeMoinsDeTrenteAns_produitEarlyOnset() {
        List<String> notes = List.of(
                "Le patient déclare qu'il lui est devenu difficile de monter les escaliers. "
                        + "Il se plaint également d'être essoufflé. "
                        + "Tests de laboratoire indiquant que les anticorps sont élevés. "
                        + "Réaction aux médicaments.",
                "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps.",
                "Le patient déclare avoir commencé à fumer depuis peu. "
                        + "Hémoglobine A1C supérieure au niveau recommandé.",
                "Taille, Poids, Cholestérol, Vertige et Réaction.");

        // Comptage littéral sur ce texte, avec le radical "fum" (couvre "fumer") :
        // Anticorps, Réaction, Hémoglobine A1C, Fumeur, Taille, Poids, Cholestérol,
        // Vertiges = 8 déclencheurs distincts, conforme au tableau récapitulatif de
        // docs/STACK_TECHNIQUE.md §7 ("Early onset (8 décl., F <30)").
        Set<String> declencheurs = detector.detectDistinctTriggers(notes);
        assertThat(declencheurs).containsExactlyInAnyOrder(
                "Anticorps", "Réaction", "Hémoglobine A1C", "Fumeur", "Taille", "Poids", "Cholestérol", "Vertiges");

        RiskLevel niveau = evaluerPatient(LocalDate.of(2002, 6, 28), Genre.F, notes);
        assertThat(niveau).isEqualTo(RiskLevel.EARLY_ONSET);
    }
}
