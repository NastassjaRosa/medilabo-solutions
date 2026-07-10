# CLAUDE.md — MédiLabo Solutions

Instructions de projet lues automatiquement par Claude Code au début de chaque tâche.

## Contexte du projet

Application de dépistage du risque de diabète de type 2, en **architecture microservices** (Spring Boot).
Projet OpenClassrooms, livré en 3 sprints. Stack : Java 17, Maven, Spring Boot 3.3.x, Spring Cloud 2023.0.x, MySQL 8, MongoDB 7, Thymeleaf, Docker.

## Documentation de référence (à consulter selon la tâche)

Lire le fichier pertinent **avant** de coder la partie concernée (les ouvrir par leur chemin plutôt que de tout charger) :

- `docs/sujet-openclassrooms.md` — cahier des charges et user stories (source de vérité fonctionnelle).
- `docs/STACK_TECHNIQUE.md` — architecture, microservices, endpoints, modèle de données, diagrammes.
- `docs/SECURITE_TESTS_QUALITE.md` — exigences de sécurité (OWASP), tests, KISS/SOLID.
- `docs/ASVS_L1_CHECKLIST.md` et `docs/ASVS_L2_L3_CHECKLIST.md` — grilles de vérification sécurité.
- `docs/diagrams/` — diagrammes Mermaid (architecture, séquence, classes, ERD, déploiement).

## Structure (monorepo)

Ce dépôt est un **mono-repository** contenant tous les microservices. Chaque service est un module Spring Boot autonome, avec son propre `pom.xml`, `Dockerfile`, et ses tests.

```
medilabo-solutions/
├── gateway/            # Spring Cloud Gateway (seul point exposé)
├── patient-service/    # CRUD patient, MySQL (base 3NF)
├── notes-service/      # notes médicales, MongoDB
├── risk-service/       # évaluation du risque, sans base (appelle patient + notes)
├── frontend/           # UI Thymeleaf
├── docs/               # documentation de référence + sujet + diagrammes
├── docker-compose.yml  # orchestration (services + bases + réseau)
├── .claude/settings.json
└── CLAUDE.md
```

Règles monorepo :
- Ne pas mélanger le code de deux services ; chaque changement reste dans le module concerné.
- Modifier `docker-compose.yml` uniquement quand un service, une base ou le réseau change.
- Le service `risk` appelle `patient` et `notes` **directement** (réseau Docker), pas via la gateway.

## Workflow Git : travailler en pull request

- **Ne jamais committer directement sur `main`.**
- Pour chaque tâche : créer une branche `feature/<description-courte>` (ou `fix/...`), committer dessus, puis ouvrir une **pull request** vers `main` via la CLI GitHub (`gh`).
- La PR doit avoir un titre clair et une description « quoi / pourquoi », et lister les points de test.
- Un commit = un changement cohérent. Format **Conventional Commits** : `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.

## Conventions de commit

- **Ne jamais ajouter de ligne `Co-Authored-By`** ni de mention « Generated with Claude Code » dans les messages de commit, les descriptions de PR ou toute métadonnée git.
- Messages en français, à l'impératif, concis, avec un corps explicatif si le changement est non trivial.

## Conventions de code : Javadoc

- **Javadoc obligatoire sur toute API publique** : classes, interfaces, méthodes et champs publics.
- Décrire l'intention (le « pourquoi »), pas la paraphrase du code.
- Utiliser les tags standard : `@param`, `@return`, `@throws`, et `@since` sur les nouvelles classes.
- Documenter en français (cohérent avec le reste du projet), phrases complètes.
- Pas de commentaire redondant sur du code évident ; commenter la logique métier non triviale (ex. règles de calcul du risque).
- Exemple attendu :

```java
/**
 * Évalue le niveau de risque de diabète d'un patient.
 *
 * @param patient les données démographiques (âge, genre)
 * @param notes   les notes médicales à analyser
 * @return le niveau de risque calculé (None, Borderline, In Danger, Early onset)
 * @throws ServiceUnavailableException si une dépendance est indisponible
 */
public RiskLevel evaluate(Patient patient, List<Note> notes) { ... }
```

## Sécurité : ne jamais diffuser d'information sensible

- **Ne jamais lire, afficher, logger ou committer** de secrets : fichiers `.env`, mots de passe de base, clés, `application-secret*`. (Renforcé par les règles `deny` de `.claude/settings.json`.)
- Toujours utiliser des **variables d'environnement** / secrets Docker pour les identifiants ; ne jamais mettre de valeur en dur dans le code ou dans `application.yml` versionné.
- Vérifier que `.gitignore` couvre `.env`, `*.env`, `**/application-secret*`, dumps de base, avant tout commit.
- Ne pas mettre de **données patient** en clair dans les logs.
- Respecter les exigences de `docs/SECURITE_TESTS_QUALITE.md` (validation des entrées, DTO, échappement Thymeleaf `th:text`, etc.).

## Rappels de style

- Respecter KISS et SOLID (voir doc sécurité).
- Écrire les tests avec le code (JUnit 5, Mockito) ; prioriser le moteur de calcul du risque.
- Documenter les endpoints via OpenAPI (springdoc).
