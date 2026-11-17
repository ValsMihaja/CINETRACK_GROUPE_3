package impl;

import dao.SalleDao;
import database.DatabaseConnection;
import model.Salle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalleDaoImpl implements SalleDao {

    @Override
    public List<Salle> findAll() throws SQLException {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT id_salle, nom, capacite, equipement FROM salles ORDER BY id_salle ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                salles.add(new Salle(
                        rs.getInt("id_salle"),
                        rs.getString("nom"),
                        rs.getInt("capacite"),
                        rs.getString("equipement")
                ));
            }
        }
        return salles;
    }
}
