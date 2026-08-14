package com.medilabo.frontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;

/**
 * Client REST utilise par le front pour appeler la gateway (chaine API HTTP Basic), avec un
 * compte de service distinct du compte humain utilise pour le login du front.
 *
 * @since 1.0
 */
@Configuration
public class GatewayClientConfig {

    /**
     * @param baseUrl          URL de base de la gateway (ex. {@code http://gateway:8080})
     * @param username         compte de service, fourni par {@code GATEWAY_AUTH_USERNAME}
     * @param password         mot de passe du compte de service, fourni par {@code GATEWAY_AUTH_PASSWORD}
     * @param connectTimeoutMs delai de connexion avant echec explicite (resilience)
     * @param readTimeoutMs    delai de lecture avant echec explicite (resilience)
     * @return le client REST configure pour appeler la gateway
     */
    @Bean
    public RestClient gatewayRestClient(@Value("${app.gateway.base-url}") String baseUrl,
                                         @Value("${GATEWAY_AUTH_USERNAME}") String username,
                                         @Value("${GATEWAY_AUTH_PASSWORD}") String password,
                                         @Value("${app.gateway.connect-timeout-ms}") int connectTimeoutMs,
                                         @Value("${app.gateway.read-timeout-ms}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(new BasicAuthenticationInterceptor(username, password))
                .build();
    }
}
