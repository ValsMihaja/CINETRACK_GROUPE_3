package dao;

import model.Film;

import java.sql.SQLException;
import java.util.List;

public interface FilmDao {

    List<Film> findAll() throws SQLException;
}
