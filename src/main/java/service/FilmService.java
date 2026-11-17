package service;

import dao.FilmDao;
import impl.FilmDaoImpl;
import model.Film;

import java.sql.SQLException;
import java.util.List;

public class FilmService {

    private final FilmDao filmDao;

    public FilmService(FilmDao filmDao) {
        this.filmDao = filmDao;
    }

    public FilmService() {
        this(new FilmDaoImpl());
    }

    public List<Film> listerFilms() throws SQLException {
        return filmDao.findAll();
    }
}
