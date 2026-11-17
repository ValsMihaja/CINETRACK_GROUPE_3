package impl;

import dao.LogSecuriteDao;
import database.DatabaseConnection;
import model.LogSecurite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LogSecuriteDaoImpl implements LogSecuriteDao {

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
}
