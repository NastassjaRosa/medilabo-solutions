# Grille ASVS 5.0 — Niveaux 2 et 3 (filtrée pour MédiLabo Solutions)

> Suite de la grille niveau 1. Exigences **additionnelles** apportées par les niveaux 2 et 3 de l'**OWASP ASVS 5.0.0**, reformulées en français et adaptées au projet.
> Chaque item est tagué **[L2]** ou **[L3]**. Les items déjà couverts au niveau 1 ne sont pas répétés.
>
> Contenu paraphrasé — ASVS est publié sous licence CC BY-SA 4.0. Se référer au standard officiel pour le texte normatif et les identifiants exacts.

---

## À quel niveau viser sur ce projet ?

- **Niveau 1** : socle minimal (grille séparée). À compléter en totalité.
- **Niveau 2** : assurance standard, **recommandé pour toute application manipulant des données sensibles**. Comme le projet traite des **données de type santé**, L2 est le niveau réaliste à viser sur les points critiques (authentification, autorisation, protection des données, journalisation).
- **Niveau 3** : haute assurance, réservé aux systèmes critiques (finance, médical, militaire). **Souvent au-delà du périmètre d'un projet pédagogique** : la plupart des items L3 sont à considérer comme des **améliorations futures** à documenter plutôt qu'à implémenter.

> Certaines exigences L2/L3 dépassent le périmètre **imposé** par le brief (qui exclut l'inscription, la gestion de rôles et le MFA). Ces items sont signalés « (au-delà du périmètre imposé) » : les documenter en **N/A justifié** ou en **amélioration future** est une réponse valable et valorisée en soutenance.

---

## Philosophie de cette grille

Cette grille ne se limite pas au périmètre minimal du projet OpenClassrooms. Elle constitue aussi un **support d'apprentissage** permettant d'appliquer progressivement les bonnes pratiques de sécurité issues d'OWASP ASVS. Certains contrôles pourront être implémentés dès ce projet, d'autres seront documentés comme **objectifs d'amélioration** afin de se rapprocher d'une architecture de production.

Argument de soutenance : le cahier des charges ne demande pas explicitement le chiffrement au repos, la gestion centralisée des secrets ou un modèle de menace. Le choix de s'aligner autant que possible sur l'OWASP ASVS niveau 2 est **volontaire** — il sert à utiliser ce projet comme exercice de développement sécurisé et de formation aux pratiques professionnelles.

> **Nota sur les données.** L'application manipule des **données de type santé simulées** (données fictives dans le cadre du projet). Le modèle métier est celui de données médicales, mais le projet **n'est pas un dispositif médical réel** et n'est pas soumis au cadre réglementaire correspondant. Dans toute la grille, « données sensibles / de type santé » renvoie à ces données simulées.

### Légende de lecture

Chaque item porte son niveau ASVS (**[L2]** ou **[L3]**). Pour ce projet, les lire ainsi :

- **[L2]** — objectif de sécurité recommandé, à viser/implémenter autant que possible dans le projet.
- **[L3]** — assurance élevée : **référentiel d'amélioration** pour une mise en production réelle (généralement non implémenté dans le cadre pédagogique).
- **(au-delà du périmètre imposé)** — dépasse le cahier des charges ; démarche volontaire, à documenter.
- **(N/A justifié)** — non applicable au projet ; à documenter avec sa justification.

---

## 1. Encodage & prévention des injections

- [ ] **[L2]** Encodage de sortie vérifié pour **chaque type de contexte** effectivement utilisé (HTML, attribut, JS, URL, CSS)
- [ ] **[L2]** Parametrisation/ORM imposée sur **tout** le code d'accès aux données (revue systématique, pas seulement les cas évidents)
- [ ] **[L2]** Protection contre l'**injection de template** (Thymeleaf : ne jamais construire d'expression depuis une entrée)
- [ ] **[L3]** Sanitisation centralisée et testée pour tout cas où du contenu semi-structuré serait rendu (aucun ici en principe : documenter en N/A)

## 2. Validation & logique métier

