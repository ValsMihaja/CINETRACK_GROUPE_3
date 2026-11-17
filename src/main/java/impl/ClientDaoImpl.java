package impl;

import dao.ClientDao;
import database.DatabaseConnection;
import model.Client;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientDaoImpl implements ClientDao {

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT id_client, nom, prenom, email, telephone, date_inscription "
                + "FROM clients ORDER BY id_client ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clients.add(mapperClient(rs));
            }
        }
        return clients;
    }

    @Override
    public Client findById(int id) throws SQLException {
        String sql = "SELECT id_client, nom, prenom, email, telephone, date_inscription "
                + "FROM clients WHERE id_client = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperClient(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean insert(Client client) throws SQLException {
        String sql = "INSERT INTO clients (nom, prenom, email, telephone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getTelephone());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Client client) throws SQLException {
        String sql = "UPDATE clients SET nom = ?, prenom = ?, email = ?, telephone = ? WHERE id_client = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getEmail());
            ps.setString(4, client.getTelephone());
            ps.setInt(5, client.getIdClient());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id_client = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Client> rechercher(String motCle) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT id_client, nom, prenom, email, telephone, date_inscription "
                + "FROM clients WHERE LOWER(nom) LIKE LOWER(?) OR LOWER(prenom) LIKE LOWER(?) "
                + "OR LOWER(COALESCE(email, '')) LIKE LOWER(?) ORDER BY nom ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String motif = "%" + (motCle != null ? motCle.trim() : "") + "%";
            ps.setString(1, motif);
            ps.setString(2, motif);
            ps.setString(3, motif);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapperClient(rs));
                }
            }
        }
        return clients;
    }

    private Client mapperClient(ResultSet rs) throws SQLException {
        Date d = rs.getDate("date_inscription");
        return new Client(
                rs.getInt("id_client"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("telephone"),
                d != null ? d.toLocalDate() : null
        );
    }
}
