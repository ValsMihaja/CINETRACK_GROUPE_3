package service;

import dao.SeanceDao;
import impl.SeanceDaoImpl;
import model.Seance;

import java.sql.SQLException;
import java.util.List;

public class SeanceService {

    private final SeanceDao seanceDao;

    public SeanceService(SeanceDao seanceDao) {
        this.seanceDao = seanceDao;
    }

    public SeanceService() {
        this(new SeanceDaoImpl());
    }

    public List<Seance> listerSeances() throws SQLException {
        return seanceDao.findAll();
    }
}
