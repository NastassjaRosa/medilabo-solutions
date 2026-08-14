package com.medilabo.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Affiche la page de connexion du front. Le traitement du formulaire (POST /login) est pris
 * en charge directement par Spring Security (form login).
 *
 * @since 1.0
 */
@Controller
public class LoginController {

    /**
     * @return le nom de la vue de connexion
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
