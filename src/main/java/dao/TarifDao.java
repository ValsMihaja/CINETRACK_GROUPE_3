package dao;

import model.Tarif;

import java.sql.SQLException;
import java.util.List;

public interface TarifDao {

    List<Tarif> findAll() throws SQLException;

    List<Tarif> findActifs() throws SQLException;

    Tarif findById(int id) throws SQLException;

    Tarif findByCategorie(String categorie) throws SQLException;

    boolean insert(Tarif tarif) throws SQLException;

    boolean update(Tarif tarif) throws SQLException;

    boolean delete(int id) throws SQLException;
}
