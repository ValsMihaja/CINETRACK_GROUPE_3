package dao;

import model.LogSecurite;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Interface DAO pour la journalisation de sécurité (CC4) et les statistiques d'exploitation.
 */
public interface ILogSecuriteDAO {

    void enregistrerLog(Integer idUtilisateur, String login, String action,
                        String tableConcernee, String donneesConcernees) throws SQLException;

    List<LogSecurite> findAll() throws SQLException;

    double getChiffreAffairesTotal() throws SQLException;

    int getTotalBilletsVendus() throws SQLException;

    int getTotalReservationsConfirmees() throws SQLException;

    List<Map<String, Object>> getTopFilms() throws SQLException;

    List<Map<String, Object>> getTauxOccupationSeances() throws SQLException;

    List<Map<String, Object>> getChiffreAffairesParPeriode() throws SQLException;
}
