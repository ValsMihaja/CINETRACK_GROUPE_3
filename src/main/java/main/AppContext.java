package main;

import controller.AuthentificationControleur;
import controller.BilletterieControleur;
import controller.DashboardControleur;
import controller.ProgrammationControleur;
import controller.StatistiqueControleur;
import dao.IClientDAO;
import dao.IFilmDAO;
import dao.ILogSecuriteDAO;
import dao.IReservationDAO;
import dao.ISalleDAO;
import dao.ISeanceDAO;
import dao.ITarifDAO;
import dao.IUtilisateurDAO;
import dao.impl.ClientDAOImpl;
import dao.impl.FilmDAOImpl;
import dao.impl.LogSecuriteDAOImpl;
import dao.impl.ReservationDAOImpl;
import dao.impl.SalleDAOImpl;
import dao.impl.SeanceDAOImpl;
import dao.impl.TarifDAOImpl;
import dao.impl.UtilisateurDAOImpl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import service.AuthentificationService;
import service.BilletterieService;
import service.ProgrammationService;
import service.StatistiqueEtSauvegardeService;

import java.io.IOException;
import java.net.URL;

/**
 * ====================================================================
 * CONTENEUR D'INJECTION DE DÉPENDANCES (IoC / Dependency Injection)
 * ====================================================================
 * RÔLE :
 * Centralise l'assemblage des couches de l'architecture MVC :
 *   1. Instancie les implémentations DAO JDBC/PostgreSQL (dao.impl.*)
 *   2. Les injecte dans les Services métier via leurs interfaces (dao.I*DAO)
 *   3. Injecte les Services dans les Contrôleurs JavaFX via FXMLLoader.setControllerFactory
 */
public class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    // Couche DAO (Interfaces -> Implémentations JDBC/PostgreSQL)
    private final IUtilisateurDAO utilisateurDAO;
    private final IFilmDAO filmDAO;
    private final ISalleDAO salleDAO;
    private final ISeanceDAO seanceDAO;
    private final IClientDAO clientDAO;
    private final ITarifDAO tarifDAO;
    private final IReservationDAO reservationDAO;
    private final ILogSecuriteDAO logSecuriteDAO;

    // Couche Service (injectée avec les interfaces DAO)
    private final AuthentificationService authentificationService;
    private final ProgrammationService programmationService;
    private final BilletterieService billetterieService;
    private final StatistiqueEtSauvegardeService statistiqueService;

    private AppContext() {
        // 1. Instanciation des implémentations DAO JDBC/PostgreSQL
        this.utilisateurDAO = new UtilisateurDAOImpl();
        this.filmDAO = new FilmDAOImpl();
        this.salleDAO = new SalleDAOImpl();
        this.seanceDAO = new SeanceDAOImpl();
        this.clientDAO = new ClientDAOImpl();
        this.tarifDAO = new TarifDAOImpl();
        this.reservationDAO = new ReservationDAOImpl();
        this.logSecuriteDAO = new LogSecuriteDAOImpl();

        // 2. Injection des DAO dans les Services métier (Constructor Injection)
        this.authentificationService = new AuthentificationService(utilisateurDAO, logSecuriteDAO);
        this.programmationService = new ProgrammationService(filmDAO, salleDAO, seanceDAO, logSecuriteDAO);
        this.billetterieService = new BilletterieService(
                reservationDAO, seanceDAO, clientDAO, tarifDAO, logSecuriteDAO
        );
        this.statistiqueService = new StatistiqueEtSauvegardeService(logSecuriteDAO);
    }

    public static AppContext getInstance() {
        return INSTANCE;
    }

    /**
     * Charge une vue FXML en injectant automatiquement les Services dans le Controller JavaFX.
     */
    public Parent chargerVue(String cheminFxml) throws IOException {
        URL fxmlUrl = getClass().getResource(cheminFxml);
        if (fxmlUrl == null) {
            throw new IllegalStateException("Fichier FXML introuvable : " + cheminFxml);
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setControllerFactory(this::creerControleurAvecInjection);
        return loader.load();
    }

    /**
     * Fabrique de contrôleurs réalisant l'injection de dépendances par constructeur.
     */
    public Object creerControleurAvecInjection(Class<?> typeControleur) {
        if (typeControleur == AuthentificationControleur.class) {
            return new AuthentificationControleur(authentificationService);
        }
        if (typeControleur == DashboardControleur.class) {
            return new DashboardControleur(authentificationService, statistiqueService);
        }
        if (typeControleur == BilletterieControleur.class) {
            return new BilletterieControleur(billetterieService, programmationService, authentificationService);
        }
        if (typeControleur == ProgrammationControleur.class) {
            return new ProgrammationControleur(programmationService, billetterieService, authentificationService);
        }
        if (typeControleur == StatistiqueControleur.class) {
            return new StatistiqueControleur(statistiqueService, authentificationService);
        }
        try {
            return typeControleur.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'instancier le contrôleur : " + typeControleur.getName(), e);
        }
    }

    public AuthentificationService getAuthentificationService() {
        return authentificationService;
    }

    public ProgrammationService getProgrammationService() {
        return programmationService;
    }

    public BilletterieService getBilletterieService() {
        return billetterieService;
    }

    public StatistiqueEtSauvegardeService getStatistiqueService() {
        return statistiqueService;
    }
}
