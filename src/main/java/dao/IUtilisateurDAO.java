package dao;

import model.Utilisateur;

import java.sql.SQLException;

/**
 * Interface DAO pour la gestion des utilisateurs internes (ADMIN, MANAGER, CAISSIER).
 */
public interface IUtilisateurDAO extends IDAO<Utilisateur> {

    Utilisateur findByLogin(String login) throws SQLException;

    void incrementerTentatives(String login) throws SQLException;

    void reinitialiserTentatives(int idUtilisateur) throws SQLException;

    boolean debloquerCompte(int idUtilisateur) throws SQLException;
}
