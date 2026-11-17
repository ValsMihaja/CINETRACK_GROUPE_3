package controller;

import database.DatabaseConnection;
import exceptions.AuthentificationException;
import exceptions.CompteBloqueException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.Main;
import model.Utilisateur;
import service.AuthentificationService;

import java.sql.SQLException;

public class AuthentificationControleur {

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtMotDePasse;

    @FXML
    private Label lblMessage;

    @FXML
    private Label lblEtatBdd;

    private final AuthentificationService authService = new AuthentificationService();

    @FXML
    public void initialize() {
        boolean bddOk = DatabaseConnection.testConnection();
        if (lblEtatBdd != null) {
            lblEtatBdd.setText(bddOk
                    ? "Base PostgreSQL connectée"
                    : "Base PostgreSQL hors ligne (localhost:5432)");
            lblEtatBdd.getStyleClass().removeAll("badge-success", "badge-danger");
            lblEtatBdd.getStyleClass().add(bddOk ? "badge-success" : "badge-danger");
        }
    }

    @FXML
    private void onSeConnecter() {
        String login = txtLogin.getText();
        String mdp = txtMotDePasse.getText();

        try {
            Utilisateur u = authService.authentifier(login, mdp);
            afficherMessage("Bienvenue " + u.getNomComplet(), false);
            Main.afficherTableauDeBord();
        } catch (CompteBloqueException | AuthentificationException e) {
            afficherMessage(e.getMessage(), true);
            txtMotDePasse.clear();
        } catch (SQLException e) {
            afficherMessage("Erreur base de données : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean erreur) {
        lblMessage.setText(texte);
        lblMessage.getStyleClass().removeAll("msg-error", "msg-success");
        lblMessage.getStyleClass().add(erreur ? "msg-error" : "msg-success");
    }
}
