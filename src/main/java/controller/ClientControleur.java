package controller;

import exceptions.CineTrackException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.Client;
import service.BilletterieService;

import java.sql.SQLException;
import java.util.List;

public class ClientControleur {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrenom;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtRecherche;

    @FXML
    private Label lblMessage;

    @FXML
    private TableView<Client> tableClients;

    @FXML
    private TableColumn<Client, Integer> colId;

    @FXML
    private TableColumn<Client, String> colNom;

    @FXML
    private TableColumn<Client, String> colPrenom;

    @FXML
    private TableColumn<Client, String> colEmail;

    @FXML
    private TableColumn<Client, String> colTelephone;

    private final BilletterieService billetterieService = new BilletterieService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        rafraichirTable();
    }

    @FXML
    private void onAjouter() {
        try {
            billetterieService.ajouterClient(
                    txtNom.getText(),
                    txtPrenom.getText(),
                    txtEmail.getText(),
                    txtTelephone.getText()
            );
            txtNom.clear();
            txtPrenom.clear();
            txtEmail.clear();
            txtTelephone.clear();
            afficherMessage("Client ajouté avec succès.", false);
            rafraichirTable();
        } catch (CineTrackException e) {
            afficherMessage(e.getMessage(), true);
        } catch (SQLException e) {
            afficherMessage("Erreur base de données : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onRechercher() {
        try {
            List<Client> liste = billetterieService.rechercherClients(txtRecherche.getText());
            tableClients.setItems(FXCollections.observableArrayList(liste));
        } catch (SQLException e) {
            afficherMessage("Erreur recherche : " + e.getMessage(), true);
        }
    }

    private void rafraichirTable() {
        try {
            List<Client> liste = billetterieService.listerClients();
            tableClients.setItems(FXCollections.observableArrayList(liste));
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
