# Sécurité, Tests & Qualité — MédiLabo Solutions

> Référence pour réaliser l'application **de manière sécurisée, testée et maintenable**.
> Basé sur l'**OWASP Top 10:2025** (édition en vigueur depuis nov. 2025 / finalisée janv. 2026) + failles spécifiques à la stack (Spring Security, JPA, MongoDB, Thymeleaf, Docker, gateway) + principes **KISS** et **SOLID**.
>
> **Le Top 10 est un document de *sensibilisation*, pas une checklist exhaustive.** Chaque catégorie regroupe des dizaines de CWE et de nombreuses failles concrètes n'y figurent pas en tant que telles. Les sections A.4 à A.8 complètent la couverture (API, vulnérabilités classiques, risques du code assisté par IA, pièges propres au projet).

---

## Sommaire

- [Cadre de lecture — trois niveaux d'exigence](#cadre-de-lecture--trois-niveaux-dexigence)
- [Partie A — Sécurité](#partie-a--sécurité)
  - [A.1 OWASP Top 10:2025 mappé au projet](#a1-owasp-top-102025-mappé-au-projet)
  - [A.2 Failles spécifiques par technologie](#a2-failles-spécifiques-par-technologie)
  - [A.3 Checklists actionnables](#a3-checklists-actionnables)
  - [A.4 Le Top 10 ne suffit pas : cadres complémentaires](#a4-le-top-10-ne-suffit-pas--cadres-complémentaires)
  - [A.5 OWASP API Security Top 10 (microservices REST)](#a5-owasp-api-security-top-10-microservices-rest)
  - [A.6 Vulnérabilités classiques à traiter au-delà du Top 10](#a6-vulnérabilités-classiques-à-traiter-au-delà-du-top-10)
  - [A.7 Développement assisté par IA : les failles qui refont surface](#a7-développement-assisté-par-ia--les-failles-qui-refont-surface)
  - [A.8 Pièges spécifiques au projet (taille des notes, ReDoS…)](#a8-pièges-spécifiques-au-projet-taille-des-notes-redos)
- [Partie B — Tests](#partie-b--tests)
- [Partie C — KISS](#partie-c--kiss)
- [Partie D — SOLID](#partie-d--solid)
- [Partie E — Definition of Done](#partie-e--definition-of-done)

---

## Cadre de lecture — trois niveaux d'exigence

Pour lever toute ambiguïté en soutenance, ce document distingue trois niveaux d'exigence. **Aucune mesure n'est retirée** : la distinction sert uniquement à montrer que le périmètre imposé, les bonnes pratiques et les améliorations possibles sont clairement séparés.

| Niveau | Signification | Exemples |
|--------|---------------|----------|
| **Obligatoire (exigé par le projet)** | Explicitement demandé par le sujet / cahier des charges | Spring Security + authentification, validation des entrées, dockerisation (une image par microservice), accès via la gateway, HTTPS en cible, base `patient` normalisée 3NF |
| **Fortement recommandé (bonnes pratiques retenues)** | Non exigé mot pour mot, mais standard professionnel appliqué ici | DTO en entrée/sortie, BCrypt, gestion globale des erreurs, en-têtes de sécurité, CORS restrictif, Actuator restreint, journalisation des accès, OWASP Dependency-Check |
| **Bonus / amélioration possible** | Va au-delà du périmètre ; à présenter comme trajectoire | SBOM, SAST/DAST, Threat Modeling STRIDE, Docker Secrets / coffre de secrets, Dependabot/Renovate, Eureka/Config Server, Resilience4j, mTLS, GridFS |

> Dans les sections qui suivent, les mesures « fortement recommandé » et « bonus » sont **conservées intégralement** : elles enrichissent la démarche sans être présentées comme des exigences du cahier des charges. C'est cette distinction — et non le retrait de contenu — qui rend le document défendable devant un jury.

---

# Partie A — Sécurité

## A.1 OWASP Top 10:2025 mappé au projet

Édition **2025** (rappel des changements clés vs 2021) : SSRF fusionné dans A01, Security Misconfiguration monte en A02, deux nouvelles catégories (A03 Software Supply Chain Failures, A10 Mishandling of Exceptional Conditions).

| # | Catégorie OWASP 2025 | Risque concret sur MédiLabo | Contre-mesures / tâches |
|---|----------------------|------------------------------|--------------------------|
| **A01** | **Broken Access Control** (inclut SSRF) | Endpoint patient/notes/risk joignable sans auth ; **IDOR/BOLA** (accéder au dossier d'un autre patient en changeant l'`id`) ; **SSRF** via `risk-service` s'il appelle une URL contrôlable | Auth **sur chaque service** ET la gateway ; **deny-by-default** ; contrôle d'accès côté serveur (jamais côté front seul) ; `risk` n'appelle QUE des services internes **en dur / whitelist** (pas d'URL issue d'une entrée utilisateur) ; désactiver les redirections ouvertes |
| **A02** | **Security Misconfiguration** (#2) | Comptes/mots de passe par défaut ; **MongoDB sans authentification** (classique de fuite) ; stacktraces exposées ; Actuator ouvert ; Swagger ouvert en prod ; ports BDD exposés ; CORS permissif | Durcissement reproductible ; **auth MongoDB activée** ; utilisateur MySQL applicatif **non-root, à privilèges minimaux** ; pas de données d'exemple/comptes par défaut ; erreurs génériques ; Actuator restreint ; CORS restrictif ; en-têtes de sécurité ; profils Spring par environnement |
| **A03** | **Software Supply Chain Failures** (nouveau) | Dépendances Maven vulnérables / transitives ; images Docker de base non épinglées ; package malveillant | **OWASP Dependency-Check** + **Dependabot/Renovate** ; versions gérées par la **BOM** (pas de version en dur) ; images Docker **officielles + tag/digest épinglé** ; `mvnw` (Maven wrapper) ; génération **SBOM** (bonus) |
| **A04** | **Cryptographic Failures** | Mots de passe en clair ; **HTTP** en transit ; secrets committés | **BCrypt** pour les mots de passe ; **HTTPS/TLS** ; aucun secret dans Git ; algorithmes/ciphers à jour |
| **A05** | **Injection** (SQL, **NoSQL**, XSS, log) | **SQLi** si `@Query` concaténé ; **NoSQL injection** Mongo (opérateur `$where`, regex, requête construite depuis l'entrée) ; **XSS** via le contenu texte libre des notes ; log injection | Requêtes **paramétrées** (méthodes Spring Data / `@Query` avec paramètres nommés) ; jamais de requête Mongo bâtie depuis une entrée brute (repositories dérivés / `Criteria` bindé) ; **échappement Thymeleaf** (`th:text`, **jamais `th:utext`** sur les notes) ; Bean Validation ; assainir les logs |
| **A06** | **Insecure Design** | Pas de limitation de tentatives de login (**brute force**) ; message d'auth qui révèle si un compte existe ; règle de risque mal conçue (fail-open) | **Threat modeling** léger (STRIDE) ; **rate limiting / verrouillage** sur le login ; messages d'erreur **génériques** ; sécurité « by design » (deny-by-default, moindre privilège) ; logique de risque déterministe et testée |
| **A07** | **Authentication Failures** | Mot de passe faible ; brute force ; **session fixation** ; credential stuffing | **BCrypt** ; politique de mot de passe forte ; **verrouillage/rate limit** ; protection session fixation (rotation d'ID de session — défaut Spring Security) ; logout propre ; **CSRF** activé sur les formulaires |
| **A08** | **Software or Data Integrity Failures** | Désérialisation non sûre ; artefacts/CI non vérifiés ; auto-update depuis source non fiable | Ne pas désérialiser de données non fiables ; **vérifier l'intégrité** des dépendances (checksums) ; protéger la CI/CD ; images épinglées par **digest** |
| **A09** | **Security Logging and Alerting Failures** | Aucun journal de **qui accède aux données patients** ; PII en clair dans les logs ; aucune alerte | Journaliser les **événements d'auth** et **accès aux données** (sans PII en clair) ; centraliser les logs ; format exploitable pour l'alerte ; rétention adaptée au contexte santé |
| **A10** | **Mishandling of Exceptional Conditions** (nouveau) | Stacktrace renvoyée au client ; **fail-open** (si `notes` tombe, `risk` renvoie « None » à tort) ; DoS via exception non gérée | **Gestion globale des erreurs** (`@RestControllerAdvice`) ; **échouer de manière sûre** (auth = fail-closed ; risque = réponse dégradée explicite, jamais un faux « aucun risque ») ; réponses d'erreur cohérentes ; **timeouts** sur les appels Feign |

> **Le piège santé de A10 sur ce projet :** si `risk-service` ne peut pas joindre `notes-service`, il ne doit **jamais** renvoyer silencieusement « None » (aucun risque). Il doit renvoyer une erreur explicite ou un statut « indéterminé ». Un faux négatif de risque médical = danger réel.

---

## A.2 Failles spécifiques par technologie

### Spring Security & authentification par formulaire
- Activer la sécurité sur **tous** les microservices back + la gateway (un service back joignable sans auth = porte dérobée).
- **`SecurityFilterChain`** avec `authorizeHttpRequests` en **deny-by-default** ; `permitAll` uniquement sur login + health.
- **CSRF activé** pour l'authentification par formulaire (Thymeleaf ajoute le token automatiquement dans les `<form>`).
- Mots de passe stockés en **BCrypt** (`BCryptPasswordEncoder`), jamais en clair — même le compte de démo.
- **Verrouillage / rate limiting** du login pour contrer le brute force.
- Gestion de session sécurisée : rotation d'ID à la connexion (anti session-fixation), logout invalidant la session.
- Éviter `remember-me` sans précaution ; si utilisé, token sécurisé + expiration.
- Ne pas exposer la page de login par défaut avec des infos de version.

### Injection SQL (patient-service / JPA)
- Utiliser les **méthodes dérivées Spring Data** ou `@Query` **avec paramètres nommés/positionnels** (`:id`), **jamais** de concaténation de chaîne.
- Bannir toute construction dynamique de JPQL/SQL à partir d'entrées utilisateur.
- Compte MySQL applicatif à **privilèges minimaux** (pas `root`, pas de `DROP`/`GRANT`).

### Injection NoSQL (notes-service / MongoDB)
- Utiliser les **repositories Spring Data MongoDB** dérivés ou `Criteria` **bindé**.
- Ne jamais injecter d'entrée brute dans une requête Mongo ; proscrire l'opérateur **`$where`** avec entrée utilisateur.
- Attention aux **regex** construites depuis l'entrée (ReDoS + contournement) — échapper ou éviter.
- **Activer l'authentification MongoDB** (l'instance par défaut sans auth est une cause fréquente de fuite) ; binder au réseau Docker interne uniquement.

### XSS (front Thymeleaf — vecteur n°1 : le contenu des notes)
- Les notes sont du **texte libre saisi par des humains** -> traiter comme non fiable.
- Utiliser **`th:text`** (échappement automatique) partout ; **ne jamais `th:utext`** sur le contenu des notes.
- Pour préserver les sauts de ligne **sans** réactiver le HTML : convertir `\n` en `<br>` **après** échappement, ou utiliser le CSS `white-space: pre-wrap` sur un bloc en `th:text`.
- En-tête `Content-Security-Policy` restrictif.

### CSRF
- Laisser le CSRF **activé** pour les endpoints d'écriture consommés par le front à formulaire.
- Pour des API REST purement stateless (si tu passes en token), documenter le choix de configuration CSRF.

### Gateway (Spring Cloud Gateway)
- **Filtre d'authentification** avant routage ; propager l'identité aux services back.
- **Rate limiting** (protège login & endpoints) — mécanisme : `RequestRateLimiter` de Spring Cloud Gateway (avec Redis) au niveau gateway, ou **Bucket4j** au niveau service.
- **CORS** restrictif (origines explicites, jamais `*` en prod).
- **En-têtes de sécurité** : `X-Content-Type-Options: nosniff`, `X-Frame-Options`/`frame-ancestors`, `Strict-Transport-Security`, `Content-Security-Policy`.
- Supprimer/masquer les en-têtes qui divulguent la stack (`Server`, versions).

### SSRF & appels inter-services (OpenFeign)
- `risk-service` ne doit appeler que des **cibles internes connues** (config figée / whitelist), **jamais** une URL dérivée d'une entrée utilisateur.
- Timeouts + gestion d'échec explicite.

### Secrets, `.env` et configuration
- **Aucun secret dans le dépôt** : mots de passe DB, clés, comptes.
- **`.gitignore`** sur `.env`, `*-secret*.yml`, dumps, etc. ; **`.dockerignore`** pour ne pas embarquer de secrets dans l'image.
- Fournir un **`.env.example`** (sans valeurs réelles) pour la reproductibilité.
- Injecter les secrets via **variables d'environnement** / **Docker secrets** ; externaliser via profils Spring.
- Ne **jamais** logguer les secrets ; scanner l'historique Git (secrets déjà committés = à révoquer + purger l'historique).

### Durcissement Docker
- Image de base **officielle, minimale (JRE slim), épinglée** (tag + digest).
- Conteneur en **utilisateur non-root** (`USER`).
- **Build multi-stage** (pas d'outils de build ni de secrets dans l'image finale).
- Pas de secret dans les **layers** ni les `ARG` persistés.
- **`.dockerignore`** complet ; système de fichiers en **lecture seule** quand possible ; `HEALTHCHECK`.
- Seule la **gateway** publie un port ; bases et services back **non exposés** sur l'hôte ; réseau Docker dédié.

### Dépendances & supply chain
- **OWASP Dependency-Check** (plugin Maven) en build + **Dependabot/Renovate**.
- Versions via **BOM Spring Cloud/Boot** (pas de versions figées à la main).
- Retirer les dépendances inutiles (surface d'attaque + poids).

### Documentation d'API (springdoc / Swagger)
- Ne pas exposer Swagger UI **publiquement en prod** sans authentification.

### Actuator
- Restreindre les endpoints exposés (`health`, `info` au minimum) ; sécuriser les endpoints sensibles ; ne pas divulguer d'infos d'environnement.

### Logging (contexte santé / RGPD)
- Journaliser auth + accès aux données patients (**traçabilité ISO/RGPD**).
- **Jamais de données de santé/PII en clair** dans les logs.
- Rétention et protection des journaux.

---

## A.3 Checklists actionnables

### Authentification & accès
- [ ] Spring Security actif sur gateway **et** chaque service back
- [ ] `authorizeHttpRequests` en deny-by-default, `permitAll` minimal (login, health)
- [ ] Mots de passe en BCrypt, aucun mot de passe en clair
- [ ] Verrouillage / rate limiting sur le login
- [ ] CSRF activé sur les formulaires
- [ ] Protection session fixation + logout invalidant la session
- [ ] Contrôle d'accès aux données côté serveur (test IDOR/BOLA)

### Injection & entrées
- [ ] Aucune requête JPA/JPQL concaténée depuis une entrée
- [ ] Aucune requête Mongo construite depuis une entrée brute, pas de `$where`
- [ ] Bean Validation sur tous les DTO d'entrée (avec adresse/téléphone nullables)
- [ ] Thymeleaf `th:text` partout, `th:utext` proscrit sur les notes
- [ ] Sauts de ligne préservés sans réactiver le HTML (CSS `pre-wrap` ou `<br>` après échappement)

### Configuration & secrets
- [ ] `.env` / secrets dans `.gitignore` et `.dockerignore`
- [ ] `.env.example` fourni sans valeurs réelles
- [ ] Aucun secret dans le code, l'historique Git ou les layers Docker
- [ ] Secrets injectés par variables d'env / Docker secrets
- [ ] Authentification MongoDB activée
- [ ] Utilisateur MySQL applicatif non-root à privilèges minimaux
- [ ] Actuator restreint, Swagger non public en prod
- [ ] CORS restrictif + en-têtes de sécurité sur la gateway

### Réseau & conteneurs
- [ ] Seule la gateway expose un port
- [ ] Bases et services back non exposés sur l'hôte, réseau Docker dédié
- [ ] Images officielles épinglées (tag + digest), utilisateur non-root, multi-stage
- [ ] `HEALTHCHECK` défini

### Supply chain & erreurs
- [ ] OWASP Dependency-Check + Dependabot/Renovate en place
- [ ] Dépendances gérées par BOM, dépendances inutiles retirées
- [ ] `@RestControllerAdvice` global, aucune stacktrace renvoyée au client
- [ ] Fail-closed pour l'auth ; risque jamais renvoyé « None » en cas de dépendance indisponible
- [ ] Timeouts sur les appels Feign

### Observabilité
- [ ] Journalisation auth + accès données patients
- [ ] Aucune donnée de santé/PII en clair dans les logs

---

## A.4 Le Top 10 ne suffit pas : cadres complémentaires

L'OWASP Top 10 est une **liste de sensibilisation** : 10 catégories qui regroupent des centaines de CWE. Pour une couverture réellement complète, on s'appuie sur des référentiels dédiés :

| Référentiel | À quoi ça sert ici |
|-------------|--------------------|
| **OWASP ASVS** (Application Security Verification Standard) | Checklist de vérification **exhaustive et testable**, par niveaux (L1/L2/L3). C'est *le* standard pour « ai-je tout couvert ? ». |
| **OWASP Proactive Controls** | Les **contrôles à mettre en place proactivement** (valider les entrées, sécuriser l'accès aux données, gérer les erreurs, etc.). Orienté « quoi faire » plutôt que « quoi éviter ». |
| **OWASP API Security Top 10 (2023)** | **Directement applicable** : projet 100 % microservices/REST. Voir A.5. |
| **OWASP Cheat Sheet Series** | Fiches pratiques par sujet (Auth, Password Storage, REST Security, XSS Prevention, SQLi Prevention, Docker Security…). Référence d'implémentation. |
| **CWE Top 25 Most Dangerous Software Weaknesses** | Vue « faiblesse par faiblesse » (mise à jour annuelle) : plus fin que les catégories OWASP. |
| **OWASP Top 10 for LLM Applications (2025)** | À consulter **uniquement si** on ajoute une brique IA au produit (pas le cas ici). |
| **NIST SSDF / 12-Factor App** | Bonnes pratiques de cycle de vie sécurisé et de configuration (utile pour le point « secrets/config »). |

> À l'échelle de ce projet, la démarche pragmatique : **ASVS niveau 1** comme grille de vérification + **API Security Top 10** + les sections ci-dessous.

---

## A.5 OWASP API Security Top 10 (microservices REST)

Le projet expose **plusieurs API REST derrière une gateway** : l'API Security Top 10 (2023) est aussi pertinent que le Top 10 généraliste.

| # | Risque API | Application MédiLabo | Contre-mesure |
|---|------------|----------------------|---------------|
| **API1** | **Broken Object Level Authorization (BOLA/IDOR)** | Lire `/patients/{id}` ou `/notes/patient/{id}` d'un autre patient en changeant l'`id` | Vérifier l'autorisation **sur l'objet** à chaque requête, côté serveur |
| **API2** | **Broken Authentication** | Login faible, brute force, session mal gérée | Voir A.2 (BCrypt, rate limit, session) |
| **API3** | **Broken Object Property Level Authorization** (ex-mass assignment / excessive data) | Un POST/PUT patient qui modifie des champs non autorisés ; renvoyer trop de champs | **DTO stricts** en entrée **et** en sortie ; ne jamais binder l'entité directement |
| **API4** | **Unrestricted Resource Consumption** | **Notes sans limite de taille** -> DoS mémoire/stockage (voir A.8) ; pas de pagination sur `/patients` | Limites de taille, **pagination**, timeouts, rate limiting |
| **API5** | **Broken Function Level Authorization (BFLA)** | Accès à une opération non prévue (ex. DELETE) | Restreindre chaque endpoint ; deny-by-default |
| **API6** | **Unrestricted Access to Sensitive Business Flows** | Automatisation abusive d'un flux | Limitation/détection d'abus |
| **API7** | **SSRF** | `risk` faisant une requête vers une cible contrôlable | Whitelist des cibles internes (voir A.2) |
| **API8** | **Security Misconfiguration** | Voir A02 du Top 10 | Durcissement, en-têtes, CORS |
| **API9** | **Improper Inventory Management** | Endpoints/versions oubliés, doc obsolète, Swagger exposé | Inventaire des endpoints, doc OpenAPI maîtrisée, pas d'API fantôme |
| **API10** | **Unsafe Consumption of APIs** | `risk` fait **confiance aveugle** aux réponses de `patient`/`notes` | Valider/typer les réponses reçues, gérer les cas d'erreur et de format |

---

## A.6 Vulnérabilités classiques à traiter au-delà du Top 10

Failles concrètes souvent absentes des « titres » du Top 10 mais bien réelles. Checklist :

**Contrôle d'accès & logique**
- [ ] **IDOR / BOLA** : autorisation vérifiée sur chaque objet (pas seulement « authentifié »)
- [ ] **Mass assignment** : DTO d'entrée stricts, pas de binding direct de l'entité
- [ ] **Excessive data exposure** : DTO de sortie, ne pas renvoyer de champs internes
- [ ] **Broken business logic** : la logique de risque ne peut pas être contournée ou faussée
- [ ] **Race conditions / TOCTOU** : cohérence sur les écritures concurrentes

**Entrées & sorties**
- [ ] **XXE** : parseurs XML configurés pour désactiver les entités externes (si XML utilisé)
- [ ] **ReDoS** : pas de regex vulnérable sur des entrées non bornées (voir A.8)
- [ ] **Open redirect** : pas de redirection vers une URL contrôlée par l'utilisateur
- [ ] **Path traversal** : pas d'accès fichier construit depuis une entrée (`../`)
- [ ] **Log injection** : neutraliser les retours-chariot/format dans les logs

**Navigateur & session**
- [ ] **Clickjacking** : en-tête `X-Frame-Options` / CSP `frame-ancestors`
- [ ] **CORS** mal configuré : origines explicites, jamais `*` avec credentials
- [ ] **En-têtes de sécurité** : `nosniff`, `HSTS`, `Referrer-Policy`, CSP
- [ ] **Cookies** : `HttpOnly`, `Secure`, `SameSite`
- [ ] **Cache** : pas de mise en cache de données sensibles côté client/proxy

**Authentification (détails)**
- [ ] **Timing attack** : comparaison de secrets à temps constant — en pratique **pris en charge par Spring Security / `BCryptPasswordEncoder`** (rien de spécifique à coder)
- [ ] **Énumération de comptes** : messages d'erreur génériques
- [ ] Si **JWT** (hors périmètre imposé mais fréquent) : rejeter `alg=none`, secret/clé forte, **expiration**, ne pas stocker de données sensibles dans le token

**Fiabilité / disponibilité**
- [ ] **Resource exhaustion / DoS** : pagination, limites de taille, timeouts, rate limiting
- [ ] **Insecure deserialization** : ne pas désérialiser de données non fiables
- [ ] **Fail-safe** : échouer fermé (auth) et ne jamais renvoyer un faux « aucun risque »

---

## A.7 Développement assisté par IA : les failles qui refont surface

> **Contexte et justification de cette section.** Le développement de cette application est réalisé avec l'assistance d'outils d'intelligence artificielle. Les recommandations de cette section ont donc pour objectif de réduire les risques spécifiques introduits par le code généré par IA (hallucination de dépendances, réintroduction de vulnérabilités connues, erreurs de configuration, etc.). Elles complètent les exigences de sécurité générales mais **ne constituent pas des exigences fonctionnelles du projet**.

Le code généré par IA réintroduit fréquemment des failles « classiques ». Constats récents (2025-2026, voir bibliographie) :

- Des études indiquent qu'une **part importante du code généré par IA** introduit des vulnérabilités du Top 10 (les mesures varient selon les études et les modèles, souvent de l'ordre de **20 à 45 %**). Les catégories les plus fréquentes : **injection** (SQL/OS/code) et **SSRF**, avec aussi de la **crypto faible** et du **XSS**.
- Nouveau vecteur de supply chain : le **« slopsquatting »**. Les modèles **hallucinent des noms de paquets** inexistants ; des attaquants **enregistrent ces noms** avec du code malveillant. Comme certaines hallucinations sont **récurrentes** (le même faux nom revient d'une session à l'autre), le paquet piégé finit par être installé. Des cas réels ont été documentés en npm/PyPI.
- Autre phénomène : **confusion inter-registres** (un nom halluciné « Python » qui existe en npm) et **copier-coller de commandes d'installation** suggérées par l'IA sans vérification.

**Garde-fous à appliquer sur CE projet (code assisté par IA compris) :**

- [ ] **Vérifier chaque dépendance suggérée** : existe-t-elle vraiment ? éditeur légitime ? téléchargements/historique cohérents ? Ne jamais `add` un paquet « parce que l'IA l'a écrit ».
- [ ] **Lockfile + versions épinglées** (BOM Maven) ; **build reproductible**.
- [ ] **Scan systématique** : OWASP Dependency-Check + Dependabot/Renovate (déjà en A.2/A03).
- [ ] **Allowlist** de dépendances ; interdire l'installation automatique de paquets par un agent sans revue humaine.
- [ ] **Revue humaine obligatoire** du code généré, avec un regard spécifique sur : requêtes concaténées (SQLi/NoSQLi), `th:utext`/HTML non échappé (XSS), secrets en dur, crypto obsolète (MD5/SHA1, `Random` non sécurisé), gestion d'erreurs qui fuit, appels réseau vers URL non maîtrisée (SSRF).
- [ ] **SAST** (analyse statique) dans la CI — mais ne pas s'y fier seul (un même défaut n'est souvent détecté que par un seul outil sur plusieurs).
- [ ] **Ne pas déléguer le jugement** : l'IA accélère la production **et** le taux d'introduction de failles ; la vitesse ne remplace pas la revue.

> Principe : **traiter tout code généré comme du code d'un tiers non fiable** — à relire, tester et scanner avant intégration.

---

## A.8 Pièges spécifiques au projet (taille des notes, ReDoS…)

Le brief impose que les notes n'aient **aucune limite de taille** et que le **format soit conservé**. C'est une exigence fonctionnelle qui crée des risques techniques précis :

- [ ] **DoS par volumétrie (API4)** : « pas de limite » côté métier != « pas de garde-fou » côté technique. Prévoir une **limite haute raisonnable** (taille de requête, quota) pour éviter la saturation mémoire/stockage.
- [ ] **Limite MongoDB** : un document BSON est **plafonné à 16 Mo**. Une note très volumineuse stockée en un seul document peut donc casser. Concevoir en conséquence (une note = un document borné). *GridFS est une piste théorique pour de très gros binaires, non requise par le sujet et non retenue ici — mentionnée uniquement pour complétude.*
- [ ] **ReDoS sur la détection de déclencheurs** : la recherche des termes (Fumeur, Anormal…) sur un texte potentiellement énorme ne doit pas utiliser de **regex à backtracking catastrophique**. Préférer une recherche simple/normalisée (minuscule + suppression d'accents + `contains`) plutôt qu'une regex complexe.
- [ ] **Normalisation Unicode** : normaliser (casse, accents, éventuellement forme Unicode) **avant** comptage, pour éviter à la fois les faux négatifs métier et les contournements.
- [ ] **XSS renforcé** : puisque le format est conservé et le texte long et libre, c'est le **vecteur XSS n°1** — `th:text` + préservation via CSS `pre-wrap` (jamais `th:utext`, jamais d'injection de `<br>` non échappés).
- [ ] **Encodage/charset** : forcer UTF-8 de bout en bout (BDD, API, front) pour préserver la terminologie médicale accentuée.

---

# Partie B — Tests

## B.1 Stratégie — pyramide de tests

```
        /\        E2E / manuels (peu)   -> parcours complet via gateway
       /  \       Intégration           -> @SpringBootTest, Testcontainers
      /----\      Slice / sécurité       -> @WebMvcTest, @DataJpaTest, @DataMongoTest, spring-security-test
     /------\     Unitaires (beaucoup)   -> JUnit 5 + Mockito : moteur de risque ++
```

## B.2 Tests par microservice

| Service | Unitaire | Slice | Intégration |
|---------|----------|-------|-------------|
| `patient` | Validation, mapping DTO | `@DataJpaTest` (repository), `@WebMvcTest` (contrôleur) | `@SpringBootTest` + (Testcontainers MySQL) |
| `notes` | Mapping, logique | `@DataMongoTest`, `@WebMvcTest` | `@SpringBootTest` + (Testcontainers MongoDB) |
| `risk` | **Moteur de calcul (prioritaire)** : comptage de déclencheurs distincts, règles âge/genre | `@WebMvcTest` | Appels Feign simulés (WireMock) |
| `gateway` | Config de routes | — | Test de routage + auth |
| `front` | Logique de contrôleur | `@WebMvcTest` (rendu Thymeleaf) | — |

## B.3 Tests de sécurité (spécifiques, à ne pas oublier)
- [ ] Accès **non authentifié** à un endpoint protégé -> **401/403** (`spring-security-test`, sans `@WithMockUser`)
- [ ] Accès authentifié valide -> **200** (`@WithMockUser`)
- [ ] **IDOR/BOLA** : un utilisateur ne peut pas lire un dossier hors périmètre attendu
- [ ] **Validation** : payloads invalides rejetés (400), champs obligatoires manquants
- [ ] **XSS** : une note contenant `<script>` est **échappée** dans le HTML rendu
- [ ] **CSRF** : requête d'écriture sans token rejetée
- [ ] **Fail-safe** : `risk` avec dépendance `notes` indisponible ne renvoie **pas** « None »

## B.4 Tests métier obligatoires (les 4 cas de référence)
- [ ] Patient 1 (F, ~59 ans, 1 déclencheur) -> **None**
- [ ] Patient 2 (M, >30 ans, 2 déclencheurs) -> **Borderline**
- [ ] Patient 3 (M, <30 ans, 3 déclencheurs) -> **In Danger**
- [ ] Patient 4 (F, <30 ans, 8 déclencheurs) -> **Early onset**
- [ ] Vérifier le **comptage distinct** (un terme répété compte 1) et les **variantes** (Fumeur/Fumeuse, Vertige/Vertiges) + insensibilité casse/accents

## B.5 Outils
- **JUnit 5**, **Mockito**, **Spring Boot Test**, **spring-security-test**
- **Testcontainers** (MySQL/MongoDB réels) — bonus fort
- **WireMock** (simulation des appels inter-services `risk` -> `patient`/`notes`)
- **JaCoCo** (couverture) ; viser une couverture significative du **moteur de risque**

---

# Partie C — KISS (Keep It Simple, Stupid)

Le brief insiste sur des sous-projets **peu complexes** et une interface **sobre**. Appliquer KISS, c'est :

- **Ne pas ajouter de microservices** au-delà des 5 nécessaires (patient, notes, risk, gateway, front).
- **Ne pas** introduire Eureka / Config Server / Kafka **si le besoin ne l'exige pas** (nommage Docker Compose suffit). Ce sont des bonus, pas des obligations.
- **Modèle de données simple** et normalisé (une table patient suffit pour la 3NF).
- **Endpoints minimaux** correspondant strictement aux user stories.
- **Logique de risque lisible** : un composant clair, pas une usine à abstractions.
- Éviter la **sur-ingénierie** : pas d'abstraction « au cas où », pas de configuration prématurée.
- Privilégier les **conventions Spring** (repositories dérivés, auto-config) plutôt que du code manuel.
- Messages, UI et code **explicites** plutôt que malins.

> Règle simple : si une brique n'est pas justifiée par une user story ou une exigence de sécurité, elle ne rentre pas.

---

# Partie D — SOLID

Application concrète des 5 principes au projet :

### S — Single Responsibility
- Séparer **Controller / Service / Repository** dans chaque microservice.
- Dans `risk-service`, isoler deux responsabilités : un **`TriggerDetector`** (détecte/compte les termes déclencheurs distincts dans les notes) **et** un **`RiskEvaluator`** (applique les règles âge/genre). Ne pas tout mélanger dans une seule méthode.

### O — Open/Closed
- Rendre la **liste des termes déclencheurs configurable** (externalisée) plutôt que codée en dur au milieu de la logique, pour qu'ajouter un terme n'oblige pas à réécrire l'algorithme. *Le sujet fige les 11 déclencheurs : c'est donc une **amélioration possible** (bonus), pas une obligation — mais elle illustre bien le principe.*
- Règles de risque extensibles (ex. table de règles / stratégie par niveau) sans modifier le code existant.

### L — Liskov Substitution
- Les DTO et interfaces (ex. clients Feign, repositories) doivent être substituables par leurs implémentations/mocks sans casser le comportement (essentiel pour les tests).

### I — Interface Segregation
- Des **clients Feign fins et ciblés** : `PatientClient` et `NotesClient` séparés, plutôt qu'une seule grosse interface fourre-tout.

### D — Dependency Inversion
- Les services dépendent d'**abstractions injectées** (interfaces de repository Spring Data, interfaces de clients), pas d'implémentations concrètes.
- `RiskEvaluator` reçoit ses dépendances par **injection** -> testable en isolation avec des mocks.

---

# Partie E — Definition of Done

Une fonctionnalité est « terminée » quand :

- [ ] Elle répond à la user story et à ses critères d'acceptation
- [ ] Endpoints validés (Bean Validation) et sécurisés (auth requise)
- [ ] Aucune faille des checklists A.3 introduite
- [ ] Tests unitaires + slice + (intégration) verts, y compris tests de sécurité
- [ ] Les 4 cas de référence passent (pour le sprint 3)
- [ ] Aucun secret dans le code / Git / image Docker
- [ ] Service dockerisé, joignable uniquement via la gateway
- [ ] Endpoints documentés via **OpenAPI (springdoc / Swagger UI)**
- [ ] Code conforme KISS/SOLID, revu
- [ ] README à jour (dont section **Green Code**)

---

## Bibliographie & références

**Référentiels officiels (à citer en soutenance)**
- OWASP Top 10:2025 — https://owasp.org/Top10/2025/
- OWASP API Security Top 10 (2023) — https://owasp.org/API-Security/
- OWASP Application Security Verification Standard (ASVS) 5.0.0, mai 2025 — https://owasp.org/www-project-application-security-verification-standard/
- OWASP Proactive Controls — https://owasp.org/www-project-proactive-controls/
- OWASP Cheat Sheet Series — https://cheatsheetseries.owasp.org/
- CWE Top 25 Most Dangerous Software Weaknesses (MITRE) — https://cwe.mitre.org/top25/
- OWASP Top 10 for LLM Applications (2025) — https://genai.owasp.org/ (pertinent uniquement si une brique IA est ajoutée au produit)

**Sécurité du code assisté par IA & slopsquatting (section A.7)**
- Spracklen, J. et al., « We Have a Package for You! A Comprehensive Analysis of Package Hallucinations by Code-Generating LLMs », USENIX Security Symposium, 2025 (étude fondatrice sur l'hallucination de paquets, taux de l'ordre de 20 %).
- Terme « slopsquatting » attribué à Seth Larson (Python Software Foundation), 2025 ; documentation d'incidents réels npm/PyPI par des équipes de recherche sécurité (ex. Socket, Aikido) en 2025-2026.
- Cloud Security Alliance (CSA), notes de recherche 2026 sur le slopsquatting et la hausse des vulnérabilités dans le code généré par IA.
- Analyses éditeurs (ex. Veracode, 2025-2026) rapportant qu'une part significative du code généré par IA (mesures variables, souvent 20-45 %) introduit des vulnérabilités de l'OWASP Top 10.

> Les chiffres cités varient selon les études, les modèles et les protocoles de mesure ; ils sont donnés à titre indicatif. Vérifier les éditions et données les plus récentes au moment de la soutenance.

---

*Document à adapter selon les exigences finales du correcteur. Les mesures « fortement recommandé » et « bonus » sont conservées volontairement : voir le « Cadre de lecture — trois niveaux d'exigence » en tête de document.*
