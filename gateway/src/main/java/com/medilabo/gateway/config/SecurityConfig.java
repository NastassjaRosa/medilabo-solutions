package com.medilabo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration Spring Security de la gateway : authentification HTTP Basic avec un
 * compte unique lu depuis des variables d'environnement (aucune inscription, aucune
 * gestion de roles). Chaque route est protegée par defaut, seule la sonde de sante
 * reste accessible sans authentification.
 *
 * @since 1.0
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final String authUsername;
    private final String authPassword;

    /**
     * @param authUsername nom du compte unique, fourni par la variable d'environnement
     *                      {@code GATEWAY_AUTH_USERNAME}
     * @param authPassword  mot de passe en clair du compte unique, fourni par la variable
     *                      d'environnement {@code GATEWAY_AUTH_PASSWORD} (jamais stocke en clair,
     *                      encode via {@link #passwordEncoder()} avant usage)
     */
    public SecurityConfig(@Value("${GATEWAY_AUTH_USERNAME}") String authUsername,
                           @Value("${GATEWAY_AUTH_PASSWORD}") String authPassword) {
        this.authUsername = authUsername;
        this.authPassword = authPassword;
    }

    /**
     * Encodeur de mot de passe utilise pour hacher le mot de passe du compte unique.
     *
     * @return un encodeur BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Fournit l'unique utilisateur autorise, construit a partir des variables d'environnement.
     *
     * @return un service d'utilisateurs reactif contenant ce seul compte
     */
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user = User.withUsername(authUsername)
                .password(passwordEncoder().encode(authPassword))
                .authorities("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }

    /**
     * Regles d'acces : deny-by-default, sauf {@code /actuator/health} et {@code /ui/**}.
     * Authentification HTTP Basic pour la chaine API (patients/notes/risk) ; pas de
     * formulaire de connexion ni de CSRF ici, la gateway ne servant pas de contenu HTML.
     * {@code /ui/**} est laisse public au niveau de la gateway car le front gere lui-meme
     * son authentification humaine (form login + session), conformement a la defense en
     * profondeur : chaque microservice se securise lui-meme plutot que de faire confiance
     * a la gateway.
     *
     * @param http constructeur de chaine de securite reactive fourni par Spring Security
     * @return la chaine de filtres de securite appliquee a toutes les requetes
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/ui/**").permitAll()
                        .anyExchange().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}
