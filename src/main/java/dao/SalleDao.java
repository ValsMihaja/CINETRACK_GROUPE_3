package dao;

import model.Salle;

import java.sql.SQLException;
import java.util.List;

public interface SalleDao {

    List<Salle> findAll() throws SQLException;

    Salle findById(int id) throws SQLException;

    Salle findByNom(String nom) throws SQLException;

    boolean insert(Salle salle) throws SQLException;

    boolean update(Salle salle) throws SQLException;

    boolean delete(int id) throws SQLException;
}
