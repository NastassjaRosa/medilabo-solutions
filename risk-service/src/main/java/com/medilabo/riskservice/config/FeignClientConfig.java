package com.medilabo.riskservice.config;

import feign.Request;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * Configuration partagée des clients OpenFeign vers {@code patient-service} et
 * {@code notes-service} : authentification HTTP Basic sortante (même compte unique
 * que celui vérifié en entrée par {@link SecurityConfig}) et timeouts explicites.
 *
 * <p><strong>Volontairement sans {@code @Configuration}</strong> : cette classe est
 * référencée via l'attribut {@code configuration} de {@code @FeignClient}. Spring Cloud
 * OpenFeign l'enregistre dans un contexte enfant dédié à chaque client ; si elle portait
 * {@code @Configuration}, le scan de composants de l'application principale la
 * chargerait aussi globalement et ses beans (interceptor, timeouts) s'appliqueraient
 * alors à tort à d'autres clients HTTP éventuels.</p>
 *
 * @since 1.0
 */
public class FeignClientConfig {

    private final String authUsername;
    private final String authPassword;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;

    /**
     * @param authUsername     compte unique, fourni par {@code GATEWAY_AUTH_USERNAME}
     * @param authPassword     mot de passe du compte unique, fourni par {@code GATEWAY_AUTH_PASSWORD}
     * @param connectTimeoutMs délai maximal de connexion, en millisecondes
     * @param readTimeoutMs    délai maximal de lecture de la réponse, en millisecondes
     */
    public FeignClientConfig(@Value("${GATEWAY_AUTH_USERNAME}") String authUsername,
                              @Value("${GATEWAY_AUTH_PASSWORD}") String authPassword,
                              @Value("${risk.feign.connect-timeout-ms}") long connectTimeoutMs,
                              @Value("${risk.feign.read-timeout-ms}") long readTimeoutMs) {
        this.authUsername = authUsername;
        this.authPassword = authPassword;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * Ajoute l'en-tête {@code Authorization: Basic ...} à chaque appel sortant.
     *
     * @return l'intercepteur d'authentification
     */
    @Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(authUsername, authPassword);
    }

    /**
     * Timeouts explicites de connexion et de lecture, pour ne jamais rester bloqué
     * indéfiniment si une dépendance est indisponible (voir la gestion des pannes
     * dans {@code RiskServiceImpl}, qui ne renvoie jamais silencieusement "None").
     *
     * @return les options de requête Feign
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(connectTimeoutMs, TimeUnit.MILLISECONDS, readTimeoutMs, TimeUnit.MILLISECONDS, true);
    }
}
