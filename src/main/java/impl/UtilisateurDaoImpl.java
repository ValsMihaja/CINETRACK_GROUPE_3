package impl;

import dao.UtilisateurDao;
import database.DatabaseConnection;
import model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDaoImpl implements UtilisateurDao {

    @Override
    public List<Utilisateur> findAll() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT id_utilisateur, login, mot_de_passe, nom, prenom, role, "
                + "tentatives_echouees, actif, date_derniere_connexion "
                + "FROM utilisateurs ORDER BY id_utilisateur ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(mapperUtilisateur(rs));
            }
        }
        return liste;
    }

    @Override
    public Utilisateur findById(int id) throws SQLException {
        String sql = "SELECT id_utilisateur, login, mot_de_passe, nom, prenom, role, "
                + "tentatives_echouees, actif, date_derniere_connexion "
                + "FROM utilisateurs WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperUtilisateur(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Utilisateur findByLogin(String login) throws SQLException {
        String sql = "SELECT id_utilisateur, login, mot_de_passe, nom, prenom, role, "
                + "tentatives_echouees, actif, date_derniere_connexion "
                + "FROM utilisateurs WHERE login = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperUtilisateur(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean insert(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateurs (login, mot_de_passe, nom, prenom, role, tentatives_echouees, actif) "
                + "VALUES (?, ?, ?, ?, ?, 0, TRUE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getLogin());
            ps.setString(2, u.getMotDePasse());
            ps.setString(3, u.getNom());
            ps.setString(4, u.getPrenom());
            ps.setString(5, u.getRole());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateurs SET login = ?, nom = ?, prenom = ?, role = ?, actif = ? "
                + "WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getLogin());
            ps.setString(2, u.getNom());
            ps.setString(3, u.getPrenom());
            ps.setString(4, u.getRole());
            ps.setBoolean(5, u.isActif());
            ps.setInt(6, u.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM utilisateurs WHERE id_utilisateur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void incrementerTentatives(String login) throws SQLException {
        String sql = "UPDATE utilisateurs SET "
                + "tentatives_echouees = tentatives_echouees + 1, "
                + "actif = CASE WHEN tentatives_echouees + 1 >= 3 THEN FALSE ELSE actif END "
                + "WHERE login = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.executeUpdate();
        }
    }

    @Override
    public void reinitialiserTentatives(int idUtilisateur) throws SQLException {
        String sql = "UPDATE utilisateurs SET tentatives_echouees = 0, "
                + "date_derniere_connexion = NOW() WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean debloquerCompte(int idUtilisateur) throws SQLException {
        String sql = "UPDATE utilisateurs SET tentatives_echouees = 0, actif = TRUE "
                + "WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            return ps.executeUpdate() > 0;
        }
    }

    private Utilisateur mapperUtilisateur(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date_derniere_connexion");
        return new Utilisateur(
                rs.getInt("id_utilisateur"),
                rs.getString("login"),
                rs.getString("mot_de_passe"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("role"),
                rs.getInt("tentatives_echouees"),
                rs.getBoolean("actif"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
