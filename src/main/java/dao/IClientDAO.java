package dao;

import model.Client;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface DAO pour la gestion des fiches clients.
 */
public interface IClientDAO extends IDAO<Client> {

    List<Client> rechercher(String motCle) throws SQLException;
}
