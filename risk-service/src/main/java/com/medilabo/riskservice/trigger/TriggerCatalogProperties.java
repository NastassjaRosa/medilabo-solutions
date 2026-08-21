package com.medilabo.riskservice.trigger;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Liste des déclencheurs recherchés dans les notes médicales, externalisée en
 * configuration ({@code risk.triggers} dans {@code application.yml}) plutôt que
 * codée en dur dans {@link TriggerDetector} : ajouter ou modifier un déclencheur
 * ne demande aucun changement de code (principe Open/Closed).
 *
 * @param triggers déclencheurs configurés, chacun avec son nom affiché et ses
 *                  formes de surface normalisées (minuscules, sans accents) à
 *                  rechercher comme sous-chaînes
 * @since 1.0
 */
@ConfigurationProperties(prefix = "risk")
public record TriggerCatalogProperties(List<TriggerDefinition> triggers) {

    /**
     * Un déclencheur et ses variantes textuelles (ex. Fumeur/Fumeuse).
     *
     * @param nom    nom du déclencheur, tel qu'il apparaît dans les résultats
     * @param formes formes de surface normalisées à rechercher (au moins une)
     */
    public record TriggerDefinition(String nom, List<String> formes) {
    }
}
