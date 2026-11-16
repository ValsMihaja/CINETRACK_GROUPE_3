package controller;

import database.DatabaseConnection;
import exceptions.AuthentificationException;
import exceptions.CompteBloqueException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.AppContext;
import main.Main;
import model.Utilisateur;
import service.AuthentificationService;

import java.sql.SQLException;

/**
 * ====================================================================
 * COUCHE : CONTROLLER (Authentification & Contrôle d'Accès)
 * ====================================================================
 * RÔLE :
 * Gère l'écran de connexion (login-view.fxml), délègue la vérification
 * au service AuthentificationService injecté et redirige vers le tableau
 * de bord selon le rôle (CAISSIER, MANAGER, ADMIN).
 */
public class AuthentificationControleur {

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtMotDePasse;

    @FXML
    private Label lblMessage;

    @FXML
    private Label lblEtatBdd;

    private final AuthentificationService authentificationService;

    /**
     * Constructeur avec injection de dépendances.
     */
    public AuthentificationControleur(AuthentificationService authentificationService) {
        this.authentificationService = authentificationService;
    }

    /**
     * Constructeur par défaut requis si instancié directement par FXMLLoader.
     */
    public AuthentificationControleur() {
        this(AppContext.getInstance().getAuthentificationService());
    }

    @FXML
    public void initialize() {
        boolean bddOk = DatabaseConnection.testConnection();
        if (lblEtatBdd != null) {
            lblEtatBdd.setText(bddOk
                    ? "● PostgreSQL connecté (projetcinetrackv2)"
                    : "● PostgreSQL hors ligne — vérifiez le service sur localhost:5432");
            lblEtatBdd.getStyleClass().removeAll("badge-success", "badge-danger");
            lblEtatBdd.getStyleClass().add(bddOk ? "badge-success" : "badge-danger");
        }
    }

    @FXML
    private void onSeConnecter() {
        String login = txtLogin.getText();
        String mdp = txtMotDePasse.getText();

        try {
            Utilisateur u = authentificationService.authentifier(login, mdp);
            AppContext.getInstance().getStatistiqueService().setOperateurCourant(u);
            afficherMessage("Connexion réussie. Bienvenue " + u.getNomComplet() + " !", false);
            Main.afficherTableauDeBord();

        } catch (CompteBloqueException e) {
            afficherMessage("🔒 " + e.getMessage(), true);
            txtMotDePasse.clear();

        } catch (AuthentificationException e) {
            afficherMessage("⚠ " + e.getMessage(), true);
            txtMotDePasse.clear();

        } catch (SQLException e) {
            afficherMessage("Erreur PostgreSQL : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onDemoCaissier() {
        txtLogin.setText("caissier1");
        txtMotDePasse.setText("caissier123");
    }

    @FXML
    private void onDemoManager() {
        txtLogin.setText("manager1");
        txtMotDePasse.setText("manager123");
    }

    @FXML
    private void onDemoAdmin() {
        txtLogin.setText("admin");
        txtMotDePasse.setText("admin123");
    }

    private void afficherMessage(String texte, boolean erreur) {
        lblMessage.setText(texte);
        lblMessage.getStyleClass().removeAll("msg-error", "msg-success");
        lblMessage.getStyleClass().add(erreur ? "msg-error" : "msg-success");
    }
}
