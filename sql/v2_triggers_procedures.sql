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
