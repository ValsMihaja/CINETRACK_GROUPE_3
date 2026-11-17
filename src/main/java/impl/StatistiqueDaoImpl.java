package impl;

import dao.StatistiqueDao;
import database.DatabaseConnection;
import model.StatFilm;
import model.StatOccupation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatistiqueDaoImpl implements StatistiqueDao {

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
    public List<StatFilm> getTopFilms() throws SQLException {
        List<StatFilm> resultats = new ArrayList<>();
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
                resultats.add(new StatFilm(
                        rs.getInt("id_film"),
                        rs.getString("titre"),
                        rs.getString("genre"),
                        rs.getDouble("note_moyenne"),
                        rs.getInt("nb_reservations"),
                        rs.getInt("total_places_vendues"),
                        rs.getDouble("chiffre_affaires_film")
                ));
            }
        }
        return resultats;
    }

    @Override
    public List<StatOccupation> getTauxOccupationSeances() throws SQLException {
        List<StatOccupation> resultats = new ArrayList<>();
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
                resultats.add(new StatOccupation(
                        rs.getInt("id_seance"),
                        rs.getDate("date_seance").toString(),
                        rs.getTime("heure").toString(),
                        rs.getString("salle"),
                        rs.getString("film"),
                        rs.getInt("capacite"),
                        rs.getInt("places_occupees"),
                        rs.getInt("places_dispo"),
                        rs.getDouble("taux_occupation_pct")
                ));
            }
        }
        return resultats;
    }
}
