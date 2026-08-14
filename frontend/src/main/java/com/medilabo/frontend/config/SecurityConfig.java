package com.medilabo.frontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security du front : authentification humaine par formulaire (session +
 * CSRF), avec un compte unique de demonstration (pas d'inscription, pas de gestion de roles,
 * conformement au sujet). Distincte du compte de service utilise par {@link GatewayClientConfig}
 * pour appeler la gateway : le front se securise lui-meme plutot que de faire confiance a la
 * gateway (defense en profondeur), meme si celle-ci laisse deja passer {@code /ui/**}.
 *
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String authUsername;
    private final String authPassword;

    /**
     * @param authUsername identifiant du compte unique, fourni par {@code FRONTEND_AUTH_USERNAME}
     * @param authPassword  mot de passe en clair du compte unique, fourni par
     *                      {@code FRONTEND_AUTH_PASSWORD} (encode via {@link #passwordEncoder()} avant usage)
     */
    public SecurityConfig(@Value("${FRONTEND_AUTH_USERNAME}") String authUsername,
                           @Value("${FRONTEND_AUTH_PASSWORD}") String authPassword) {
        this.authUsername = authUsername;
        this.authPassword = authPassword;
    }

    /**
     * @return un encodeur BCrypt pour le mot de passe du compte unique
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * @return le service d'utilisateurs contenant l'unique compte autorise
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
     * Regles d'acces : deny-by-default, seule la page de connexion et la sonde de sante sont
     * publiques. Authentification par formulaire (session cote serveur), CSRF actif, en-tete
     * Content-Security-Policy restrictif (defense XSS complementaire a l'echappement Thymeleaf).
     *
     * @param http constructeur de chaine de securite fourni par Spring Security
     * @return la chaine de filtres de securite appliquee a toutes les requetes
     * @throws Exception si la construction de la chaine echoue
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/login", "/actuator/health", "/css/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/patients", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")))
                .build();
    }
}
