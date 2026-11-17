package impl;

import dao.TarifDao;
import database.DatabaseConnection;
import model.Tarif;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TarifDaoImpl implements TarifDao {

    @Override
    public List<Tarif> findAll() throws SQLException {
        List<Tarif> tarifs = new ArrayList<>();
        String sql = "SELECT id_tarif, categorie, libelle, prix_unitaire, description, actif "
                + "FROM tarifs ORDER BY id_tarif ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tarifs.add(new Tarif(
                        rs.getInt("id_tarif"),
                        rs.getString("categorie"),
                        rs.getString("libelle"),
                        rs.getDouble("prix_unitaire"),
                        rs.getString("description"),
                        rs.getBoolean("actif")
                ));
            }
        }
        return tarifs;
    }
}
