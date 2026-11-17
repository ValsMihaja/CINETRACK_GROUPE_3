package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Tarif;
import service.TarifService;

import java.sql.SQLException;
import java.util.List;

public class TarifControleur {

    @FXML
    private Label lblMessage;

    @FXML
    private TableView<Tarif> tableTarifs;

    @FXML
    private TableColumn<Tarif, Integer> colId;

    @FXML
    private TableColumn<Tarif, String> colCategorie;

    @FXML
    private TableColumn<Tarif, String> colLibelle;

    @FXML
    private TableColumn<Tarif, String> colPrix;

    @FXML
    private TableColumn<Tarif, String> colDescription;

    private final TarifService tarifService = new TarifService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idTarif"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixTexte"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        rafraichirTable();
    }

    @FXML
    private void rafraichirTable() {
        try {
            List<Tarif> tarifs = tarifService.listerTarifs();
            tableTarifs.setItems(FXCollections.observableArrayList(tarifs));
            lblMessage.setText("");
        } catch (SQLException e) {
            lblMessage.setText("Erreur base de données : " + e.getMessage());
        }
    }
}
