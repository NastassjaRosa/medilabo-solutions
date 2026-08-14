package com.medilabo.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifie les regles d'autorisation de {@link SecurityConfig} en isolation (sans demarrer
 * l'application ni router reellement vers un service aval). Un test bout-en-bout via
 * {@code WebTestClient} sur un serveur demarre serait fragile pour {@code /ui/**} tant que le
 * frontend n'est pas demarre dans ce module (resolution DNS/connexion reseau reelle) ; ce test
 * isole donc uniquement la chaine de filtres de securite, exactement comme prevu pour tester un
 * {@link SecurityWebFilterChain} en unite.
 */
class SecurityConfigUnitTest {

    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";

    private final SecurityConfig securityConfig = new SecurityConfig(USERNAME, PASSWORD);

    private WebTestClient client() {
        // Hors contexte Spring, l'auto-configuration ne fournit pas de ReactiveAuthenticationManager
        // a partir du UserDetailsService : on le construit manuellement pour ce test isole.
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(securityConfig.userDetailsService());
        authenticationManager.setPasswordEncoder(securityConfig.passwordEncoder());

        ServerHttpSecurity http = ServerHttpSecurity.http()
                .authenticationManager((ReactiveAuthenticationManager) authenticationManager);
        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);
        return WebTestClient.bindToWebHandler(exchange -> exchange.getResponse().setComplete())
                .webFilter(new WebFilterChainProxy(chain))
                .build();
    }

    @Test
    void healthEstAccessibleSansAuthentification() {
        client().get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void uiEstAccessibleSansAuthentification() {
        // Le front gere sa propre authentification (form login + session) : la gateway ne
        // doit plus exiger de HTTP Basic sur /ui/**.
        client().get()
                .uri("/ui/patients")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void uneRouteProtegeeSansAuthentificationRenvoie401() {
        client().get()
                .uri("/patients")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void uneRouteProtegeeAvecDeBonsIdentifiantsRenvoie200() {
        client().get()
                .uri("/patients")
                .headers(headers -> headers.setBasicAuth(USERNAME, PASSWORD))
                .exchange()
                .expectStatus().isOk();
    }
}
