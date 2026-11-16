package dao;

import model.Salle;

import java.sql.SQLException;

/**
 * Interface DAO pour la gestion des salles de projection.
 */
public interface ISalleDAO extends IDAO<Salle> {

    Salle findByNom(String nom) throws SQLException;
}
