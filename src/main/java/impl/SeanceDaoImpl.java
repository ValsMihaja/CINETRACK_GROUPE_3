package impl;

import dao.SeanceDao;
import database.DatabaseConnection;
import model.Seance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeanceDaoImpl implements SeanceDao {

    @Override
    public List<Seance> findAll() throws SQLException {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT sc.id_seance, sc.id_film, f.titre AS titre_film, f.duree AS duree_film, "
                + "sc.id_salle, sa.nom AS nom_salle, sa.capacite AS capacite_salle, "
                + "sc.date_seance, sc.heure, sc.places_dispo "
                + "FROM seances sc "
                + "JOIN films f ON f.id_film = sc.id_film "
                + "JOIN salles sa ON sa.id_salle = sc.id_salle "
                + "ORDER BY sc.date_seance ASC, sc.heure ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                seances.add(new Seance(
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
                ));
            }
        }
        return seances;
    }
}
