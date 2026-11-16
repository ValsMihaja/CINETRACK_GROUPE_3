-- =============================================================================
-- CINETRACK V2 - CRÉATION DES 8 TABLES MÉTIER (DDL POSTGRESQL)
-- Client : Cinéma Le Lumière | Prestataire : DevCore
-- Contrainte respectée : 8 tables (entre 5 et 10 tables)
-- =============================================================================

DROP TABLE IF EXISTS logs_securite CASCADE;
DROP TABLE IF EXISTS historique_tarifs CASCADE;
DROP TABLE IF EXISTS avis CASCADE;
DROP TABLE IF EXISTS reservations CASCADE;
DROP TABLE IF EXISTS tarifs CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS seances CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS salles CASCADE;
DROP TABLE IF EXISTS utilisateurs CASCADE;

-- =============================================================================
-- 1. TABLE UTILISATEURS (Personnel interne : Administrateur, Manager, Caissier)
-- =============================================================================
CREATE TABLE utilisateurs (
    id_utilisateur          SERIAL PRIMARY KEY,
    login                   VARCHAR(50)  NOT NULL UNIQUE,
    mot_de_passe            VARCHAR(255) NOT NULL, -- Haché (BCrypt)
    nom                     VARCHAR(80)  NOT NULL,
    prenom                  VARCHAR(80)  DEFAULT '',
    role                    VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'CAISSIER')),
    tentatives_echouees     INT          NOT NULL DEFAULT 0 CHECK (tentatives_echouees >= 0),
    actif                   BOOLEAN      NOT NULL DEFAULT TRUE,
    date_derniere_connexion TIMESTAMP
);

-- =============================================================================
-- 2. TABLE SALLES (Salles de projection et capacités)
-- =============================================================================
CREATE TABLE salles (
    id_salle   SERIAL PRIMARY KEY,
    nom        VARCHAR(50)  NOT NULL UNIQUE,
    capacite   INT          NOT NULL CHECK (capacite > 0),
    equipement VARCHAR(100)
);

-- =============================================================================
-- 3. TABLE FILMS (Catalogue des films)
-- =============================================================================
CREATE TABLE films (
    id_film  SERIAL PRIMARY KEY,
    titre    VARCHAR(100) NOT NULL,
    genre    VARCHAR(50)  NOT NULL,
    duree    INT          NOT NULL CHECK (duree > 0), -- Durée en minutes
    note     DECIMAL(3,1) DEFAULT 0.0 CHECK (note >= 0 AND note <= 10),
    synopsis TEXT
);

-- =============================================================================
-- 4. TABLE SEANCES (Programmation des films dans les salles)
-- =============================================================================
CREATE TABLE seances (
    id_seance    SERIAL PRIMARY KEY,
    id_film      INT  NOT NULL REFERENCES films(id_film) ON DELETE CASCADE,
    id_salle     INT  NOT NULL REFERENCES salles(id_salle) ON DELETE CASCADE,
    date_seance  DATE NOT NULL,
    heure        TIME NOT NULL,
    places_dispo INT  NOT NULL CHECK (places_dispo >= 0),
    UNIQUE (id_salle, date_seance, heure)
);

-- =============================================================================
-- 5. TABLE CLIENTS (Fiches clients gérées au guichet par le Caissier)
-- =============================================================================
CREATE TABLE clients (
    id_client        SERIAL PRIMARY KEY,
    nom              VARCHAR(80)  NOT NULL,
    prenom           VARCHAR(80)  NOT NULL,
    email            VARCHAR(120) UNIQUE,
    telephone        VARCHAR(30),
    date_inscription DATE         NOT NULL DEFAULT CURRENT_DATE
);

-- =============================================================================
-- 6. TABLE TARIFS (Catégories tarifaires : Plein tarif, Réduit, Étudiant, Abonnement)
-- =============================================================================
CREATE TABLE tarifs (
    id_tarif      SERIAL PRIMARY KEY,
    categorie     VARCHAR(30)   NOT NULL UNIQUE CHECK (categorie IN ('PLEIN_TARIF', 'TARIF_REDUIT', 'TARIF_ETUDIANT', 'ABONNEMENT')),
    libelle       VARCHAR(60)   NOT NULL,
    prix_unitaire DECIMAL(8,2)  NOT NULL CHECK (prix_unitaire >= 0),
    description   VARCHAR(200),
    actif         BOOLEAN       NOT NULL DEFAULT TRUE
);

