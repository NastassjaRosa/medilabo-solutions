-- Jeu de donnees de test du sujet OpenClassrooms (docs/sujet-openclassrooms.md).
-- Les ids sont figes a 1-4 car ils sont repris tels quels par les notes (sprint 2)
-- et les risques attendus (sprint 3). INSERT IGNORE evite les doublons/erreurs
-- de cle primaire a chaque redemarrage du conteneur.
INSERT IGNORE INTO patient (id, nom, prenom, date_naissance, genre, adresse, telephone) VALUES
    (1, 'TestNone',       'Test', '1966-12-31', 'F', '1 Brookside St', '100-222-3333'),
    (2, 'TestBorderline', 'Test', '1945-06-24', 'M', '2 High St',      '200-333-4444'),
    (3, 'TestInDanger',   'Test', '2004-06-18', 'M', '3 Club Road',    '300-444-5555'),
    (4, 'TestEarlyOnset', 'Test', '2002-06-28', 'F', '4 Valley Dr',    '400-555-6666');
