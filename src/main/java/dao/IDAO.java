package dao;

import java.sql.SQLException;
import java.util.List;

/**
 * ====================================================================
 * COUCHE : DAO INTERFACE GENERIQUE (IDAO<T>)
 * ====================================================================
 * RÔLE :
 * Contrat générique d'accès aux données (CRUD) permettant le découplage
 * entre la couche Service et l'implémentation concrète JDBC/PostgreSQL,
 * ainsi que l'injection de dépendances.
 *
 * @param <T> Type de l'entité Model manipulée
 */
public interface IDAO<T> {

    List<T> findAll() throws SQLException;

    T findById(int id) throws SQLException;

    boolean insert(T entite) throws SQLException;

    boolean update(T entite) throws SQLException;

    boolean delete(int id) throws SQLException;
}
