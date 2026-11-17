package impl;

import dao.SeanceDao;
import database.DatabaseConnection;
import model.Seance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SeanceDaoImpl implements SeanceDao {

    private static final String SELECT_BASE =
            "SELECT sc.id_seance, sc.id_film, f.titre AS titre_film, f.duree AS duree_film, "
                    + "sc.id_salle, sa.nom AS nom_salle, sa.capacite AS capacite_salle, "
                    + "sc.date_seance, sc.heure, sc.places_dispo "
                    + "FROM seances sc "
                    + "JOIN films f ON f.id_film = sc.id_film "
                    + "JOIN salles sa ON sa.id_salle = sc.id_salle";

    @Override
    public List<Seance> findAll() throws SQLException {
        List<Seance> seances = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY sc.date_seance ASC, sc.heure ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                seances.add(mapperSeance(rs));
            }
        }
        return seances;
    }

    @Override
    public Seance findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE sc.id_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperSeance(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean insert(Seance seance) throws SQLException {
        String sql = "INSERT INTO seances (id_film, id_salle, date_seance, heure, places_dispo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seance.getIdFilm());
            ps.setInt(2, seance.getIdSalle());
            ps.setDate(3, Date.valueOf(seance.getDateSeance()));
            ps.setTime(4, Time.valueOf(seance.getHeure()));
            ps.setInt(5, seance.getPlacesDispo());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Seance seance) throws SQLException {
        String sql = "UPDATE seances SET id_film = ?, id_salle = ?, date_seance = ?, heure = ?, places_dispo = ? "
                + "WHERE id_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seance.getIdFilm());
            ps.setInt(2, seance.getIdSalle());
            ps.setDate(3, Date.valueOf(seance.getDateSeance()));
            ps.setTime(4, Time.valueOf(seance.getHeure()));
            ps.setInt(5, seance.getPlacesDispo());
            ps.setInt(6, seance.getIdSeance());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM seances WHERE id_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Seance> rechercherParCriteres(LocalDate date, Integer idSalle, Integer idFilm) throws SQLException {
        List<Seance> seances = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");

        if (date != null) {
            sql.append(" AND sc.date_seance = ?");
        }
        if (idSalle != null && idSalle > 0) {
            sql.append(" AND sc.id_salle = ?");
        }
        if (idFilm != null && idFilm > 0) {
            sql.append(" AND sc.id_film = ?");
        }
        sql.append(" ORDER BY sc.date_seance ASC, sc.heure ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (date != null) {
                ps.setDate(idx++, Date.valueOf(date));
            }
            if (idSalle != null && idSalle > 0) {
                ps.setInt(idx++, idSalle);
            }
            if (idFilm != null && idFilm > 0) {
                ps.setInt(idx, idFilm);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seances.add(mapperSeance(rs));
                }
            }
        }
        return seances;
    }

    @Override
    public boolean verifierConflitHoraire(int idSalle, LocalDate dateSeance, LocalTime heureDebut,
                                          int dureeMinutes, int idSeanceExclue) throws SQLException {
        String sql = "SELECT sc.heure, f.duree FROM seances sc "
                + "JOIN films f ON f.id_film = sc.id_film "
                + "WHERE sc.id_salle = ? AND sc.date_seance = ? AND sc.id_seance <> ?";

        LocalTime heureFinNouvelle = heureDebut.plusMinutes(dureeMinutes);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSalle);
            ps.setDate(2, Date.valueOf(dateSeance));
            ps.setInt(3, idSeanceExclue);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalTime debutExistante = rs.getTime("heure").toLocalTime();
                    LocalTime finExistante = debutExistante.plusMinutes(rs.getInt("duree"));
                    if (heureDebut.isBefore(finExistante) && heureFinNouvelle.isAfter(debutExistante)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Seance mapperSeance(ResultSet rs) throws SQLException {
        return new Seance(
                rs.getInt("id_seance"),
                rs.getInt("id_film"),
                rs.getString("titre_film"),
                rs.getInt("duree_film"),
                rs.getInt("id_salle"),
                rs.getString("nom_salle"),
                rs.getInt("capacite_salle"),
                rs.getDate("date_seance").toLocalDate(),
                rs.getTime("heure").toLocalTime(),
                rs.getInt("places_dispo")
        );
    }
}
