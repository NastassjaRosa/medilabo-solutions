# Sujet OpenClassrooms — MédiLabo Solutions

Source de vérité fonctionnelle du projet. À conserver dans `docs/` et à ne pas modifier.

## Contexte

Société internationale travaillant avec des cliniques et cabinets privés sur le dépistage des risques de maladies. Le client (Abernathy Clinic, Ramesh) demande une application pour aider ses médecins à **détecter le risque de diabète de type 2**, en se concentrant sur les soins préventifs.

Responsable produit : Taylor Waters. Livraison en **3 sprints** dans un dépôt GitHub.

## Contraintes imposées par le client

1. Application découpée en **microservices** (projets Spring Boot), joignables via un microservice **gateway** (Spring Cloud Gateway).
2. Une **image Docker** par microservice.
3. **Bases de données normalisées (3NF)** pour la qualité des données (certification ISO).
4. Accès aux données patients **sécurisé** via **Spring Security** (authentification ; pas d'inscription ni de gestion de droits à mettre en place).
5. Démarche **Green Code** : recherche à mener + suggestions d'actions dans le README + brief en réunion finale.

Notes de cadrage : l'interface doit rester **sobre**. Le microservice front et la gateway sont **communs aux 3 sprints** et évoluent au fil des besoins. Les bases de données restent **simples**.

## Découpage des sprints

### Sprint 1 — Microservice patient, gateway, première interface

Mettre en place l'infrastructure microservices dockerisée : un premier microservice back (patient) avec sa base **SQL**, un microservice **gateway** (Spring Cloud Gateway), et un microservice **front**. Le back expose des endpoints REST pour gérer le dossier patient.

**User stories :**
- *Vue des infos personnelles des patients* : en tant qu'organisateur, voir les informations personnelles d'un patient pour vérifier son identité. Informations : prénom, nom, date de naissance, genre, adresse postale, numéro de téléphone.
- *Mise à jour des informations personnelles* : en tant qu'organisateur, mettre à jour les informations d'un patient.
- *Ajouter des informations personnelles* : en tant qu'organisateur, ajouter un patient. **L'adresse postale et le numéro de téléphone sont optionnels** pour un dossier valide.

### Sprint 2 — Notes du médecin, base NoSQL

Nouveau microservice back pour la gestion des **notes** (comptes rendus de visite), exposant des endpoints REST. Base **NoSQL (MongoDB)**. Front et gateway mis à jour pour afficher/ajouter les notes dans la page patient.

Contraintes notes : formats d'origine conservés (sauts de ligne, etc.) ; **pas de limite de taille** côté métier ; terminologie médicale commune.

**User stories :**
- *Vue historique du patient* : en tant que praticien, voir l'historique des informations du patient.
- *Ajouter une note à l'historique* : en tant que praticien, ajouter une note d'observation.

### Sprint 3 — Évaluation du risque de diabète

Nouveau microservice **sans base dédiée**, qui interroge les microservices patient et notes pour produire le niveau de risque, affiché sur la page patient. Front et gateway mis à jour.

**User story :**
- *Générer un rapport de diabète* : en tant que praticien, consulter le risque de diabète d'un patient.

## Règles de calcul du niveau de risque

Quatre niveaux : **None**, **Borderline**, **In Danger**, **Early onset**.

- **None** : aucune note contenant de terme déclencheur.
- **Borderline** : entre **deux et cinq** déclencheurs **et** patient de **plus de 30 ans**.
- **In Danger** :
  - homme de moins de 30 ans : **trois** déclencheurs ;
  - femme de moins de 30 ans : **quatre** déclencheurs ;
  - plus de 30 ans : **six ou sept** déclencheurs.
- **Early onset** :
  - homme de moins de 30 ans : **au moins cinq** déclencheurs ;
  - femme de moins de 30 ans : **au moins sept** déclencheurs ;
  - plus de 30 ans : **huit ou plus**.

### Termes déclencheurs à rechercher dans les notes

Hémoglobine A1C, Microalbumine, Taille, Poids, Fumeur / Fumeuse, Anormal, Cholestérol, Vertiges, Rechute, Réaction, Anticorps.

> Interprétation retenue (à valider) : comptage des termes **distincts** (un terme présent plusieurs fois compte une seule fois), recherche **insensible à la casse et aux accents**, variantes gérées (Fumeur/Fumeuse, Vertige/Vertiges). Frontière d'âge : `âge > 30` strict.

## Données de test

### Sprint 1 — Patients

| Nom | Prénom | Date de naissance | Genre | Adresse | Téléphone |
|-----|--------|-------------------|-------|---------|-----------|
| TestNone | Test | 1966-12-31 | F | 1 Brookside St | 100-222-3333 |
| TestBorderline | Test | 1945-06-24 | M | 2 High St | 200-333-4444 |
| TestInDanger | Test | 2004-06-18 | M | 3 Club Road | 300-444-5555 |
| TestEarlyOnset | Test | 2002-06-28 | F | 4 Valley Dr | 400-555-6666 |

### Sprint 2 — Notes des médecins (par patient)

- **TestNone (patId 1)** : « Le patient déclare qu'il se sent très bien. Poids égal ou inférieur au poids recommandé. »
- **TestBorderline (patId 2)** :
  - « Le patient déclare qu'il ressent beaucoup de stress au travail. Il se plaint également que son audition est anormale dernièrement. »
  - « Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois. Il remarque également que son audition continue d'être anormale. »
- **TestInDanger (patId 3)** :
  - « Le patient déclare qu'il fume depuis peu. »
  - « Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière. Il se plaint également de crises d'apnée respiratoire anormales. Tests de laboratoire indiquant un taux de cholestérol LDL élevé. »
- **TestEarlyOnset (patId 4)** :
  - « Le patient déclare qu'il lui est devenu difficile de monter les escaliers. Il se plaint également d'être essoufflé. Tests de laboratoire indiquant que les anticorps sont élevés. Réaction aux médicaments. »
  - « Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps. »
  - « Le patient déclare avoir commencé à fumer depuis peu. Hémoglobine A1C supérieure au niveau recommandé. »
  - « Taille, Poids, Cholestérol, Vertige et Réaction. »

### Sprint 3 — Risques attendus

| patId | Patient | Risque attendu |
|-------|---------|----------------|
| 1 | TestNone | None |
| 2 | TestBorderline | Borderline |
| 3 | TestInDanger | In Danger |
| 4 | TestEarlyOnset | Early onset |

## Livrables attendus (par sprint)

- **Sprint 1** : microservices back (patient + base SQL), gateway (Spring Cloud Gateway), front.
- **Sprint 2** : microservice back notes (MongoDB) + front et gateway mis à jour.
- **Sprint 3** : microservice back évaluation du risque + front et gateway mis à jour.
- **Transverse** : dockerisation de chaque microservice, README (dont section Green Code), authentification Spring Security.
