-- Schema de la table patient (base MySQL patientdb, conforme 3NF).
-- Execute avant l'initialisation de Hibernate (spring.jpa.hibernate.ddl-auto=validate
-- se contente de verifier la coherence entre ce schema et l'entite Patient).
CREATE TABLE IF NOT EXISTS patient (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    prenom          VARCHAR(50)  NOT NULL,
    nom             VARCHAR(50)  NOT NULL,
    date_naissance  DATE         NOT NULL,
    genre           CHAR(1)      NOT NULL,
    adresse         VARCHAR(255),
    telephone       VARCHAR(20)
);
