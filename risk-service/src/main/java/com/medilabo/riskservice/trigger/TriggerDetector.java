package com.medilabo.riskservice.trigger;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Détecte et compte les déclencheurs distincts présents dans un ensemble de notes
 * médicales. Seule responsabilité : le comptage (SRP) — l'application des règles
 * âge/genre est déléguée à {@link com.medilabo.riskservice.engine.RiskEvaluator}.
 *
 * <p>La recherche est insensible à la casse et aux accents, et gère les variantes
 * de genre/nombre (ex. Fumeur/Fumeuse, Vertige/Vertiges) via la liste de formes de
 * surface de chaque {@link TriggerCatalogProperties.TriggerDefinition}. La
 * normalisation utilise une simple suppression de marques diacritiques
 * ({@code \p{M}}), sans regex à backtracking complexe, pour éviter tout risque de
 * ReDoS sur des notes de taille non bornée.</p>
 *
 * @since 1.0
 */
@Component
public class TriggerDetector {

    private final List<TriggerCatalogProperties.TriggerDefinition> triggers;

    /**
     * @param catalog catalogue externalisé des déclencheurs à rechercher
     */
    public TriggerDetector(TriggerCatalogProperties catalog) {
        this.triggers = catalog.triggers();
    }

    /**
     * Compte les déclencheurs distincts présents dans l'ensemble des notes fournies :
     * un déclencheur présent plusieurs fois (dans une même note ou entre plusieurs
     * notes) ne compte qu'une seule fois.
     *
     * @param noteContents contenu texte de chaque note du patient
     * @return les noms des déclencheurs distincts trouvés
     */
    public Set<String> detectDistinctTriggers(List<String> noteContents) {
        String normalizedText = normalize(String.join(" ", noteContents));
        Set<String> found = new LinkedHashSet<>();
        for (TriggerCatalogProperties.TriggerDefinition trigger : triggers) {
            boolean present = trigger.formes().stream()
                    .anyMatch(forme -> normalizedText.contains(normalize(forme)));
            if (present) {
                found.add(trigger.nom());
            }
        }
        return found;
    }

    /**
     * Normalise un texte pour une recherche insensible à la casse et aux accents :
     * minuscules puis suppression des marques diacritiques (forme NFD).
     *
     * @param text texte à normaliser, peut être {@code null}
     * @return le texte normalisé, chaîne vide si {@code text} est {@code null}
     */
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String lower = text.toLowerCase(Locale.FRENCH);
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
    }
}
