package controller;

import exceptions.AuthentificationException;
import exceptions.CompteBloqueException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import main.Main;
import service.AuthentificationService;

import java.sql.SQLException;

public class AuthentificationControleur {

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtMotDePasse;

    @FXML
    private Label lblMessage;

    private final AuthentificationService authService = new AuthentificationService();

    @FXML
    private void onSeConnecter() {
        String login = txtLogin.getText();
        String mdp = txtMotDePasse.getText();

        try {
            authService.authentifier(login, mdp);
            Main.afficherTableauDeBord();
        } catch (CompteBloqueException | AuthentificationException e) {
            afficherMessage(e.getMessage(), true);
            txtMotDePasse.clear();
        } catch (SQLException e) {
            afficherMessage("Erreur base de données : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
