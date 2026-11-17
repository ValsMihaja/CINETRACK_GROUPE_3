package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Seance;
import service.SeanceService;

import java.sql.SQLException;
import java.util.List;

public class SeanceControleur {

    @FXML
    private Label lblMessage;

    @FXML
    private TableView<Seance> tableSeances;

    @FXML
    private TableColumn<Seance, Integer> colId;

    @FXML
    private TableColumn<Seance, String> colFilm;

    @FXML
    private TableColumn<Seance, String> colSalle;

    @FXML
    private TableColumn<Seance, String> colDate;

    @FXML
    private TableColumn<Seance, String> colHeure;

    @FXML
    private TableColumn<Seance, Integer> colPlaces;

    private final SeanceService seanceService = new SeanceService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idSeance"));
        colFilm.setCellValueFactory(new PropertyValueFactory<>("titreFilm"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("nomSalle"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateTexte"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureTexte"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("placesDispo"));

        rafraichirTable();
    }

    @FXML
    private void rafraichirTable() {
        try {
            List<Seance> seances = seanceService.listerSeances();
            tableSeances.setItems(FXCollections.observableArrayList(seances));
            lblMessage.setText("");
        } catch (SQLException e) {
            lblMessage.setText("Erreur base de données : " + e.getMessage());
        }
    }
}
