package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Client;
import service.ClientService;

import java.sql.SQLException;
import java.util.List;

public class ClientControleur {

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

    private final ClientService clientService = new ClientService();

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
    private void rafraichirTable() {
        try {
            List<Client> clients = clientService.listerClients();
            tableClients.setItems(FXCollections.observableArrayList(clients));
            lblMessage.setText("");
        } catch (SQLException e) {
            lblMessage.setText("Erreur base de données : " + e.getMessage());
        }
    }
}
