package dao;

import model.Utilisateur;

import java.sql.SQLException;
import java.util.List;

public interface UtilisateurDao {

    List<Utilisateur> findAll() throws SQLException;

    Utilisateur findById(int id) throws SQLException;

    Utilisateur findByLogin(String login) throws SQLException;

    boolean insert(Utilisateur utilisateur) throws SQLException;

    boolean update(Utilisateur utilisateur) throws SQLException;

    boolean delete(int id) throws SQLException;

    void incrementerTentatives(String login) throws SQLException;

    void reinitialiserTentatives(int idUtilisateur) throws SQLException;

    boolean debloquerCompte(int idUtilisateur) throws SQLException;
}