-- =============================================================================
-- 7. TABLE RESERVATIONS (Billetterie, réservations et billets uniques)
-- =============================================================================
CREATE TABLE reservations (
    id_reservation   SERIAL PRIMARY KEY,
    numero_billet    VARCHAR(30)  NOT NULL UNIQUE,
    id_seance        INT          NOT NULL REFERENCES seances(id_seance) ON DELETE CASCADE,
    id_client        INT          NOT NULL REFERENCES clients(id_client) ON DELETE RESTRICT,
    id_tarif         INT          NOT NULL REFERENCES tarifs(id_tarif) ON DELETE RESTRICT,
    id_utilisateur   INT          REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL,
    nb_places        INT          NOT NULL CHECK (nb_places > 0),
    montant_total    DECIMAL(8,2) NOT NULL CHECK (montant_total >= 0),
    date_reservation TIMESTAMP    NOT NULL DEFAULT NOW(),
    statut           VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMEE' CHECK (statut IN ('CONFIRMEE', 'ANNULEE'))
);

-- =============================================================================
-- 8. TABLE LOGS_SECURITE (CC4 - Journalisation des actions sensibles)
-- =============================================================================
CREATE TABLE logs_securite (
    id_log             SERIAL PRIMARY KEY,
    id_utilisateur     INT          REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL,
    utilisateur_login  VARCHAR(50)  NOT NULL DEFAULT 'SYSTEM',
    action             VARCHAR(50)  NOT NULL,
    table_concernee    VARCHAR(50),
    donnees_concernees TEXT         NOT NULL,
    date_heure         TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Index pour accélérer les recherches et statistiques
CREATE INDEX idx_seances_date ON seances(date_seance);
CREATE INDEX idx_seances_film ON seances(id_film);
CREATE INDEX idx_seances_salle ON seances(id_salle);
CREATE INDEX idx_reservations_seance ON reservations(id_seance);
CREATE INDEX idx_reservations_date ON reservations(date_reservation);

-- =============================================================================
-- CINETRACK V2 - DÉCLENCHEURS (TRIGGERS) ET PROCÉDURES STOCKÉES
-- Client : Cinéma Le Lumière | Prestataire : DevCore
-- =============================================================================

-- =============================================================================
-- 1. TRIGGER : DÉTECTION AUTOMATIQUE DES CONFLITS DE PROGRAMMATION DE SÉANCE
--    & INITIALISATION DES PLACES DISPONIBLES
-- =============================================================================
CREATE OR REPLACE FUNCTION fn_verifier_conflit_seance()
RETURNS TRIGGER AS $$
DECLARE
    v_duree_nouvelle INT;
    v_capacite_salle INT;
    v_conflit_id     INT;
    v_conflit_titre  VARCHAR(100);
    v_conflit_heure  TIME;
BEGIN
    SELECT duree INTO v_duree_nouvelle FROM films WHERE id_film = NEW.id_film;
    SELECT capacite INTO v_capacite_salle FROM salles WHERE id_salle = NEW.id_salle;

    IF TG_OP = 'INSERT' AND (NEW.places_dispo IS NULL OR NEW.places_dispo > v_capacite_salle) THEN
        NEW.places_dispo := v_capacite_salle;
    END IF;

    SELECT s.id_seance, f.titre, s.heure
    INTO v_conflit_id, v_conflit_titre, v_conflit_heure
    FROM seances s
    JOIN films f ON f.id_film = s.id_film
    WHERE s.id_salle = NEW.id_salle
      AND s.date_seance = NEW.date_seance
      AND (TG_OP = 'INSERT' OR s.id_seance <> NEW.id_seance)
      AND (NEW.heure < (s.heure + (f.duree || ' minutes')::INTERVAL)::TIME)
      AND (s.heure < (NEW.heure + (v_duree_nouvelle || ' minutes')::INTERVAL)::TIME)
    LIMIT 1;

    IF v_conflit_id IS NOT NULL THEN
        RAISE EXCEPTION 'Conflit de planification dans cette salle : chevauchement avec la séance #% (%) prévue à %.',
            v_conflit_id, v_conflit_titre, v_conflit_heure;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_verif_conflit_seance ON seances;
CREATE TRIGGER trg_verif_conflit_seance
BEFORE INSERT OR UPDATE ON seances
FOR EACH ROW
EXECUTE FUNCTION fn_verifier_conflit_seance();

-- =============================================================================
-- 2. TRIGGER : MISE À JOUR AUTOMATIQUE DES PLACES DISPONIBLES
--    & CALCUL DU MONTANT TOTAL / NUMÉRO DE BILLET LORS D'UNE RÉSERVATION
-- =============================================================================
CREATE OR REPLACE FUNCTION fn_gerer_places_reservation()
RETURNS TRIGGER AS $$
DECLARE
    v_places_dispo  INT;
    v_prix_unitaire DECIMAL(8,2);
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.numero_billet IS NULL OR TRIM(NEW.numero_billet) = '' THEN
            NEW.numero_billet := 'BIL-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(NEW.id_reservation::TEXT, 4, '0');
        END IF;

        SELECT prix_unitaire INTO v_prix_unitaire FROM tarifs WHERE id_tarif = NEW.id_tarif;
        IF NEW.montant_total IS NULL OR NEW.montant_total = 0 THEN
            NEW.montant_total := v_prix_unitaire * NEW.nb_places;
        END IF;

        IF NEW.statut = 'CONFIRMEE' THEN
            SELECT places_dispo INTO v_places_dispo
            FROM seances
            WHERE id_seance = NEW.id_seance
            FOR UPDATE;

            IF v_places_dispo < NEW.nb_places THEN
                RAISE EXCEPTION 'Capacité insuffisante : seulement % place(s) disponible(s) pour cette séance (demandé : %).',
                    v_places_dispo, NEW.nb_places;
            END IF;

            UPDATE seances
            SET places_dispo = places_dispo - NEW.nb_places
            WHERE id_seance = NEW.id_seance;
        END IF;
        RETURN NEW;

    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.statut = 'CONFIRMEE' AND NEW.statut = 'ANNULEE' THEN
            UPDATE seances
            SET places_dispo = places_dispo + OLD.nb_places
            WHERE id_seance = NEW.id_seance;

            INSERT INTO logs_securite (id_utilisateur, utilisateur_login, action, table_concernee, donnees_concernees)
            VALUES (
                NEW.id_utilisateur,
                COALESCE((SELECT login FROM utilisateurs WHERE id_utilisateur = NEW.id_utilisateur), 'CAISSIER'),
                'ANNULATION_RESERVATION',
                'reservations',
                'Billet ' || NEW.numero_billet || ' annulé (+' || OLD.nb_places || ' places remises sur séance #' || NEW.id_seance || ')'
            );
        END IF;
        RETURN NEW;

    ELSIF TG_OP = 'DELETE' THEN
        IF OLD.statut = 'CONFIRMEE' THEN
            UPDATE seances
            SET places_dispo = places_dispo + OLD.nb_places
            WHERE id_seance = OLD.id_seance;
        END IF;
        RETURN OLD;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_maj_places_reservation ON reservations;
CREATE TRIGGER trg_maj_places_reservation
BEFORE INSERT OR UPDATE OR DELETE ON reservations
FOR EACH ROW
EXECUTE FUNCTION fn_gerer_places_reservation();

-- =============================================================================
-- 3. TRIGGER : JOURNALISATION DES MODIFICATIONS TARIFAIRES DANS LOGS_SECURITE
-- =============================================================================
CREATE OR REPLACE FUNCTION fn_journaliser_modification_tarif()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.prix_unitaire <> NEW.prix_unitaire THEN
        INSERT INTO logs_securite (utilisateur_login, action, table_concernee, donnees_concernees)
        VALUES (
            CURRENT_USER,
            'MODIFICATION_TARIF',
            'tarifs',
            'Tarif ' || NEW.categorie || ' (' || NEW.libelle || ') modifié : ' || OLD.prix_unitaire || ' Rs -> ' || NEW.prix_unitaire || ' Rs'
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_historique_tarifs ON tarifs;
CREATE TRIGGER trg_historique_tarifs
AFTER UPDATE ON tarifs
FOR EACH ROW
EXECUTE FUNCTION fn_journaliser_modification_tarif();

-- =============================================================================
-- 4. TRIGGER : JOURNALISATION DE SÉCURITÉ SUR SUPPRESSION DE FILMS ET SALLES (CC4)
-- =============================================================================
CREATE OR REPLACE FUNCTION fn_journaliser_suppression_sensible()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'films' THEN
        INSERT INTO logs_securite (utilisateur_login, action, table_concernee, donnees_concernees)
        VALUES (
            CURRENT_USER,
            'SUPPRESSION_FILM',
            'films',
            'Film supprimé : ID=' || OLD.id_film || ', Titre=' || OLD.titre || ', Genre=' || OLD.genre
        );
    ELSIF TG_TABLE_NAME = 'salles' THEN
        INSERT INTO logs_securite (utilisateur_login, action, table_concernee, donnees_concernees)
        VALUES (
            CURRENT_USER,
            'SUPPRESSION_SALLE',
            'salles',
            'Salle supprimée : ID=' || OLD.id_salle || ', Nom=' || OLD.nom || ', Capacité=' || OLD.capacite
        );
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_log_suppression_film ON films;
CREATE TRIGGER trg_log_suppression_film
AFTER DELETE ON films
FOR EACH ROW
EXECUTE FUNCTION fn_journaliser_suppression_sensible();

DROP TRIGGER IF EXISTS trg_log_suppression_salle ON salles;
CREATE TRIGGER trg_log_suppression_salle
AFTER DELETE ON salles
FOR EACH ROW
EXECUTE FUNCTION fn_journaliser_suppression_sensible();

-- =============================================================================
-- 5. PROCÉDURE STOCKÉE 1 : CRÉER UNE RÉSERVATION ET GÉNÉRER UN BILLET UNIQUE
-- =============================================================================
CREATE OR REPLACE PROCEDURE sp_creer_reservation(
    IN  p_id_seance      INT,
    IN  p_id_client      INT,
    IN  p_id_tarif       INT,
    IN  p_id_utilisateur INT,
    IN  p_nb_places      INT,
    INOUT p_id_reservation INT DEFAULT NULL,
    INOUT p_numero_billet  VARCHAR DEFAULT NULL,
    INOUT p_montant_total  DECIMAL(8,2) DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_prix_unitaire DECIMAL(8,2);
BEGIN
    SELECT prix_unitaire INTO v_prix_unitaire
    FROM tarifs
    WHERE id_tarif = p_id_tarif AND actif = TRUE;

    IF v_prix_unitaire IS NULL THEN
        RAISE EXCEPTION 'Tarif introuvable ou inactif (ID=%).', p_id_tarif;
    END IF;

    p_montant_total := v_prix_unitaire * p_nb_places;

    INSERT INTO reservations (id_seance, id_client, id_tarif, id_utilisateur, nb_places, montant_total, statut)
    VALUES (p_id_seance, p_id_client, p_id_tarif, p_id_utilisateur, p_nb_places, p_montant_total, 'CONFIRMEE')
    RETURNING id_reservation, numero_billet INTO p_id_reservation, p_numero_billet;
END;
$$;

-- =============================================================================
-- 6. PROCÉDURE STOCKÉE 2 : ANNULER UNE RÉSERVATION
-- =============================================================================
CREATE OR REPLACE PROCEDURE sp_annuler_reservation(
    IN p_id_reservation INT,
    IN p_id_utilisateur INT DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_statut VARCHAR(20);
BEGIN
    SELECT statut INTO v_statut
    FROM reservations
    WHERE id_reservation = p_id_reservation;

    IF v_statut IS NULL THEN
        RAISE EXCEPTION 'Réservation introuvable (ID=%).', p_id_reservation;
    ELSIF v_statut = 'ANNULEE' THEN
        RAISE EXCEPTION 'La réservation #% est déjà annulée.', p_id_reservation;
    END IF;

    UPDATE reservations
    SET statut = 'ANNULEE',
        id_utilisateur = COALESCE(p_id_utilisateur, id_utilisateur)
    WHERE id_reservation = p_id_reservation;
END;
$$;

-- =============================================================================
-- 7. PROCÉDURE STOCKÉE 3 : GESTION DE L'AUTHENTIFICATION ET BLOCAGE APRÈS 3 ÉCHECS
-- =============================================================================
CREATE OR REPLACE PROCEDURE sp_enregistrer_tentative_connexion(
    IN p_login  VARCHAR,
    IN p_succes BOOLEAN
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_user    INT;
    v_tentatives INT;
BEGIN
    SELECT id_utilisateur, tentatives_echouees
    INTO v_id_user, v_tentatives
    FROM utilisateurs
    WHERE login = p_login;

    IF v_id_user IS NULL THEN
        INSERT INTO logs_securite (utilisateur_login, action, table_concernee, donnees_concernees)
        VALUES (p_login, 'ECHEC_CONNEXION', 'utilisateurs', 'Tentative de connexion avec un identifiant inconnu');
        RETURN;
    END IF;

    IF p_succes THEN
        UPDATE utilisateurs
        SET tentatives_echouees = 0,
            date_derniere_connexion = NOW()
        WHERE id_utilisateur = v_id_user;

        INSERT INTO logs_securite (id_utilisateur, utilisateur_login, action, table_concernee, donnees_concernees)
        VALUES (v_id_user, p_login, 'CONNEXION_REUSSIE', 'utilisateurs', 'Authentification réussie');
    ELSE
        v_tentatives := v_tentatives + 1;
        IF v_tentatives >= 3 THEN
            UPDATE utilisateurs
            SET tentatives_echouees = v_tentatives,
                actif = FALSE
            WHERE id_utilisateur = v_id_user;

            INSERT INTO logs_securite (id_utilisateur, utilisateur_login, action, table_concernee, donnees_concernees)
            VALUES (v_id_user, p_login, 'COMPTE_BLOQUE', 'utilisateurs', 'Compte bloqué automatiquement après 3 tentatives échouées consécutives');
        ELSE
            UPDATE utilisateurs
            SET tentatives_echouees = v_tentatives
            WHERE id_utilisateur = v_id_user;

            INSERT INTO logs_securite (id_utilisateur, utilisateur_login, action, table_concernee, donnees_concernees)
            VALUES (v_id_user, p_login, 'ECHEC_CONNEXION', 'utilisateurs', 'Tentative échouée (' || v_tentatives || '/3)');
        END IF;
    END IF;
END;
$$;

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

-- =============================================================================
-- CINETRACK V2 - REQUÊTES D'EXPLOITATION ET DE STATISTIQUES
-- Client : Cinéma Le Lumière | Prestataire : DevCore
-- =============================================================================

-- 1. Liste des films les plus réservés sur une période donnée
SELECT 
    f.id_film,
    f.titre,
    f.genre,
    f.note AS note_moyenne,
    COUNT(r.id_reservation) AS nb_reservations,
    COALESCE(SUM(r.nb_places), 0) AS total_places_vendues,
    COALESCE(SUM(r.montant_total), 0.00) AS chiffre_affaires_film
FROM films f
LEFT JOIN seances s ON s.id_film = f.id_film
LEFT JOIN reservations r ON r.id_seance = s.id_seance AND r.statut = 'CONFIRMEE'
WHERE s.date_seance BETWEEN '2026-11-01' AND '2026-11-30' OR s.date_seance IS NULL
GROUP BY f.id_film, f.titre, f.genre, f.note
ORDER BY total_places_vendues DESC, chiffre_affaires_film DESC;

-- 2. Taux d'occupation par salle et par séance
SELECT 
    s.id_seance,
    s.date_seance,
    s.heure,
    sa.nom AS salle,
    f.titre AS film,
    sa.capacite,
    (sa.capacite - s.places_dispo) AS places_occupees,
    s.places_dispo,
    ROUND(((sa.capacite - s.places_dispo)::DECIMAL / sa.capacite) * 100, 2) AS taux_occupation_pct
FROM seances s
JOIN salles sa ON sa.id_salle = s.id_salle
JOIN films f ON f.id_film = s.id_film
ORDER BY s.date_seance, s.heure;

-- 3. Chiffre d'affaires par jour
SELECT 
    DATE(r.date_reservation) AS jour,
    COUNT(r.id_reservation) AS nb_reservations_confirmees,
    SUM(r.nb_places) AS places_vendues,
    SUM(r.montant_total) AS chiffre_affaires_jour
FROM reservations r
WHERE r.statut = 'CONFIRMEE'
GROUP BY DATE(r.date_reservation)
ORDER BY jour DESC;

-- 4. Chiffre d'affaires par mois
SELECT 
    TO_CHAR(r.date_reservation, 'YYYY-MM') AS mois,
    COUNT(r.id_reservation) AS nb_reservations_confirmees,
    SUM(r.nb_places) AS places_vendues,
    SUM(r.montant_total) AS chiffre_affaires_mensuel
FROM reservations r
WHERE r.statut = 'CONFIRMEE'
GROUP BY TO_CHAR(r.date_reservation, 'YYYY-MM')
ORDER BY mois DESC;

-- 5. Consultation du journal de sécurité (CC4)
SELECT * FROM logs_securite ORDER BY date_heure DESC;
