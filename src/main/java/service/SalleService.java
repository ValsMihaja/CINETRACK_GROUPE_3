package service;

import dao.SalleDao;
import impl.SalleDaoImpl;
import model.Salle;

import java.sql.SQLException;
import java.util.List;

public class SalleService {

    private final SalleDao salleDao;

    public SalleService(SalleDao salleDao) {
        this.salleDao = salleDao;
    }

    public SalleService() {
        this(new SalleDaoImpl());
    }

    public List<Salle> listerSalles() throws SQLException {
        return salleDao.findAll();
    }
}
