package dao;

import model.Film;

import java.sql.SQLException;
import java.util.List;

public interface FilmDao {

    List<Film> findAll() throws SQLException;

    Film findById(int id) throws SQLException;

    boolean insert(Film film) throws SQLException;

    boolean update(Film film) throws SQLException;

    boolean delete(int id) throws SQLException;

    List<Film> rechercher(String motCleTitre, String genre) throws SQLException;

    List<String> findAllGenres() throws SQLException;
}
