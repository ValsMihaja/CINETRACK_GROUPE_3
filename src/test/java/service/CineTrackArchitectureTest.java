package service;

import database.DatabaseConnection;
import impl.ClientDaoImpl;
import impl.FilmDaoImpl;
import impl.SalleDaoImpl;
import impl.SeanceDaoImpl;
import impl.TarifDaoImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CineTrackArchitectureTest {

    @Test
    void testConnexionPostgreSQL() {
        assertTrue(DatabaseConnection.testConnection());
    }

    @Test
    void testArchitectureMVCetInjection() throws Exception {
        FilmService filmService = new FilmService(new FilmDaoImpl());
        SalleService salleService = new SalleService(new SalleDaoImpl());
        SeanceService seanceService = new SeanceService(new SeanceDaoImpl());
        ClientService clientService = new ClientService(new ClientDaoImpl());
        TarifService tarifService = new TarifService(new TarifDaoImpl());

        assertFalse(filmService.listerFilms().isEmpty());
        assertFalse(salleService.listerSalles().isEmpty());
        assertFalse(seanceService.listerSeances().isEmpty());
        assertFalse(clientService.listerClients().isEmpty());
        assertFalse(tarifService.listerTarifs().isEmpty());
    }
}
