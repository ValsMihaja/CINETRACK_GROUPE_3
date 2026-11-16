package controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import main.AppContext;
import main.Main;
import model.Utilisateur;
import service.AuthentificationService;
import service.StatistiqueEtSauvegardeService;

import java.io.IOException;
import java.sql.SQLException;

/**
 * ====================================================================
 * COUCHE : CONTROLLER (Tableau de Bord Principal & Navigation par Rôle)
 * ====================================================================
 * RÔLE :
 * Contrôle la fenêtre principale (dashboard-view.fxml), applique les droits
 * d'accès selon l'héritage des acteurs :
 * - CAISSIER : Billetterie, Réservations, Clients, Consultation du planning
 * - MANAGER (hérite de Caissier) : + Films, Salles, Programmation Séances, Tarifs, Statistiques
 * - ADMIN (hérite de Manager) : + Gestion des Utilisateurs, Logs de sécurité, Sauvegarde BDD
 */
public class DashboardControleur {

    @FXML
    private Label lblUtilisateurConnecte;

    @FXML
    private Label lblRoleBadge;

    @FXML
    private Button btnNavBilletterie;

    @FXML
    private Button btnNavProgrammation;

    @FXML
    private Button btnNavStatistiques;

    @FXML
    private StackPane contentPane;

    private final AuthentificationService authentificationService;
    private final StatistiqueEtSauvegardeService statistiqueService;

    public DashboardControleur(AuthentificationService authentificationService,
                               StatistiqueEtSauvegardeService statistiqueService) {
        this.authentificationService = authentificationService;
        this.statistiqueService = statistiqueService;
    }

    public DashboardControleur() {
        this(
                AppContext.getInstance().getAuthentificationService(),
                AppContext.getInstance().getStatistiqueService()
        );
    }

    @FXML
    public void initialize() {
        Utilisateur u = authentificationService.getUtilisateurConnecte();
        if (u != null) {
            lblUtilisateurConnecte.setText(u.getNomComplet() + " (@" + u.getLogin() + ")");
            lblRoleBadge.setText(u.getRole());

            // Gestion des droits d'accès selon le rôle :
            // Le Caissier a accès à la Billetterie et à la consultation du programme,
            // tandis que les statistiques/administration sont réservées au Manager et à l'Admin.
            boolean estManagerOuAdmin = u.estManagerOuPlus();
            btnNavStatistiques.setDisable(!estManagerOuAdmin);
        }

        // Afficher l'écran de Billetterie par défaut au démarrage
        onAfficherBilletterie();
    }

    @FXML
    private void onAfficherBilletterie() {
        chargerSousVue("/view/billetterie-view.fxml", btnNavBilletterie);
    }

    @FXML
    private void onAfficherProgrammation() {
        chargerSousVue("/view/programmation-view.fxml", btnNavProgrammation);
    }

    @FXML
    private void onAfficherStatistiques() {
        chargerSousVue("/view/statistiques-view.fxml", btnNavStatistiques);
    }

    @FXML
    private void onSeDeconnecter() {
        try {
            authentificationService.deconnecter();
        } catch (SQLException ignored) {
        }
        Main.afficherEcranConnexion();
    }

    private void chargerSousVue(String fxmlPath, Button boutonActif) {
        try {
            Parent vue = AppContext.getInstance().chargerVue(fxmlPath);
            contentPane.getChildren().setAll(vue);

            btnNavBilletterie.getStyleClass().remove("nav-btn-active");
            btnNavProgrammation.getStyleClass().remove("nav-btn-active");
            btnNavStatistiques.getStyleClass().remove("nav-btn-active");
            if (boutonActif != null && !boutonActif.getStyleClass().contains("nav-btn-active")) {
                boutonActif.getStyleClass().add("nav-btn-active");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
