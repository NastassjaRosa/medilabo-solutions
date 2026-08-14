package com.medilabo.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifie les regles d'authentification de la gateway : deny-by-default sur les routes
 * protegees, acces public uniquement sur {@code /actuator/health}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySecurityTest {

    private static final String AUTH_USERNAME = "test-user";
    private static final String AUTH_PASSWORD = "test-password";

    @Value("${local.server.port}")
    private int port;

    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void authProperties(DynamicPropertyRegistry registry) {
        registry.add("GATEWAY_AUTH_USERNAME", () -> AUTH_USERNAME);
        registry.add("GATEWAY_AUTH_PASSWORD", () -> AUTH_PASSWORD);
    }

    @Test
    void healthEstAccessibleSansAuthentification() {
        client().get()
                .uri("/actuator/health")
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
    void uneRouteProtegeeAvecDeMauvaisIdentifiantsRenvoie401() {
        client().get()
                .uri("/patients")
                .headers(headers -> headers.setBasicAuth(AUTH_USERNAME, "mot-de-passe-incorrect"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void avecDeBonsIdentifiantsLaSecuriteLaisseLaRequeteAtteindreLeRoutage() {
        // Aucune route ne correspond a ce chemin : la reponse 404 (et non 401) prouve que
        // l'authentification a ete acceptee et que la requete a bien atteint le routage,
        // sans dependre d'un appel reseau vers un service aval reellement demarre.
        client().get()
                .uri("/chemin-inconnu")
                .headers(headers -> headers.setBasicAuth(AUTH_USERNAME, AUTH_PASSWORD))
                .exchange()
                .expectStatus().isNotFound();
    }

    private WebTestClient client() {
        if (webTestClient == null) {
            webTestClient = WebTestClient.bindToServer()
                    .baseUrl("http://localhost:" + port)
                    .build();
        }
        return webTestClient;
    }
}
