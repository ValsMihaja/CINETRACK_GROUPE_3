package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import main.Main;
import model.Utilisateur;
import service.AuthentificationService;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardControleur {

    @FXML
    private Label lblUtilisateurConnecte;

    @FXML
    private Label lblRoleBadge;

    @FXML
    private Button btnNavBilletterie;

    @FXML
    private Button btnNavClients;

    @FXML
    private Button btnNavSeances;

    @FXML
    private Button btnNavFilms;

    @FXML
    private Button btnNavSalles;

    @FXML
    private Button btnNavTarifs;

    @FXML
    private Button btnNavStatistiques;

    @FXML
    private Button btnNavAdministration;

    @FXML
    private StackPane contentPane;

    private final AuthentificationService authService = new AuthentificationService();

    @FXML
    public void initialize() {
        Utilisateur u = AuthentificationService.getSession();
        if (u != null) {
            lblUtilisateurConnecte.setText(u.getNomComplet());
            lblRoleBadge.setText(u.getRole());

            btnNavStatistiques.setDisable(!u.estManagerOuPlus());
            btnNavAdministration.setDisable(!u.estAdmin());
        }
        onAfficherBilletterie();
    }

    @FXML
    private void onAfficherBilletterie() {
        chargerVue("/view/billetterie-view.fxml", btnNavBilletterie);
    }

    @FXML
    private void onAfficherClients() {
        chargerVue("/view/clients-view.fxml", btnNavClients);
    }

    @FXML
    private void onAfficherSeances() {
        chargerVue("/view/seances-view.fxml", btnNavSeances);
    }

    @FXML
    private void onAfficherFilms() {
        chargerVue("/view/films-view.fxml", btnNavFilms);
    }

    @FXML
    private void onAfficherSalles() {
        chargerVue("/view/salles-view.fxml", btnNavSalles);
    }

    @FXML
    private void onAfficherTarifs() {
        chargerVue("/view/tarifs-view.fxml", btnNavTarifs);
    }

    @FXML
    private void onAfficherStatistiques() {
        chargerVue("/view/statistiques-view.fxml", btnNavStatistiques);
    }

    @FXML
    private void onAfficherAdministration() {
        chargerVue("/view/administration-view.fxml", btnNavAdministration);
    }

    @FXML
    private void onSeDeconnecter() {
        try {
            authService.deconnecter();
        } catch (SQLException ignored) {
        }
        Main.afficherEcranConnexion();
    }

    private void chargerVue(String fxmlPath, Button boutonActif) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(vue);

            Button[] boutons = {
                    btnNavBilletterie, btnNavClients, btnNavSeances, btnNavFilms,
                    btnNavSalles, btnNavTarifs, btnNavStatistiques, btnNavAdministration
            };
            for (Button btn : boutons) {
                btn.getStyleClass().remove("nav-btn-active");
            }
            if (boutonActif != null) {
                boutonActif.getStyleClass().add("nav-btn-active");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
