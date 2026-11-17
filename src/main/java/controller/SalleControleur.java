package controller;

import exceptions.CineTrackException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.Salle;
import model.Utilisateur;
import service.AuthentificationService;
import service.ProgrammationService;

import java.sql.SQLException;

public class SalleControleur {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtCapacite;

    @FXML
    private TextField txtEquipement;

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    @FXML
    private Label lblMessage;

    @FXML
    private TableView<Salle> tableSalles;

    @FXML
    private TableColumn<Salle, Integer> colId;

    @FXML
    private TableColumn<Salle, String> colNom;

    @FXML
    private TableColumn<Salle, Integer> colCapacite;

    @FXML
    private TableColumn<Salle, String> colEquipement;

    private final ProgrammationService programmationService = new ProgrammationService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idSalle"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colEquipement.setCellValueFactory(new PropertyValueFactory<>("equipement"));

        tableSalles.getSelectionModel().selectedItemProperty().addListener((obs, ancien, s) -> {
            if (s != null) {
                txtNom.setText(s.getNom());
                txtCapacite.setText(String.valueOf(s.getCapacite()));
                txtEquipement.setText(s.getEquipement());
            }
        });

        Utilisateur u = AuthentificationService.getSession();
        boolean peutModifier = u != null && u.estManagerOuPlus();
        btnAjouter.setDisable(!peutModifier);
        btnModifier.setDisable(!peutModifier);
        btnSupprimer.setDisable(!peutModifier);

        rafraichirTable();
    }

    @FXML
    private void onAjouter() {
        int capacite;
        try {
            capacite = Integer.parseInt(txtCapacite.getText().trim());
        } catch (NumberFormatException e) {
            afficherMessage("La capacité doit être un nombre entier.", true);
            return;
        }

        try {
            programmationService.ajouterSalle(txtNom.getText(), capacite, txtEquipement.getText());
            txtNom.clear();
            txtCapacite.clear();
            txtEquipement.clear();
            afficherMessage("Salle ajoutée.", false);
            rafraichirTable();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onModifier() {
        Salle sel = tableSalles.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez une salle dans le tableau.", true);
            return;
        }

        int capacite;
        try {
            capacite = Integer.parseInt(txtCapacite.getText().trim());
        } catch (NumberFormatException e) {
            afficherMessage("Capacité invalide.", true);
            return;
        }

        try {
            programmationService.modifierSalle(sel.getIdSalle(), txtNom.getText(), capacite, txtEquipement.getText());
            afficherMessage("Salle modifiée.", false);
            rafraichirTable();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onSupprimer() {
        Salle sel = tableSalles.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez une salle à supprimer.", true);
            return;
        }

        try {
            programmationService.supprimerSalle(sel.getIdSalle(), AuthentificationService.getSession());
            txtNom.clear();
            txtCapacite.clear();
            txtEquipement.clear();
            afficherMessage("Salle supprimée.", false);
            rafraichirTable();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    private void rafraichirTable() {
        try {
            tableSalles.setItems(FXCollections.observableArrayList(programmationService.listerSalles()));
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
