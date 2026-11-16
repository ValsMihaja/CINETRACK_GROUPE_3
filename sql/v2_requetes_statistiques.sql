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
