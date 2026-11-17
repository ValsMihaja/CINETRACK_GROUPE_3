package dao;

import model.Client;

import java.sql.SQLException;
import java.util.List;

public interface ClientDao {

    List<Client> findAll() throws SQLException;

    Client findById(int id) throws SQLException;

    boolean insert(Client client) throws SQLException;

    boolean update(Client client) throws SQLException;

    boolean delete(int id) throws SQLException;

    List<Client> rechercher(String motCle) throws SQLException;
}
