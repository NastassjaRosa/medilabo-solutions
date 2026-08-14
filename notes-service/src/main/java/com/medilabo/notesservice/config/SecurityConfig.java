package com.medilabo.notesservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security de notes-service : authentification HTTP Basic avec un
 * compte unique lu depuis des variables d'environnement (le même compte que la gateway et
 * le front utilisent déjà pour appeler ce service). Le service ne fait pas confiance à la
 * gateway et vérifie lui-même chaque requête (défense en profondeur), même s'il n'est en
 * pratique joignable que via le réseau Docker interne.
 *
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String authUsername;
    private final String authPassword;

    /**
     * @param authUsername nom du compte unique, fourni par la variable d'environnement
     *                      {@code GATEWAY_AUTH_USERNAME}
     * @param authPassword  mot de passe en clair du compte unique, fourni par la variable
     *                      d'environnement {@code GATEWAY_AUTH_PASSWORD} (jamais stocké en clair,
     *                      encodé via {@link #passwordEncoder()} avant usage)
     */
    public SecurityConfig(@Value("${GATEWAY_AUTH_USERNAME}") String authUsername,
                           @Value("${GATEWAY_AUTH_PASSWORD}") String authPassword) {
        this.authUsername = authUsername;
        this.authPassword = authPassword;
    }

    /**
     * Encodeur de mot de passe utilisé pour hacher le mot de passe du compte unique.
     *
     * @return un encodeur BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Fournit l'unique utilisateur autorisé, construit à partir des variables d'environnement.
     *
     * @return un service d'utilisateurs contenant ce seul compte
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername(authUsername)
                .password(passwordEncoder().encode(authPassword))
                .authorities("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Règles d'accès : deny-by-default, seule {@code /actuator/health} est publique.
     * Authentification HTTP Basic, sans état (chaque requête est authentifiée
     * indépendamment) ; pas de formulaire de connexion ni de CSRF, ce service n'exposant
     * pas de contenu HTML.
     *
     * @param http constructeur de chaîne de sécurité fourni par Spring Security
     * @return la chaîne de filtres de sécurité appliquée à toutes les requêtes
     * @throws Exception si la construction de la chaîne de filtres échoue
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable())
                .build();
    }
}
