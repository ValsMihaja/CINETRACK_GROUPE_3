package controller;

import exceptions.CineTrackException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.LogSecurite;
import model.Utilisateur;
import service.AuthentificationService;
import service.StatistiqueEtSauvegardeService;

import java.io.File;
import java.sql.SQLException;

public class AdministrationControleur {

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtMotDePasse;

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrenom;

    @FXML
    private ComboBox<String> cbRole;

    @FXML
    private Label lblMessage;

    @FXML
    private TableView<Utilisateur> tableUtilisateurs;

    @FXML
    private TableColumn<Utilisateur, Integer> colUserId;

    @FXML
    private TableColumn<Utilisateur, String> colUserLogin;

    @FXML
    private TableColumn<Utilisateur, String> colUserNom;

    @FXML
    private TableColumn<Utilisateur, String> colUserRole;

    @FXML
    private TableColumn<Utilisateur, String> colUserStatut;

    @FXML
    private TableView<LogSecurite> tableLogs;

    @FXML
    private TableColumn<LogSecurite, Integer> colLogId;

    @FXML
    private TableColumn<LogSecurite, String> colLogDate;

    @FXML
    private TableColumn<LogSecurite, String> colLogUser;

    @FXML
    private TableColumn<LogSecurite, String> colLogAction;

    @FXML
    private TableColumn<LogSecurite, String> colLogDetails;

    private final AuthentificationService authService = new AuthentificationService();
    private final StatistiqueEtSauvegardeService sauvegardeService = new StatistiqueEtSauvegardeService();

    @FXML
    public void initialize() {
        cbRole.setItems(FXCollections.observableArrayList("CAISSIER", "MANAGER", "ADMIN"));
        cbRole.getSelectionModel().selectFirst();

        colUserId.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colUserLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colUserNom.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserStatut.setCellValueFactory(new PropertyValueFactory<>("statutTexte"));

        colLogId.setCellValueFactory(new PropertyValueFactory<>("idLog"));
        colLogDate.setCellValueFactory(new PropertyValueFactory<>("dateTexte"));
        colLogUser.setCellValueFactory(new PropertyValueFactory<>("utilisateurLogin"));
        colLogAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colLogDetails.setCellValueFactory(new PropertyValueFactory<>("donneesConcernees"));

        rafraichir();
    }

    @FXML
    private void onCreerUtilisateur() {
        try {
            authService.ajouterUtilisateur(
                    txtLogin.getText(),
                    txtMotDePasse.getText(),
                    txtNom.getText(),
                    txtPrenom.getText(),
                    cbRole.getValue()
            );
            txtLogin.clear();
            txtMotDePasse.clear();
            txtNom.clear();
            txtPrenom.clear();
            afficherMessage("Utilisateur créé.", false);
            rafraichir();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onDebloquerUtilisateur() {
        Utilisateur sel = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un utilisateur.", true);
            return;
        }
        try {
            authService.debloquerUtilisateur(sel.getIdUtilisateur());
            afficherMessage("Compte débloqué.", false);
            rafraichir();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onSupprimerUtilisateur() {
        Utilisateur sel = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un utilisateur.", true);
            return;
        }
        try {
            authService.supprimerUtilisateur(sel.getIdUtilisateur());
            afficherMessage("Utilisateur supprimé.", false);
            rafraichir();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onSauvegarderBDD() {
        try {
            File dossier = new File("backups");
            sauvegardeService.executerSauvegardeBDD(dossier.getAbsolutePath());
            afficherMessage("Sauvegarde créée dans : " + dossier.getPath(), false);
            rafraichir();
        } catch (Exception e) {
            afficherMessage("Erreur sauvegarde : " + e.getMessage(), true);
        }
    }

    private void rafraichir() {
        try {
            tableUtilisateurs.setItems(FXCollections.observableArrayList(authService.listerUtilisateurs()));
            tableLogs.setItems(FXCollections.observableArrayList(sauvegardeService.consulterLogsSecurite()));
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
