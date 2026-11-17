package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Salle;
import service.SalleService;

import java.sql.SQLException;
import java.util.List;

public class SalleControleur {

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

    private final SalleService salleService = new SalleService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idSalle"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colEquipement.setCellValueFactory(new PropertyValueFactory<>("equipement"));

        rafraichirTable();
    }

    @FXML
    private void rafraichirTable() {
        try {
            List<Salle> salles = salleService.listerSalles();
            tableSalles.setItems(FXCollections.observableArrayList(salles));
            lblMessage.setText("");
        } catch (SQLException e) {
            lblMessage.setText("Erreur base de données : " + e.getMessage());
        }
    }
}
