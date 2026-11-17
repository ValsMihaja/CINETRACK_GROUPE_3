package dao;

import model.Seance;

import java.sql.SQLException;
import java.util.List;

public interface SeanceDao {

    List<Seance> findAll() throws SQLException;
}
