# MédiLabo Solutions — Dépistage du risque de diabète de type 2

Application en architecture **microservices** permettant à des professionnels de santé
de gérer des dossiers patients, d'y consigner des notes de consultation, et d'évaluer
automatiquement le **risque de diabète de type 2** à partir de ces notes.

Projet livré en 3 sprints, chaque microservice conteneurisé avec Docker et joignable
via une passerelle unique (Spring Cloud Gateway).

---

## Table des matières

- [Architecture](#architecture)
- [Stack technique](#stack-technique)
- [Prérequis](#prérequis)
- [Installation et lancement](#installation-et-lancement)
- [Accès à l'application](#accès-à-lapplication)
- [Sécurité](#sécurité)
- [Données de test](#données-de-test)
- [Règles de calcul du risque](#règles-de-calcul-du-risque)
- [Tests](#tests)
- [Green Code — démarche d'éco-conception](#green-code--démarche-déco-conception)

---

## Architecture

L'application est découpée en cinq microservices Spring Boot indépendants. Seule la
gateway expose un port vers l'extérieur ; tous les autres services et les bases de
données communiquent sur un réseau Docker interne (`medilabo-net`) et ne sont pas
accessibles directement depuis l'hôte.

```
                          ┌─────────────────┐
       Navigateur  ─────► │     gateway     │  :8080  (seul port exposé)
                          │ Spring Cloud GW │
                          └────────┬────────┘
                                   │  (réseau interne medilabo-net)
          ┌────────────────┬───────┴────────┬──────────────────┐
          ▼                ▼                ▼                   ▼
   ┌────────────┐   ┌────────────┐   ┌────────────┐     ┌────────────┐
   │  frontend  │   │  patient-  │   │   notes-   │     │   risk-    │
   │ Thymeleaf  │   │  service   │   │  service   │     │  service   │
   └────────────┘   └─────┬──────┘   └─────┬──────┘     └─────┬──────┘
                          ▼                ▼                   │
                     ┌─────────┐     ┌──────────┐              │
                     │  MySQL  │     │ MongoDB  │     appelle patient +
                     │  (3NF)  │     │          │     notes pour calculer
                     └─────────┘     └──────────┘     le risque (sans BDD)
```

| Microservice     | Rôle                                                        | Base de données        |
|------------------|-------------------------------------------------------------|------------------------|
| `gateway`        | Point d'entrée unique, routage, authentification HTTP Basic | —                      |
| `frontend`       | Interface web sobre (Thymeleaf)                             | —                      |
| `patient-service`| Gestion des données démographiques des patients (CRUD REST) | MySQL, normalisée 3NF  |
| `notes-service`  | Gestion des notes de consultation (CRUD REST)               | MongoDB                |
| `risk-service`   | Calcul du niveau de risque de diabète                       | aucune (interroge les deux autres services) |

Le choix de deux types de bases est volontaire : les données patient sont structurées
et **normalisées en 3NF** (exigence de certification ISO du client), tandis que les notes
sont du texte libre, multi-lignes et de taille variable, mieux adaptées à une base
documentaire NoSQL.

### Routage de la gateway

| Chemin        | Service cible     |
|---------------|-------------------|
| `/ui/**`      | `frontend`        |
| `/patients/**`| `patient-service` |
| `/notes/**`   | `notes-service`   |
| `/risk/**`    | `risk-service`    |

---

## Stack technique

- **Java 17+** (compatible 21)
- **Spring Boot 3.3.x**
- **Spring Cloud Gateway** (routage)
- **Spring Security** (authentification HTTP Basic entre services, form login sur le front)
- **Spring Data JPA** + **MySQL 8** (patient-service)
- **Spring Data MongoDB** + **MongoDB 7** (notes-service)
- **Thymeleaf** (interface web)
- **Maven** (build)
- **Docker** / **Docker Compose** (conteneurisation et orchestration)
- **JUnit 5** + **Spring Boot Test** (tests)

---

## Prérequis

- **Docker** et **Docker Compose** installés et démarrés.
- Aucune installation de Java ou Maven n'est nécessaire pour lancer l'application :
  chaque image est construite en multi-stage (compilation puis exécution dans un
  conteneur non-root).
- Pour lancer les tests localement (facultatif) : **JDK 17+** et **Maven**.

---

## Installation et lancement

### 1. Cloner le dépôt

```bash
git clone https://github.com/NastassjaRosa/medilabo-solutions.git
cd medilabo-solutions
```

### 2. Créer le fichier de variables d'environnement

Les identifiants (comptes applicatifs, mots de passe des bases) ne sont **jamais
versionnés**. Un modèle est fourni : copiez-le et renseignez vos propres valeurs.

```bash
cp .env.example .env
```

Éditez ensuite `.env` pour remplacer chaque `change-me` par une valeur réelle :

```dotenv
# --- MySQL (patient-service) ---
MYSQL_ROOT_PASSWORD=...
MYSQL_DATABASE=patientdb
MYSQL_USER=medilabo
MYSQL_PASSWORD=...

# --- MongoDB (notes-service) ---
MONGO_USER=medilabo
MONGO_PASSWORD=...
MONGO_DATABASE=notesdb

# --- Gateway (authentification HTTP Basic entre services) ---
GATEWAY_AUTH_USERNAME=medilabo
GATEWAY_AUTH_PASSWORD=...

# --- Front (compte de connexion humain) ---
FRONTEND_AUTH_USERNAME=medilabo
FRONTEND_AUTH_PASSWORD=...
```

> Le fichier `.env` est ignoré par git (`.gitignore`). Ne le committez jamais.

### 3. Construire et démarrer l'ensemble

```bash
docker compose up --build
```

Docker construit les cinq images, démarre les deux bases de données, puis les services
dans l'ordre des dépendances. Le premier lancement peut prendre quelques minutes
(téléchargement des images et compilation Maven).

Pour arrêter : `Ctrl + C`, puis `docker compose down` (ajouter `-v` pour supprimer aussi
les volumes de données MySQL et MongoDB).

---

## Accès à l'application

Une fois les conteneurs démarrés, l'application est accessible à l'adresse :

**http://localhost:8080/ui**

Connectez-vous avec le compte défini par `FRONTEND_AUTH_USERNAME` /
`FRONTEND_AUTH_PASSWORD` dans votre `.env`.

Depuis l'interface, vous pouvez consulter la liste des patients, voir le détail d'un
patient (informations personnelles, historique des notes, **niveau de risque de
diabète**), ajouter ou modifier un patient, et ajouter des notes de consultation.

---

## Sécurité

- **Point d'entrée unique** : seule la gateway expose un port (`8080`). Les microservices
  back et les bases ne sont joignables que sur le réseau Docker interne.
- **Authentification HTTP Basic** entre la gateway et chaque service back, avec un compte
  de service unique lu depuis les variables d'environnement. Les mots de passe sont
  hachés en **BCrypt** au démarrage ; aucun secret n'est écrit en dur dans le code.
- **Défense en profondeur** : chaque service back applique lui-même Spring Security
  (deny-by-default, seul `/actuator/health` reste public), et non pas uniquement la
  gateway.
- **Front** : authentification par formulaire (form login) avec session et protection
  CSRF ; échappement systématique des données affichées (Thymeleaf `th:text`) pour
  prévenir les injections XSS.
- **Secrets** : tous les identifiants proviennent du fichier `.env` non versionné ;
  `.env.example` ne contient que des valeurs `change-me`.

---

## Données de test

Au démarrage, les jeux de données sont **chargés automatiquement**, sans saisie manuelle :

- **Patients** : `patient-service/src/main/resources/schema.sql` (structure, en 3NF) et
  `data.sql` (4 patients de test) sont exécutés par Spring Boot au démarrage de MySQL.
- **Notes** : un initialiseur (`CommandLineRunner`) insère les notes de consultation des
  4 patients dans MongoDB si la collection est vide, avec des `patientId` (1 à 4) alignés
  sur les identifiants du patient-service.

Les quatre patients de test correspondent chacun à un niveau de risque attendu :

| PatientId | Risque attendu |
|-----------|----------------|
| 1         | None           |
| 2         | Borderline     |
| 3         | In Danger      |
| 4         | Early onset    |

---

## Règles de calcul du risque

Le `risk-service` détermine l'un des quatre niveaux à partir du nombre de **termes
déclencheurs** distincts trouvés dans les notes du patient, croisé avec son **âge** et son
**genre**.

Termes déclencheurs recherchés : Hémoglobine A1C, Microalbumine, Taille, Poids, Fumeur
(et formes associées : fumeuse, fume, fumer, fumait), Anormal, Cholestérol, Vertiges,
Rechute, Réaction, Anticorps.

- **None** : aucun terme déclencheur.
- **Borderline** : 2 à 5 déclencheurs, patient de plus de 30 ans.
- **In Danger** :
    - homme de moins de 30 ans : au moins 3 déclencheurs ;
    - femme de moins de 30 ans : au moins 4 déclencheurs ;
    - plus de 30 ans : 6 ou 7 déclencheurs.
- **Early onset** :
    - homme de moins de 30 ans : au moins 5 déclencheurs ;
    - femme de moins de 30 ans : au moins 7 déclencheurs ;
    - plus de 30 ans : 8 déclencheurs ou plus.

> Le comptage se fait sur les termes **distincts** (un même terme n'est compté qu'une
> fois). En cas d'indisponibilité d'un service dépendant, le risk-service renvoie une
> erreur explicite et l'interface affiche « risque indéterminé » — jamais « None » par
> défaut (comportement *fail-safe*).

---

## Tests

Chaque microservice possède ses propres tests (contrôleurs, sécurité, clients, moteur de
calcul). Pour lancer les tests d'un service :

```bash
cd <nom-du-service>
mvn test
```

Le `risk-service` inclut notamment la validation des **4 cas de référence**
(None / Borderline / In Danger / Early onset). Le `frontend` teste l'affichage du risque
ainsi que le cas d'erreur (fail-safe : ne jamais afficher « None » sur échec du service).

---

## Green Code — démarche d'éco-conception

Le client a demandé d'intégrer une démarche **Green Code** (numérique responsable) au
projet. Cette section présente le contexte, les référentiels de référence, les actions
déjà appliquées dans le projet, et des pistes d'amélioration.

### Contexte et référentiels

L'éco-conception logicielle vise à réduire l'empreinte environnementale d'un service
numérique (consommation d'énergie, de ressources serveur, et contribution à
l'obsolescence du matériel) sur l'ensemble de son cycle de vie. Un logiciel plus sobre
consomme moins de ressources à chaque exécution, pour chaque utilisateur, pendant toute
sa durée de vie.

Deux référentiels français font autorité et servent de base à cette démarche :

- **RGESN** — Référentiel Général d'Éco-conception de Services Numériques, publié dans sa
  version définitive en mai 2024 par la DINUM, l'ADEME, l'ARCEP et l'INR. Il propose
  78 critères présentés sous forme de questions pratiques, couvrant tout le cycle de vie
  d'un service numérique. C'est la référence opérationnelle des équipes produit.
- **GR491** — Guide de Référence de conception responsable de services numériques, publié
  par l'Institut du Numérique Responsable (INR). Plus exhaustif (plusieurs centaines de
  critères regroupés en familles d'actions), il est le corpus de fond dont le RGESN est
  issu.

Ces référentiels s'inscrivent dans un cadre réglementaire en vigueur (loi REEN, CSRD),
qui fait progressivement du numérique responsable une obligation pour de nombreuses
organisations.

### Actions déjà appliquées dans le projet

Plusieurs choix d'architecture et d'implémentation vont déjà dans le sens de la sobriété :

- **Images Docker optimisées** : chaque service est construit en *multi-stage* avec une
  image d'exécution légère (JRE slim, utilisateur non-root). Les images sont plus petites,
  se téléchargent et démarrent plus vite, et consomment moins de stockage et de bande
  passante.
- **Choix raisonné des bases de données** : données relationnelles normalisées en 3NF pour
  les patients (pas de redondance, donc moins de stockage et des requêtes plus efficaces),
  base documentaire pour les notes en texte libre. Le bon outil pour chaque besoin évite
  le sur-dimensionnement.
- **Interface sobre** : le front Thymeleaf reste volontairement épuré, sans bibliothèque
  front lourde, sans média superflu, ce qui allège les pages transférées.
- **Architecture ciblée** : le `risk-service` ne possède pas de base de données propre et
  ne stocke rien ; il calcule à la demande, évitant toute duplication de données.
- **Timeouts et périmètre réseau maîtrisés** : la gateway définit des délais de connexion
  et de réponse courts, ce qui évite de mobiliser des ressources sur des appels bloqués.

### Pistes d'amélioration à mener

Pour approfondir la démarche, en s'appuyant sur les critères du RGESN et du GR491 :

- **Mesurer avant d'optimiser** : instrumenter l'application (temps de réponse, requêtes
  base de données, poids des pages) afin d'identifier les points les plus coûteux, plutôt
  que d'optimiser à l'aveugle. C'est le premier principe du RGESN.
- **Optimiser les requêtes** : éviter les requêtes redondantes ou en cascade (N+1) entre
  le risk-service et les services patient/notes ; ajouter les index nécessaires côté base ;
  ne récupérer que les champs utiles.
- **Mettre en cache les résultats stables** : le niveau de risque d'un patient ne change
  que lorsqu'une note est ajoutée ; un cache éviterait de recalculer à chaque affichage.
- **Ajuster les ressources des conteneurs** : dimensionner la mémoire et le CPU alloués à
  chaque service au plus juste, et n'exécuter que les services réellement nécessaires.
- **Alléger encore le front** : compression des réponses (gzip), mise en cache des
  ressources statiques (CSS), pagination des listes de patients pour ne pas tout charger
  d'un coup.
- **Sobriété du cycle de développement** : limiter la taille des dépendances Maven au
  strict nécessaire, supprimer le code mort, et privilégier des builds reproductibles pour
  éviter des reconstructions inutiles.
- **Prolonger la durée de vie** : viser la compatibilité et la maintenabilité dans le
  temps plutôt que des dépendances qui imposent des mises à jour matérielles fréquentes.

> Un outillage dédié existe pour aller plus loin, comme le skill open source
> [green-claude](https://github.com/Institut-du-Numerique-Responsable/green-claude) de
> l'INR, qui applique automatiquement les règles RGESN/GR491 lors de l'écriture ou de la
> revue de code.
