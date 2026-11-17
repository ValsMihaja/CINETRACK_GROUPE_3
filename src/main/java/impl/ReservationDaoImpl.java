package impl;

import dao.ReservationDao;
import database.DatabaseConnection;
import model.Reservation;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ReservationDaoImpl implements ReservationDao {

    private static final String SELECT_BASE =
            "SELECT r.id_reservation, r.numero_billet, r.id_seance, "
                    + "CONCAT(f.titre, ' - ', sa.nom, ' (', sc.date_seance, ' ', sc.heure, ')') AS info_seance, "
                    + "r.id_client, CONCAT(c.prenom, ' ', c.nom) AS nom_client, "
                    + "r.id_tarif, t.libelle AS libelle_tarif, r.id_utilisateur, "
                    + "r.nb_places, r.montant_total, r.date_reservation, r.statut "
                    + "FROM reservations r "
                    + "JOIN seances sc ON sc.id_seance = r.id_seance "
                    + "JOIN films f ON f.id_film = sc.id_film "
                    + "JOIN salles sa ON sa.id_salle = sc.id_salle "
                    + "JOIN clients c ON c.id_client = r.id_client "
                    + "JOIN tarifs t ON t.id_tarif = r.id_tarif";

    @Override
    public List<Reservation> findAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY r.id_reservation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapperReservation(rs));
            }
        }
        return reservations;
    }

    @Override
    public Reservation findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.id_reservation = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperReservation(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Reservation findByNumeroBillet(String numeroBillet) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.numero_billet = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroBillet);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperReservation(rs);
                }
            }
        }
        return null;
    }

    @Override
    public String creerReservationSP(int idSeance, int idClient, int idTarif, int idUtilisateur, int nbPlaces)
            throws SQLException {
        String sql = "CALL sp_creer_reservation(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, idSeance);
            cs.setInt(2, idClient);
            cs.setInt(3, idTarif);
            cs.setInt(4, idUtilisateur);
            cs.setInt(5, nbPlaces);
            cs.setNull(6, Types.VARCHAR);
            cs.registerOutParameter(6, Types.VARCHAR);
            cs.setNull(7, Types.NUMERIC);
            cs.registerOutParameter(7, Types.NUMERIC);

            cs.execute();
            return cs.getString(6);
        }
    }

    @Override
    public boolean annulerReservationSP(int idReservation, int idUtilisateur) throws SQLException {
        String sql = "CALL sp_annuler_reservation(?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, idReservation);
            cs.setInt(2, idUtilisateur);
            cs.execute();
            return true;
        }
    }

    @Override
    public boolean insert(Reservation r) throws SQLException {
        String billet = creerReservationSP(
                r.getIdSeance(),
                r.getIdClient(),
                r.getIdTarif(),
                r.getIdUtilisateur() != null ? r.getIdUtilisateur() : 1,
                r.getNbPlaces()
        );
        r.setNumeroBillet(billet);
        return billet != null;
    }

    @Override
    public boolean update(Reservation r) throws SQLException {
        String sql = "UPDATE reservations SET statut = ? WHERE id_reservation = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getStatut());
            ps.setInt(2, r.getIdReservation());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return annulerReservationSP(id, 1);
    }

    private Reservation mapperReservation(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date_reservation");
        BigDecimal total = rs.getBigDecimal("montant_total");
        int idUser = rs.getInt("id_utilisateur");
        Integer idUtilisateurObj = rs.wasNull() ? null : idUser;

        return new Reservation(
                rs.getInt("id_reservation"),
                rs.getString("numero_billet"),
                rs.getInt("id_seance"),
                rs.getString("info_seance"),
                rs.getInt("id_client"),
                rs.getString("nom_client"),
                rs.getInt("id_tarif"),
                rs.getString("libelle_tarif"),
                idUtilisateurObj,
                rs.getInt("nb_places"),
                total != null ? total.doubleValue() : 0.0,
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("statut")
        );
    }
}
