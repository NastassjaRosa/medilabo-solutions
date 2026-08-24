package com.medilabo.frontend.controller;

import com.medilabo.frontend.client.NoteGatewayClient;
import com.medilabo.frontend.client.PatientGatewayClient;
import com.medilabo.frontend.client.RiskGatewayClient;
import com.medilabo.frontend.dto.Genre;
import com.medilabo.frontend.dto.NoteFormDTO;
import com.medilabo.frontend.dto.PatientDTO;
import com.medilabo.frontend.exception.GatewayUnavailableException;
import com.medilabo.frontend.exception.GatewayValidationException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Vues du dossier demographique patient : liste, detail, ajout et modification. Communique
 * avec le patient-service via la gateway ({@link PatientGatewayClient}).
 *
 * @since 1.0
 */
@Controller
public class PatientUiController {

    private final PatientGatewayClient patientGatewayClient;
    private final NoteGatewayClient noteGatewayClient;
    private final RiskGatewayClient riskGatewayClient;

    /**
     * @param patientGatewayClient client d'acces au dossier patient via la gateway
     * @param noteGatewayClient    client d'acces a l'historique des notes via la gateway
     * @param riskGatewayClient    client d'acces a l'evaluation du risque via la gateway
     */
    public PatientUiController(PatientGatewayClient patientGatewayClient, NoteGatewayClient noteGatewayClient,
                                RiskGatewayClient riskGatewayClient) {
        this.patientGatewayClient = patientGatewayClient;
        this.noteGatewayClient = noteGatewayClient;
        this.riskGatewayClient = riskGatewayClient;
    }

    /**
     * @return redirection vers la liste des patients
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/patients";
    }

    /**
     * Liste des patients, pour verifier l'identite d'un patient donne.
     *
     * @param model modele de la vue
     * @return la vue liste
     */
    @GetMapping("/patients")
    public String list(Model model) {
        model.addAttribute("patients", patientGatewayClient.findAll());
        return "patients/list";
    }

    /**
     * Detail des informations personnelles d'un patient, avec son historique de notes.
     *
     * @param id    identifiant du patient
     * @param model modele de la vue
     * @return la vue detail
     */
    @GetMapping("/patients/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientGatewayClient.findById(id));
        model.addAttribute("notes", noteGatewayClient.findByPatientId(id));
        model.addAttribute("noteForm", new NoteFormDTO());
        addRiskToModel(id, model);
        return "patients/detail";
    }

    /**
     * Ajoute une note d'observation a l'historique d'un patient.
     *
     * @param id            identifiant du patient concerne
     * @param noteForm      contenu saisi par le praticien
     * @param bindingResult resultat de la validation Bean Validation
     * @param model         modele de la vue
     * @return redirection vers le detail en cas de succes, sinon reaffichage du detail avec l'erreur
     */
    @PostMapping("/patients/{id}/notes")
    public String addNote(@PathVariable Long id, @Valid @ModelAttribute("noteForm") NoteFormDTO noteForm,
                           BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("patient", patientGatewayClient.findById(id));
            model.addAttribute("notes", noteGatewayClient.findByPatientId(id));
            addRiskToModel(id, model);
            return "patients/detail";
        }
        try {
            noteGatewayClient.create(id, noteForm);
            return "redirect:/patients/" + id;
        } catch (GatewayValidationException validation) {
            applyFieldErrors(bindingResult, validation, "noteForm");
            model.addAttribute("patient", patientGatewayClient.findById(id));
            model.addAttribute("notes", noteGatewayClient.findByPatientId(id));
            addRiskToModel(id, model);
            return "patients/detail";
        }
    }

    /**
     * Ajoute le risque evalue au modele de la vue detail, ou un message d'erreur explicite si
     * le risk-service (ou une de ses dependances) est indisponible. Ne jamais laisser cette
     * panne remonter comme erreur globale : le reste de la page (infos patient, notes) doit
     * rester consultable, et le risque ne doit jamais s'afficher a tort comme "None".
     *
     * @param id    identifiant du patient
     * @param model modele de la vue
     */
    private void addRiskToModel(Long id, Model model) {
        try {
            model.addAttribute("risk", riskGatewayClient.findByPatientId(id));
        } catch (GatewayUnavailableException e) {
            model.addAttribute("riskError", "Risque indéterminé : le service d'évaluation est temporairement indisponible.");
        }
    }

    /**
     * Formulaire d'ajout d'un patient.
     *
     * @param model modele de la vue
     * @return la vue formulaire
     */
    @GetMapping("/patients/new")
    public String newForm(Model model) {
        model.addAttribute("patient", new PatientDTO());
        model.addAttribute("genres", Genre.values());
        model.addAttribute("mode", "create");
        return "patients/form";
    }

    /**
     * Soumission du formulaire d'ajout.
     *
     * @param patient       donnees saisies
     * @param bindingResult resultat de la validation Bean Validation
     * @param model         modele de la vue
     * @return redirection vers le detail en cas de succes, sinon reaffichage du formulaire
     */
    @PostMapping("/patients")
    public String create(@Valid @ModelAttribute("patient") PatientDTO patient, BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", Genre.values());
            model.addAttribute("mode", "create");
            return "patients/form";
        }
        try {
            PatientDTO created = patientGatewayClient.create(patient);
            return "redirect:/patients/" + created.getId();
        } catch (GatewayValidationException validation) {
            applyFieldErrors(bindingResult, validation, "patient");
            model.addAttribute("genres", Genre.values());
            model.addAttribute("mode", "create");
            return "patients/form";
        }
    }

    /**
     * Formulaire de modification d'un patient existant.
     *
     * @param id    identifiant du patient a modifier
     * @param model modele de la vue
     * @return la vue formulaire
     */
    @GetMapping("/patients/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientGatewayClient.findById(id));
        model.addAttribute("genres", Genre.values());
        model.addAttribute("mode", "edit");
        return "patients/form";
    }

    /**
     * Soumission du formulaire de modification.
     *
     * @param id            identifiant du patient a modifier
     * @param patient       donnees saisies
     * @param bindingResult resultat de la validation Bean Validation
     * @param model         modele de la vue
     * @return redirection vers le detail en cas de succes, sinon reaffichage du formulaire
     */
    @PostMapping("/patients/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("patient") PatientDTO patient,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", Genre.values());
            model.addAttribute("mode", "edit");
            return "patients/form";
        }
        try {
            patientGatewayClient.update(id, patient);
            return "redirect:/patients/" + id;
        } catch (GatewayValidationException validation) {
            applyFieldErrors(bindingResult, validation, "patient");
            model.addAttribute("genres", Genre.values());
            model.addAttribute("mode", "edit");
            return "patients/form";
        }
    }

    private void applyFieldErrors(BindingResult bindingResult, GatewayValidationException validation,
                                   String objectName) {
        validation.getFieldErrors().forEach((field, message) ->
                bindingResult.addError(new FieldError(objectName, field, message)));
    }
}
