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
