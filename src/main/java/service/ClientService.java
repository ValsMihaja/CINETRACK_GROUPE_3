package service;

import dao.ClientDao;
import impl.ClientDaoImpl;
import model.Client;

import java.sql.SQLException;
import java.util.List;

public class ClientService {

    private final ClientDao clientDao;

    public ClientService(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public ClientService() {
        this(new ClientDaoImpl());
    }

    public List<Client> listerClients() throws SQLException {
        return clientDao.findAll();
    }
}
