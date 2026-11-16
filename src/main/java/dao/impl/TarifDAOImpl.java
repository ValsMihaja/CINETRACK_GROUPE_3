package dao.impl;

import dao.ITarifDAO;
import database.DatabaseConnection;
import model.Tarif;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC/PostgreSQL de ITarifDAO.
 */
public class TarifDAOImpl implements ITarifDAO {

    @Override
    public List<Tarif> findAll() throws SQLException {
        List<Tarif> tarifs = new ArrayList<>();
        String sql = "SELECT id_tarif, categorie, libelle, prix_unitaire, description, actif "
                + "FROM tarifs ORDER BY id_tarif ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tarifs.add(mapperTarif(rs));
            }
        }
        return tarifs;
    }

    @Override
    public List<Tarif> findActifs() throws SQLException {
        List<Tarif> tarifs = new ArrayList<>();
        String sql = "SELECT id_tarif, categorie, libelle, prix_unitaire, description, actif "
                + "FROM tarifs WHERE actif = TRUE ORDER BY id_tarif ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tarifs.add(mapperTarif(rs));
            }
        }
        return tarifs;
    }

    @Override
    public Tarif findById(int id) throws SQLException {
        String sql = "SELECT id_tarif, categorie, libelle, prix_unitaire, description, actif "
                + "FROM tarifs WHERE id_tarif = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperTarif(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Tarif findByCategorie(String categorie) throws SQLException {
        String sql = "SELECT id_tarif, categorie, libelle, prix_unitaire, description, actif "
                + "FROM tarifs WHERE categorie = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categorie);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperTarif(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean insert(Tarif tarif) throws SQLException {
        String sql = "INSERT INTO tarifs (categorie, libelle, prix_unitaire, description, actif) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tarif.getCategorie());
            ps.setString(2, tarif.getLibelle());
            ps.setDouble(3, tarif.getPrixUnitaire());
            ps.setString(4, tarif.getDescription());
            ps.setBoolean(5, tarif.isActif());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Tarif tarif) throws SQLException {
        String sql = "UPDATE tarifs SET libelle = ?, prix_unitaire = ?, description = ?, actif = ? "
                + "WHERE id_tarif = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tarif.getLibelle());
            ps.setDouble(2, tarif.getPrixUnitaire());
            ps.setString(3, tarif.getDescription());
            ps.setBoolean(4, tarif.isActif());
            ps.setInt(5, tarif.getIdTarif());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM tarifs WHERE id_tarif = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Tarif mapperTarif(ResultSet rs) throws SQLException {
        return new Tarif(
                rs.getInt("id_tarif"),
                rs.getString("categorie"),
                rs.getString("libelle"),
                rs.getDouble("prix_unitaire"),
                rs.getString("description"),
                rs.getBoolean("actif")
        );
    }
}
