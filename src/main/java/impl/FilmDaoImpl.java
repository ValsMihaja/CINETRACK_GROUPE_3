package impl;

import dao.FilmDao;
import database.DatabaseConnection;
import model.Film;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FilmDaoImpl implements FilmDao {

    @Override
    public List<Film> findAll() throws SQLException {
        List<Film> films = new ArrayList<>();
        String sql = "SELECT id_film, titre, genre, duree, note, synopsis FROM films ORDER BY id_film ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                films.add(new Film(
                        rs.getInt("id_film"),
                        rs.getString("titre"),
                        rs.getString("genre"),
                        rs.getInt("duree"),
                        rs.getDouble("note"),
                        rs.getString("synopsis")
                ));
            }
        }
        return films;
    }
}
