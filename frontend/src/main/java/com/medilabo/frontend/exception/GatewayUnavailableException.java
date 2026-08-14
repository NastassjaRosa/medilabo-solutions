package com.medilabo.frontend.exception;

/**
 * Levee lorsque la gateway ou un service en aval est indisponible, en timeout, ou renvoie
 * une erreur inattendue (5xx). Ne doit jamais etre confondue avec une reponse metier vide :
 * l'appelant doit afficher une erreur explicite plutot que de masquer la panne.
 *
 * @since 1.0
 */
public class GatewayUnavailableException extends RuntimeException {

    /**
     * @param message description de l'echec, destinee aux journaux (pas au client)
     * @param cause   cause technique d'origine
     */
    public GatewayUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
