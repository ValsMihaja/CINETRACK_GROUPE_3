package dao;

import model.Tarif;

import java.sql.SQLException;
import java.util.List;

public interface TarifDao {

    List<Tarif> findAll() throws SQLException;
}
