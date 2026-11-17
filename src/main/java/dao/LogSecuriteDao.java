package dao;

import model.LogSecurite;

import java.sql.SQLException;
import java.util.List;

public interface LogSecuriteDao {

    void enregistrerLog(Integer idUtilisateur, String login, String action, String tableConcernee,
                        String donneesConcernees) throws SQLException;

    List<LogSecurite> findAll() throws SQLException;
}
