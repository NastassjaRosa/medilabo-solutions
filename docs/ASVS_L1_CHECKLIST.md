# Grille ASVS 5.0 — Niveau 1 (filtrée pour MédiLabo Solutions)

> Checklist de **vérification** de sécurité, adaptée de l'**OWASP ASVS 5.0.0** (version stable publiée le 30 mai 2025), **niveau 1**.
> Exigences **reformulées en français et adaptées au projet** (contenu paraphrasé — ASVS est publié sous licence CC BY-SA 4.0 ; se référer au standard officiel pour le texte exact et les identifiants).
>
> **Périmètre pris en compte :** microservices REST (patient, notes, risk), gateway Spring Cloud Gateway, front Thymeleaf server-side, Spring Security (auth formulaire/Basic), MySQL, MongoDB, Docker.
> **Chapitres exclus car non applicables :** OAuth/OIDC, WebRTC, upload de fichiers (aucun dans le périmètre), self-contained tokens **sauf si** tu ajoutes du JWT (section optionnelle en fin de grille).
>
> **Sur les niveaux :** le **niveau 1 constitue un socle de vérification de base**. Les niveaux 2 et 3 apportent une **assurance plus élevée**, notamment pour les applications manipulant des données sensibles. La bonne cible pour un projet de cette taille : viser L1 complet, puis piocher dans L2 sur les points sensibles (auth, accès aux données de santé).

---

## Comment l'utiliser
- Coche chaque item quand il est **implémenté ET vérifié** (idéalement par un test automatisé).
- « N/A » est acceptable **si tu justifies** pourquoi l'exigence ne s'applique pas (traçabilité).
- Les items marqués **(prioritaire)** sont à traiter en premier, vu le contexte santé / microservices.

---

## 1. Encodage & prévention des injections

- [ ] (prioritaire) Les sorties sont **encodées selon le contexte** (HTML, attribut, URL, JS) — Thymeleaf `th:text` par défaut, jamais `th:utext` sur du contenu utilisateur (notes)
- [ ] (prioritaire) Protection contre l'**injection SQL** : requêtes paramétrées / méthodes Spring Data, aucune concaténation d'entrée dans une requête JPQL/SQL
- [ ] (prioritaire) Protection contre l'**injection NoSQL** (MongoDB) : pas de requête construite depuis une entrée brute, pas d'opérateur `$where` avec entrée utilisateur
- [ ] Protection contre l'**injection de commande OS** : aucun appel système construit depuis une entrée (a priori aucun ici)
- [ ] Protection contre l'**injection dans les logs** : neutraliser retours-chariot / caractères de contrôle dans ce qui est journalisé
- [ ] Si parsing XML : entités externes **désactivées** (anti-XXE)

## 2. Validation & logique métier

- [ ] (prioritaire) **Toutes les entrées** sont validées côté serveur (Bean Validation) : type, longueur, format, plage
- [ ] Champs obligatoires vérifiés ; **adresse et téléphone acceptés vides** (règle métier) mais validés si présents
- [ ] (prioritaire) La **logique de risque** ne peut être ni contournée ni faussée par des entrées inattendues (âge négatif, genre inconnu, notes vides)
- [ ] Bornes appliquées aux entrées potentiellement volumineuses (taille des notes — cf. chapitre 4 « limites/pagination » et document Sécurité, section A.8 volumétrie)
- [ ] Rejet propre (400) des données malformées, sans divulgation d'info technique

## 3. Sécurité front (navigateur)

- [ ] (prioritaire) En-tête **`Content-Security-Policy`** défini (restreint scripts/styles)
- [ ] En-tête **`X-Content-Type-Options: nosniff`**
- [ ] (prioritaire) Protection **clickjacking** : `X-Frame-Options: DENY` ou CSP `frame-ancestors`
- [ ] Cookies de session en **`HttpOnly`**, **`Secure`**, **`SameSite`**
- [ ] (prioritaire) **CORS** restrictif sur la gateway : origines explicites, jamais `*` avec credentials
- [ ] Pas de données sensibles mises en cache côté navigateur/proxy (`Cache-Control` adapté)
- [ ] Aucune donnée sensible dans les URL (query string) — utiliser le corps de requête

## 4. API & services web

