package dao.impl;

import dao.ILogSecuriteDAO;
import database.DatabaseConnection;
import model.LogSecurite;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation JDBC/PostgreSQL de ILogSecuriteDAO.
 * Gère la journalisation de sécurité (CC4) et les requêtes statistiques d'exploitation.
 */
public class LogSecuriteDAOImpl implements ILogSecuriteDAO {

    @Override
    public void enregistrerLog(Integer idUtilisateur, String login, String action,
                               String tableConcernee, String donneesConcernees) throws SQLException {
        String sql = "INSERT INTO logs_securite (id_utilisateur, utilisateur_login, action, table_concernee, donnees_concernees) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idUtilisateur != null && idUtilisateur > 0) {
                ps.setInt(1, idUtilisateur);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, login != null ? login : "SYSTEM");
            ps.setString(3, action);
            ps.setString(4, tableConcernee);
            ps.setString(5, donneesConcernees);
            ps.executeUpdate();
        }
    }

    @Override
    public List<LogSecurite> findAll() throws SQLException {
        List<LogSecurite> logs = new ArrayList<>();
        String sql = "SELECT id_log, id_utilisateur, utilisateur_login, action, "
                + "table_concernee, donnees_concernees, date_heure "
                + "FROM logs_securite ORDER BY id_log DESC LIMIT 200";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idUser = rs.getInt("id_utilisateur");
                Integer idUserObj = rs.wasNull() ? null : idUser;
                Timestamp ts = rs.getTimestamp("date_heure");
                logs.add(new LogSecurite(
                        rs.getInt("id_log"),
                        idUserObj,
                        rs.getString("utilisateur_login"),
                        rs.getString("action"),
                        rs.getString("table_concernee"),
                        rs.getString("donnees_concernees"),
                        ts != null ? ts.toLocalDateTime() : null
                ));
            }
        }
        return logs;
    }

    @Override
    public double getChiffreAffairesTotal() throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant_total), 0.0) AS ca_total "
                + "FROM reservations WHERE statut = 'CONFIRMEE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("ca_total");
            }
        }
        return 0.0;
    }

    @Override
    public int getTotalBilletsVendus() throws SQLException {
        String sql = "SELECT COALESCE(SUM(nb_places), 0) AS total_places "
                + "FROM reservations WHERE statut = 'CONFIRMEE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_places");
            }
        }
        return 0;
    }

    @Override
    public int getTotalReservationsConfirmees() throws SQLException {
        String sql = "SELECT COUNT(*) AS nb_res FROM reservations WHERE statut = 'CONFIRMEE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("nb_res");
            }
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> getTopFilms() throws SQLException {
        List<Map<String, Object>> resultats = new ArrayList<>();
        String sql = "SELECT f.id_film, f.titre, f.genre, f.note AS note_moyenne, "
                + "COUNT(r.id_reservation) FILTER (WHERE r.statut = 'CONFIRMEE') AS nb_reservations, "
                + "COALESCE(SUM(r.nb_places) FILTER (WHERE r.statut = 'CONFIRMEE'), 0) AS total_places_vendues, "
                + "COALESCE(SUM(r.montant_total) FILTER (WHERE r.statut = 'CONFIRMEE'), 0.00) AS chiffre_affaires_film "
                + "FROM films f "
                + "LEFT JOIN seances s ON s.id_film = f.id_film "
                + "LEFT JOIN reservations r ON r.id_seance = s.id_seance "
                + "GROUP BY f.id_film, f.titre, f.genre, f.note "
                + "ORDER BY total_places_vendues DESC, chiffre_affaires_film DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> ligne = new LinkedHashMap<>();
                ligne.put("idFilm", rs.getInt("id_film"));
                ligne.put("titre", rs.getString("titre"));
                ligne.put("genre", rs.getString("genre"));
                ligne.put("noteMoyenne", rs.getDouble("note_moyenne"));
                ligne.put("nbReservations", rs.getInt("nb_reservations"));
                ligne.put("placesVendues", rs.getInt("total_places_vendues"));
                ligne.put("chiffreAffaires", rs.getDouble("chiffre_affaires_film"));
                resultats.add(ligne);
            }
        }
        return resultats;
    }

    @Override
    public List<Map<String, Object>> getTauxOccupationSeances() throws SQLException {
        List<Map<String, Object>> resultats = new ArrayList<>();
        String sql = "SELECT sc.id_seance, sc.date_seance, sc.heure, sa.nom AS salle, f.titre AS film, "
                + "sa.capacite, (sa.capacite - sc.places_dispo) AS places_occupees, sc.places_dispo, "
                + "ROUND(((sa.capacite - sc.places_dispo)::DECIMAL / sa.capacite) * 100, 2) AS taux_occupation_pct "
                + "FROM seances sc "
                + "JOIN salles sa ON sa.id_salle = sc.id_salle "
                + "JOIN films f ON f.id_film = sc.id_film "
                + "ORDER BY sc.date_seance ASC, sc.heure ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> ligne = new LinkedHashMap<>();
                ligne.put("idSeance", rs.getInt("id_seance"));
                ligne.put("dateSeance", rs.getDate("date_seance").toString());
                ligne.put("heure", rs.getTime("heure").toString());
                ligne.put("salle", rs.getString("salle"));
                ligne.put("film", rs.getString("film"));
                ligne.put("capacite", rs.getInt("capacite"));
                ligne.put("placesOccupees", rs.getInt("places_occupees"));
                ligne.put("placesDispo", rs.getInt("places_dispo"));
                ligne.put("tauxOccupation", rs.getDouble("taux_occupation_pct"));
                resultats.add(ligne);
            }
        }
        return resultats;
    }

    @Override
    public List<Map<String, Object>> getChiffreAffairesParPeriode() throws SQLException {
        List<Map<String, Object>> resultats = new ArrayList<>();
        String sql = "SELECT DATE(date_reservation) AS periode, "
                + "COUNT(id_reservation) AS nb_reservations, "
                + "SUM(nb_places) AS places_vendues, "
                + "SUM(montant_total) AS chiffre_affaires "
                + "FROM reservations WHERE statut = 'CONFIRMEE' "
                + "GROUP BY DATE(date_reservation) ORDER BY periode DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> ligne = new LinkedHashMap<>();
                ligne.put("periode", rs.getDate("periode").toString());
                ligne.put("nbReservations", rs.getInt("nb_reservations"));
                ligne.put("placesVendues", rs.getInt("places_vendues"));
                ligne.put("chiffreAffaires", rs.getDouble("chiffre_affaires"));
                resultats.add(ligne);
            }
        }
        return resultats;
    }
}
