package dao;

import model.Tarif;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface DAO pour la gestion des grilles tarifaires.
 */
public interface ITarifDAO extends IDAO<Tarif> {

    List<Tarif> findActifs() throws SQLException;

    Tarif findByCategorie(String categorie) throws SQLException;
}