- [ ] (prioritaire) Chaque endpoint REST applique **authentification + autorisation** (deny-by-default)
- [ ] (prioritaire) **DTO stricts** en entrée (pas de binding direct de l'entité -> anti mass-assignment) et en sortie (pas de champ interne exposé)
- [ ] Méthodes HTTP restreintes à celles attendues par endpoint
- [ ] `Content-Type` validé/attendu ; réponses correctement typées (`application/json`)
- [ ] (prioritaire) `risk-service` **valide les réponses** reçues de `patient`/`notes` (pas de confiance aveugle)
- [ ] Pagination/limites sur les listes (`/patients`, `/notes/...`) pour éviter la sur-consommation

## 5. Authentification

- [ ] (prioritaire) Mots de passe stockés avec un algorithme **fort et salé** (BCrypt), jamais en clair ni en hash rapide (MD5/SHA1)
- [ ] (prioritaire) Aucun **identifiant/mot de passe par défaut** actif en dehors du strict nécessaire de démo
- [ ] Politique de mot de passe raisonnable pour le(s) compte(s) (longueur minimale)
- [ ] (prioritaire) **Anti-brute-force** : limitation de tentatives / temporisation sur le login
- [ ] Messages d'échec d'authentification **génériques** (pas d'énumération de comptes)
- [ ] Comparaison des secrets **à temps constant** (utiliser les mécanismes du framework)
- [ ] Déconnexion disponible et effective

## 6. Gestion de session

- [ ] (prioritaire) **Nouvel identifiant de session à la connexion** (anti session-fixation — défaut Spring Security)
- [ ] Session **invalidée au logout** et après expiration (timeout d'inactivité)
- [ ] Identifiant de session transmis uniquement via cookie sécurisé (jamais dans l'URL)
- [ ] (prioritaire) **CSRF** activé pour les requêtes d'écriture issues du front à formulaire

## 7. Autorisation / contrôle d'accès

- [ ] (prioritaire) Contrôle d'accès **appliqué côté serveur** (jamais uniquement côté front)
- [ ] (prioritaire) **Deny-by-default** : tout endpoint non explicitement ouvert est refusé
- [ ] (prioritaire) Vérification d'accès **au niveau de l'objet** (anti IDOR/BOLA) sur `/patients/{id}` et `/notes/patient/{id}`
- [ ] Contrôle d'accès **au niveau des fonctions** (anti BFLA : pas d'opération non prévue accessible)
- [ ] Les services back **ne sont pas joignables** directement depuis l'extérieur (seule la gateway expose un port)

## 8. Cryptographie

- [ ] Algorithmes/hash **à jour** (pas de MD5/SHA1 pour la sécurité ; générateur aléatoire **sécurisé**, pas `java.util.Random` pour des secrets)
- [ ] Aucune clé/secret **codé en dur** dans le source
- [ ] Rotation possible des secrets (secrets externalisés, non figés dans l'image)

## 9. Communication sécurisée

- [ ] (prioritaire) **TLS/HTTPS** prévu pour tout trafic exposé hors environnement local ; en local Docker, l'absence de TLS est **documentée et limitée à l'environnement de développement**
- [ ] Configuration TLS moderne (versions/ciphers récents) si TLS activé
- [ ] Communications internes sur un **réseau Docker isolé** (bases et services non exposés sur l'hôte)

## 10. Configuration & durcissement

- [ ] (prioritaire) **Aucun secret dans le dépôt Git** ni dans l'historique (`.gitignore`, `.dockerignore`, `.env.example` sans valeurs)
- [ ] (prioritaire) Secrets injectés par **variables d'environnement / Docker secrets**
- [ ] (prioritaire) **Authentification MongoDB activée** ; utilisateur **MySQL applicatif non-root** à privilèges minimaux
- [ ] **Messages d'erreur génériques** en production (aucune stacktrace renvoyée au client)
- [ ] **Actuator** restreint ; **Swagger/OpenAPI** non exposé publiquement sans auth en prod
- [ ] En-têtes divulguant la stack (`Server`, versions) supprimés/masqués
- [ ] Images Docker **officielles, minimales, épinglées** (tag + digest), conteneur **non-root**, build multi-stage
- [ ] (prioritaire) Dépendances **scannées** (OWASP Dependency-Check / Dependabot) et **épinglées** (BOM) ; aucune dépendance inutile ou non vérifiée (anti slopsquatting)

## 11. Protection des données (contexte santé / RGPD)

- [ ] (prioritaire) Les **données patients** ne sont accessibles qu'après authentification et contrôle d'accès
- [ ] (prioritaire) **Aucune donnée de santé/PII en clair dans les logs**
- [ ] Pas d'exposition de données sensibles via des réponses trop larges ou des messages d'erreur
- [ ] Données minimales renvoyées (principe de minimisation)

## 12. Secure coding & architecture

- [ ] (prioritaire) **Fail-safe** : en cas d'erreur, on échoue **fermé** pour l'auth ; `risk` ne renvoie **jamais** un faux « None » si une dépendance est indisponible
- [ ] Pas de **désérialisation** de données non fiables
- [ ] Séparation claire des responsabilités (controller/service/repository) — cohérent SOLID
- [ ] Dépendances tierces (et **code généré par IA**) traitées comme non fiables : revue + tests + scan avant intégration
- [ ] Timeouts sur les appels inter-services (Feign)

## 13. Journalisation & gestion des erreurs

- [ ] (prioritaire) **Gestion globale des erreurs** (`@RestControllerAdvice`) : réponses cohérentes, pas de fuite technique
- [ ] Journalisation des **événements de sécurité** (connexions, échecs d'auth, accès aux données patients)
- [ ] Les logs **ne contiennent pas** de secrets ni de PII en clair
- [ ] Horodatage et informations suffisantes pour l'investigation (sans sur-collecte)

---

## (Optionnel) Self-contained tokens — uniquement si JWT

À remplir **seulement** si tu introduis des JWT (non imposé par le brief) :

- [ ] Algorithme de signature explicite et sûr ; **`alg=none` rejeté**
- [ ] Clé/secret de signature **forte** et non committée
- [ ] **Expiration** (`exp`) courte et vérifiée ; `iss`/`aud` validés
- [ ] Aucune donnée sensible stockée dans le payload du token
- [ ] Révocation / invalidation prévue (liste de révocation ou durée de vie courte)

---

## Suivi (résumé)

| Chapitre | Total items | Cochés | N/A justifiés |
|----------|:-----------:|:------:|:-------------:|
| 1. Encodage & injections | 6 | | |
| 2. Validation & logique | 5 | | |
| 3. Sécurité front | 7 | | |
| 4. API & services | 6 | | |
| 5. Authentification | 7 | | |
| 6. Session | 4 | | |
| 7. Autorisation | 5 | | |
| 8. Cryptographie | 3 | | |
| 9. Communication | 3 | | |
| 10. Configuration | 9 | | |
| 11. Protection des données | 4 | | |
| 12. Secure coding | 5 | | |
| 13. Logging & erreurs | 4 | | |
| **Total** | **68** | | |

---

*Adapté de l'OWASP ASVS 5.0.0 (CC BY-SA 4.0). Reformulé et filtré pour le projet ; consulter le standard officiel (owasp.org) pour le texte normatif et les identifiants exacts.*
