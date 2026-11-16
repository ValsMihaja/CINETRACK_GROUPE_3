-- =============================================================================
-- CINETRACK V2 - JEU DE DONNÉES DE TEST (DML POSTGRESQL)
-- =============================================================================

-- 1. UTILISATEURS (Administrateur, Manager, Caissiers)
INSERT INTO utilisateurs (login, mot_de_passe, nom, prenom, role, tentatives_echouees, actif) VALUES
('admin',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dupont',  'Jean',   'ADMIN',    0, TRUE),
('manager1',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Laurent', 'Marc',   'MANAGER',  0, TRUE),
('caissier1', '$2a$10$Dow1z9F9.y7uX8H/v6T9B.X5O1vK8Z6eR8T2pL4M7N0Q3W5E7R9Y2', 'Martin',  'Marie',  'CAISSIER', 0, TRUE),
('caissier2', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Bernard', 'Lucas',  'CAISSIER', 0, TRUE),
('caissier3', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Moreau',  'Sophie', 'CAISSIER', 3, FALSE);

-- 2. SALLES DE PROJECTION DU CINÉMA LE LUMIÈRE
INSERT INTO salles (nom, capacite, equipement) VALUES
('Salle Apollo',   120, 'Dolby Atmos + Laser 4K'),
('Salle Lumière',   80, '4K + Sous-titres + Son 7.1'),
('Salle Étoile',    50, 'Climatisation + Écran Panoramique'),
('Salle Horizon',  150, 'IMAX 3D + Dolby Surround');

-- 3. CATALOGUE DE FILMS
INSERT INTO films (titre, genre, duree, note, synopsis) VALUES
('Les Gardiens du Temps', 'Action',          130, 8.2, 'Des héros voyagent dans le temps pour sauver le monde d''un paradoxe temporel.'),
('Paris en Rire',         'Comédie',          95, 7.5, 'Un comédien maladroit rate tout ce qu''il entreprend sauf sa vie sentimentale.'),
('Le Dernier Train',      'Drame',           110, 9.0, 'Une femme parcourt l''Europe à la recherche de son frère mystérieusement disparu.'),
('Nuit Blanche à Lyon',   'Thriller',        105, 6.8, 'Un détective insomniaque enquête sur une série de cambriolages simultanés.'),
('La Planète des Bêtes',  'Animation',        88, 8.7, 'Des animaux parlants partent à l''aventure pour retrouver l''oasis sacrée.'),
('Amour et Baguette',     'Comédie',         100, 7.1, 'Une romance inattendue à Paris autour d''une boulangerie artisanale.'),
('Zéro Gravité',          'Science-Fiction', 140, 8.9, 'Deux astronautes dérivent dans l''espace profond après une tempête solaire.'),
('Le Château Oublié',     'Drame',           120, 6.2, 'Un jeune héritier découvre les secrets enfouis d''un manoir familial.');

-- 4. CATÉGORIES TARIFAIRES
INSERT INTO tarifs (categorie, libelle, prix_unitaire, description, actif) VALUES
('PLEIN_TARIF',    'Plein Tarif',    350.00, 'Tarif standard adulte pour toutes les séances',                    TRUE),
('TARIF_REDUIT',   'Tarif Réduit',   250.00, 'Tarif réduit (enfants -12 ans, seniors +60 ans, PMR)',             TRUE),
('TARIF_ETUDIANT', 'Tarif Étudiant', 220.00, 'Tarif préférentiel sur présentation d''une carte étudiante valide', TRUE),
('ABONNEMENT',     'Abonnement',     180.00, 'Tarif abonné Carte CinéPass Le Lumière',                           TRUE);

-- 5. PROGRAMMATION DES SÉANCES (Sans chevauchement d'horaires dans une même salle)
INSERT INTO seances (id_film, id_salle, date_seance, heure, places_dispo) VALUES
(1, 1, '2026-11-06', '10:00', 120),
(4, 1, '2026-11-06', '14:00', 120),
(7, 1, '2026-11-06', '18:00', 120),
(2, 2, '2026-11-06', '11:00',  80),
(3, 2, '2026-11-06', '15:00',  80),
(5, 3, '2026-11-06', '14:30',  50),
(6, 4, '2026-11-06', '20:00', 150),
(5, 2, '2026-11-07', '10:00',  80),
(6, 3, '2026-11-07', '11:00',  50),
(7, 4, '2026-11-07', '14:00', 150),
(1, 2, '2026-11-07', '16:00',  80),
(8, 1, '2026-11-08', '09:30', 120),
(3, 2, '2026-11-08', '13:30',  80),
(2, 4, '2026-11-08', '20:00', 150);

-- 6. FICHES CLIENTS
INSERT INTO clients (nom, prenom, email, telephone, date_inscription) VALUES
('Ramdenee', 'Kévin',   'kevin.ramdenee@email.mu',   '+230 5711 2233', '2026-10-15'),
('Lefèvre',  'Camille', 'camille.lefevre@email.mu',  '+230 5822 3344', '2026-10-18'),
('Appadoo',  'Nadia',   'nadia.appadoo@email.mu',    '+230 5933 4455', '2026-10-20'),
('Perrier',  'Thomas',  'thomas.perrier@email.mu',   '+230 5744 5566', '2026-10-25'),
('Wong',     'Chloé',   'chloe.wong@email.mu',       '+230 5855 6677', '2026-11-01'),
('Naidoo',   'Rohan',   'rohan.naidoo@email.mu',     '+230 5966 7788', '2026-11-02');

-- 7. RÉSERVATIONS ET BILLETS (Test des procédures stockées et triggers de mise à jour des places)
DO $$
DECLARE
    v_id_res INT;
    v_billet VARCHAR;
    v_total  DECIMAL(8,2);
BEGIN
    CALL sp_creer_reservation(1, 1, 1, 3, 2, v_id_res, v_billet, v_total);
    CALL sp_creer_reservation(1, 2, 3, 3, 3, v_id_res, v_billet, v_total);
    CALL sp_creer_reservation(6, 3, 2, 4, 4, v_id_res, v_billet, v_total);
    CALL sp_creer_reservation(3, 4, 4, 3, 2, v_id_res, v_billet, v_total);
    CALL sp_creer_reservation(7, 5, 1, 4, 5, v_id_res, v_billet, v_total);
    CALL sp_creer_reservation(5, 6, 1, 3, 2, v_id_res, v_billet, v_total);
    CALL sp_annuler_reservation(v_id_res, 3);
END;
$$;

-- 8. EXEMPLE DE MODIFICATION TARIFAIRE JOURNALISÉE
UPDATE tarifs SET prix_unitaire = 360.00 WHERE categorie = 'PLEIN_TARIF';

-- 9. EXEMPLE D'AUTHENTIFICATION JOURNALISÉE (Via procédure stockée)
CALL sp_enregistrer_tentative_connexion('admin', TRUE);
CALL sp_enregistrer_tentative_connexion('manager1', TRUE);
CALL sp_enregistrer_tentative_connexion('caissier1', TRUE);
