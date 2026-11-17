package dao;

import model.Client;

import java.sql.SQLException;
import java.util.List;

public interface ClientDao {

    List<Client> findAll() throws SQLException;
}
