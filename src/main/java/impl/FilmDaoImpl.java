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
                films.add(mapperFilm(rs));
            }
        }
        return films;
    }

    @Override
    public Film findById(int id) throws SQLException {
        String sql = "SELECT id_film, titre, genre, duree, note, synopsis FROM films WHERE id_film = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapperFilm(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean insert(Film film) throws SQLException {
        String sql = "INSERT INTO films (titre, genre, duree, note, synopsis) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, film.getTitre());
            ps.setString(2, film.getGenre());
            ps.setInt(3, film.getDuree());
            ps.setDouble(4, film.getNote());
            ps.setString(5, film.getSynopsis());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Film film) throws SQLException {
        String sql = "UPDATE films SET titre = ?, genre = ?, duree = ?, note = ?, synopsis = ? WHERE id_film = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, film.getTitre());
            ps.setString(2, film.getGenre());
            ps.setInt(3, film.getDuree());
            ps.setDouble(4, film.getNote());
            ps.setString(5, film.getSynopsis());
            ps.setInt(6, film.getIdFilm());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM films WHERE id_film = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Film> rechercher(String motCleTitre, String genre) throws SQLException {
        List<Film> films = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id_film, titre, genre, duree, note, synopsis FROM films WHERE 1=1"
        );
        boolean filtreTitre = motCleTitre != null && !motCleTitre.trim().isEmpty();
        boolean filtreGenre = genre != null && !genre.trim().isEmpty() && !"TOUS".equalsIgnoreCase(genre);

        if (filtreTitre) {
            sql.append(" AND LOWER(titre) LIKE LOWER(?)");
        }
        if (filtreGenre) {
            sql.append(" AND LOWER(genre) = LOWER(?)");
        }
        sql.append(" ORDER BY titre ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (filtreTitre) {
                ps.setString(idx++, "%" + motCleTitre.trim() + "%");
            }
            if (filtreGenre) {
                ps.setString(idx, genre.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    films.add(mapperFilm(rs));
                }
            }
        }
        return films;
    }

    @Override
    public List<String> findAllGenres() throws SQLException {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM films ORDER BY genre ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }
        }
        return genres;
    }

    private Film mapperFilm(ResultSet rs) throws SQLException {
        return new Film(
                rs.getInt("id_film"),
                rs.getString("titre"),
                rs.getString("genre"),
                rs.getInt("duree"),
                rs.getDouble("note"),
                rs.getString("synopsis")
        );
    }
}
