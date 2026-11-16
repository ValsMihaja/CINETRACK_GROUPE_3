package dao;

import model.Film;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface DAO pour la gestion du catalogue de films.
 */
public interface IFilmDAO extends IDAO<Film> {

    List<Film> rechercher(String motCleTitre, String genre) throws SQLException;

    List<String> findAllGenres() throws SQLException;
}