- [ ] **[L2]** Les **règles métier** (séquencement, cohérence) sont validées côté serveur : ex. calcul de risque déclenché dans un ordre valide, sur des données cohérentes
- [ ] **[L2]** **Anti-automatisation** sur les fonctions sensibles (limitation du débit de requêtes, protection contre l'usage abusif)
- [ ] **[L2]** Limites métier explicites (taille des notes, nombre d'éléments retournés) documentées et testées
- [ ] **[L3]** Les flux métier sensibles disposent d'un **modèle de menace documenté** et sont testés contre l'abus

## 3. Sécurité front (navigateur)

- [ ] **[L2]** CSP renforcée (basée sur **nonce/hash**), pas de `unsafe-inline` pour les scripts
- [ ] **[L2]** **Sous-resource Integrity (SRI)** sur les ressources externes chargées (si CDN utilisé)
- [ ] **[L2]** `Referrer-Policy` restrictive ; aucune donnée sensible dans le DOM ou le stockage navigateur
- [ ] **[L3]** CSP stricte sans `unsafe-inline`/`unsafe-eval` + **Trusted Types** activés

## 4. API & services web

- [ ] **[L2]** Requêtes **et** réponses validées contre un **schéma** (contrat OpenAPI appliqué via un validateur de contrat lorsqu'il est en place, pas seulement documenté)
- [ ] **[L2]** **Rate limiting par client** au niveau de la gateway
- [ ] **[L2]** Autorisation cohérente appliquée **à la fois** à la gateway et dans chaque service (défense en profondeur)
- [ ] **[L3]** **mTLS** (TLS mutuel) entre microservices ; requêtes internes authentifiées de service à service

## 5. Authentification

- [ ] **[L2]** Politique de mot de passe alignée sur les recommandations récentes (longueur, **vérification contre les mots de passe compromis**)
- [ ] **[L2]** **Verrouillage de compte** avec seuils et temporisation adaptés (anti credential-stuffing)
- [ ] **[L2]** (au-delà du périmètre imposé) Le brief exclut l'inscription/récupération : si ajoutées un jour, sécuriser ces flux (pas d'énumération, jetons à usage unique et expirants)
- [ ] **[L3]** (au-delà du périmètre imposé) **MFA** / authentification résistante au phishing pour l'accès aux données de type santé

## 6. Gestion de session

- [ ] **[L2]** **Timeout absolu** de session (durée de vie maximale), en plus du timeout d'inactivité
- [ ] **[L2]** Contrôle des **sessions concurrentes** (détection/limitation)
- [ ] **[L2]** Ré-authentification exigée avant une action particulièrement sensible (si applicable)
- [ ] **[L3]** Durée de vie de session courte + ré-authentification pour toute opération à fort impact

## 7. Autorisation / contrôle d'accès

- [ ] **[L2]** Contrôle d'accès **centralisé** et appliqué au niveau service (pas dupliqué de façon incohérente)
- [ ] **[L2]** **Principe du moindre privilège** appliqué aux comptes applicatifs (DB, service)
- [ ] **[L2]** Les **décisions d'accès aux données patients** sont journalisées (traçabilité santé)
- [ ] **[L3]** (au-delà du périmètre imposé) **Séparation des tâches** / autorisation multi-acteurs pour les opérations les plus sensibles

## 8. Cryptographie

- [ ] **[L2]** **Inventaire cryptographique** documenté (où et comment le chiffrement/hachage est utilisé)
- [ ] **[L2]** **Chiffrement au repos** des données sensibles — objectif recommandé par ASVS ; le moyen (chiffrement de la base, du disque ou équivalent) est laissé à l'architecture, à implémenter lorsque l'infrastructure le permet
- [ ] **[L2]** Secrets gérés via un **coffre/gestionnaire de secrets** (pas seulement des variables d'environnement en clair)
- [ ] **[L2]** Algorithmes conformes à **l'état de l'art et aux recommandations actuelles**
- [ ] **[L3]** Clés gérées par **KMS/HSM** (hors périmètre du projet) ; **crypto-agilité** (rotation/changement d'algorithme sans refonte)

## 9. Communication sécurisée

- [ ] **[L2]** **TLS 1.2+ partout en production**, y compris les communications **internes** entre services (en local Docker, le trafic inter-conteneurs n'est généralement pas chiffré — à documenter)
- [ ] **[L2]** **HSTS** activé ; validation stricte des certificats
- [ ] **[L3]** **mTLS** entre tous les microservices ; aucun trafic interne en clair

## 10. Configuration & durcissement

- [ ] **[L2]** **SBOM** (nomenclature logicielle) générée ; dépendances vérifiées en intégrité
- [ ] **[L2]** **Scan d'images conteneur** intégré à la CI ; aucun mode debug en production
- [ ] **[L2]** Comptes de service à **privilèges minimaux** ; en-têtes de sécurité vérifiés automatiquement
- [ ] **[L2]** Gestion des secrets par un outil dédié (coffre) plutôt qu'en clair dans l'environnement
- [ ] **[L3]** **Builds reproductibles** ; **artefacts signés** (ex. Sigstore) ; durcissement runtime (système de fichiers en lecture seule, `seccomp`) ; **politiques réseau** entre conteneurs

## 11. Protection des données (contexte santé / RGPD)

- [ ] **[L2]** Les **données sensibles sont protégées au repos** (chiffrement de la base, du disque ou mécanisme équivalent) — objectif recommandé, moyen laissé à l'architecture
- [ ] **[L2]** **Classification** des données et politique de **rétention/suppression** (conformité RGPD)
- [ ] **[L2]** **Minimisation** appliquée (ne collecter/renvoyer que le nécessaire) et vérifiée
- [ ] **[L2]** Données réelles **masquées/anonymisées** dans les environnements hors production
- [ ] **[L3]** **Chiffrement au niveau du champ** pour les données les plus sensibles ; contrôle de résidence des données

## 12. Secure coding & architecture

- [ ] **[L2]** **Modèle de menace documenté** (STRIDE) avec frontières de confiance identifiées
- [ ] **[L2]** Dépendances issues de **sources fiables** avec **vérification d'intégrité** (anti supply-chain / slopsquatting)
- [ ] **[L2]** **SAST recommandé** ; **DAST** si une infrastructure de test est disponible — intégrés à la CI
- [ ] **[L3]** Modèle de menace **maintenu à jour** ; revue d'architecture de sécurité ; **tous** les appels inter-services authentifiés (défense en profondeur)

## 13. Journalisation & gestion des erreurs

- [ ] **[L2]** **Journalisation centralisée** des événements de sécurité, avec **alerte** sur les événements notables
- [ ] **[L2]** Horloges synchronisées (**NTP**) pour l'horodatage fiable des logs
- [ ] **[L2]** Rétention des logs alignée sur le contexte santé/RGPD ; absence de données sensibles dans les logs **vérifiée**
- [ ] **[L3]** **Intégrité des logs** protégée (inviolabilité) ; intégration **SIEM** ; alerte en temps réel

---

## (Optionnel) Self-contained tokens — uniquement si JWT

- [ ] **[L2]** Durée de vie courte + mécanisme de **révocation** effectif
- [ ] **[L2]** Clés de signature **gérées et rotables** (via coffre/KMS)
- [ ] **[L3]** Rotation automatique des clés ; validation stricte de toutes les revendications (`iss`, `aud`, `exp`, `nbf`)

---

## Suivi (résumé)

| Chapitre | Items L2 | Items L3 |
|----------|:--------:|:--------:|
| 1. Encodage & injections | 3 | 1 |
| 2. Validation & logique | 3 | 1 |
| 3. Sécurité front | 3 | 1 |
| 4. API & services | 3 | 1 |
| 5. Authentification | 3 | 1 |
| 6. Session | 3 | 1 |
| 7. Autorisation | 3 | 1 |
| 8. Cryptographie | 4 | 1 |
| 9. Communication | 2 | 1 |
| 10. Configuration | 4 | 1 |
| 11. Protection des données | 4 | 1 |
| 12. Secure coding | 3 | 1 |
| 13. Logging & erreurs | 3 | 1 |
| **Total** | **41** | **13** |

---

## Recommandation de priorisation pour ce projet

1. **Compléter L1 en totalité** (socle non négociable).
2. **Cibler L2 sur 4 domaines** vu le contexte santé : Authentification (5), Autorisation (7), Protection des données (11), Journalisation (13).
3. **Documenter les autres items L2** en « fait / partiel / amélioration future ».
4. **L3** : à mentionner en soutenance comme **trajectoire** (ce qu'il faudrait pour une mise en production réelle en environnement de type santé), pas comme livrable attendu.

---

## Tableau de maturité

| Niveau | Objectif pour ce projet |
|--------|--------------------------|
| **L1** | 100 % des exigences applicables satisfaites |
| **L2** | Mise en œuvre sur les domaines sensibles : authentification, autorisation, protection des données, journalisation |
| **L3** | Référentiel d'amélioration pour une mise en production réelle |

---

*Adapté de l'OWASP ASVS 5.0.0 (CC BY-SA 4.0). Reformulé et filtré pour le projet ; consulter le standard officiel (owasp.org) pour le texte normatif et les identifiants exacts.*
