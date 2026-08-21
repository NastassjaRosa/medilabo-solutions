package com.medilabo.riskservice.exception;

/**
 * Levée lorsque {@code patient-service} ou {@code notes-service} est indisponible
 * (timeout, connexion refusée, erreur serveur). Traduit en réponse 503 par
 * {@link ApiExceptionHandler} : le risque est alors explicitement <em>indéterminé</em>,
 * jamais assimilé à {@code None} (piège fail-open documenté dans
 * {@code SECURITE_TESTS_QUALITE.md}, catégorie OWASP A10).
 *
 * @since 1.0
 */
public class DependencyUnavailableException extends RuntimeException {

    /**
     * @param serviceName nom du service indisponible (ex. {@code "patient-service"})
     * @param cause       exception d'origine (timeout, erreur réseau, statut HTTP inattendu)
     */
    public DependencyUnavailableException(String serviceName, Throwable cause) {
        super("Risque indéterminé : " + serviceName + " indisponible", cause);
    }
}
