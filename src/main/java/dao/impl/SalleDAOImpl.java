package dao.impl;

import dao.ISalleDAO;
import database.DatabaseConnection;
import model.Salle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC/PostgreSQL de ISalleDAO.
 */
public class SalleDAOImpl implements ISalleDAO {

    @Override
    public List<Salle> findAll() throws SQLException {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT id_salle, nom, capacite, equipement FROM salles ORDER BY id_salle ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                salles.add(mapperSalle(rs));
            }
        }
        return salles;
    }

    @Override
    public Salle findById(int id) throws SQLException {
        String sql = "SELECT id_salle, nom, capacite, equipement FROM salles WHERE id_salle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperSalle(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Salle findByNom(String nom) throws SQLException {
        String sql = "SELECT id_salle, nom, capacite, equipement FROM salles WHERE LOWER(nom) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperSalle(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean insert(Salle salle) throws SQLException {
        String sql = "INSERT INTO salles (nom, capacite, equipement) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, salle.getNom());
            ps.setInt(2, salle.getCapacite());
            ps.setString(3, salle.getEquipement());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Salle salle) throws SQLException {
        String sql = "UPDATE salles SET nom = ?, capacite = ?, equipement = ? WHERE id_salle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, salle.getNom());
            ps.setInt(2, salle.getCapacite());
            ps.setString(3, salle.getEquipement());
            ps.setInt(4, salle.getIdSalle());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM salles WHERE id_salle = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Salle mapperSalle(ResultSet rs) throws SQLException {
        return new Salle(
                rs.getInt("id_salle"),
                rs.getString("nom"),
                rs.getInt("capacite"),
                rs.getString("equipement")
        );
    }
}
