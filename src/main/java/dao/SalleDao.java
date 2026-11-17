package dao;

import model.Salle;

import java.sql.SQLException;
import java.util.List;

public interface SalleDao {

    List<Salle> findAll() throws SQLException;
}
