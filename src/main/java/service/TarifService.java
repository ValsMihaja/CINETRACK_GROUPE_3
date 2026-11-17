package service;

import dao.TarifDao;
import impl.TarifDaoImpl;
import model.Tarif;

import java.sql.SQLException;
import java.util.List;

public class TarifService {

    private final TarifDao tarifDao;

    public TarifService(TarifDao tarifDao) {
        this.tarifDao = tarifDao;
    }

    public TarifService() {
        this(new TarifDaoImpl());
    }

    public List<Tarif> listerTarifs() throws SQLException {
        return tarifDao.findAll();
    }
}
