package com.medilabo.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifie que la configuration de routage declarative (application.yml) expose bien les
 * quatre routes attendues, chacune avec le predicat de chemin et la destination Docker
 * correspondants.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "GATEWAY_AUTH_USERNAME=test-user",
        "GATEWAY_AUTH_PASSWORD=test-password"
})
class GatewayRoutingTest {

    @Autowired
    private GatewayProperties gatewayProperties;

    @Test
    void lesQuatreRoutesAttenduesSontConfigurees() {
        Map<String, RouteDefinition> routesParId = gatewayProperties.getRoutes().stream()
                .collect(java.util.stream.Collectors.toMap(RouteDefinition::getId, r -> r));

        assertThat(routesParId).containsOnlyKeys("patient-service", "notes-service", "risk-service", "frontend");

        assertRoute(routesParId, "patient-service", "http://patient-service:8080", "/patients/**");
        assertRoute(routesParId, "notes-service", "http://notes-service:8080", "/notes/**");
        assertRoute(routesParId, "risk-service", "http://risk-service:8080", "/risk/**");
        assertRoute(routesParId, "frontend", "http://frontend:8080", "/ui/**");
    }

    private void assertRoute(Map<String, RouteDefinition> routes, String id, String expectedUri, String expectedPathPattern) {
        RouteDefinition route = routes.get(id);
        assertThat(route.getUri()).isEqualTo(URI.create(expectedUri));

        List<String> predicateArgs = route.getPredicates().stream()
                .filter(p -> "Path".equals(p.getName()))
                .flatMap(p -> p.getArgs().values().stream())
                .toList();
        assertThat(predicateArgs).contains(expectedPathPattern);
    }
}
