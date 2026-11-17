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
    private Button btnNavProgrammation;

    @FXML
    private Button btnNavStatistiques;

    @FXML
    private StackPane contentPane;

    private final AuthentificationService authService = new AuthentificationService();

    @FXML
    public void initialize() {
        Utilisateur u = authService.getUtilisateurConnecte();
        if (u != null) {
            lblUtilisateurConnecte.setText(u.getNomComplet());
            lblRoleBadge.setText(u.getRole());
            btnNavStatistiques.setDisable(!u.estManagerOuPlus());
        }
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
            authService.deconnecter();
        } catch (SQLException ignored) {
        }
        Main.afficherEcranConnexion();
    }

    private void chargerSousVue(String fxmlPath, Button boutonActif) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
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
