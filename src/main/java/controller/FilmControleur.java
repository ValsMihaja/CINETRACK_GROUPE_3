package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Film;
import service.FilmService;

import java.sql.SQLException;
import java.util.List;

public class FilmControleur {

    @FXML
    private Label lblMessage;

    @FXML
    private TableView<Film> tableFilms;

    @FXML
    private TableColumn<Film, Integer> colId;

    @FXML
    private TableColumn<Film, String> colTitre;

    @FXML
    private TableColumn<Film, String> colGenre;

    @FXML
    private TableColumn<Film, Integer> colDuree;

    @FXML
    private TableColumn<Film, Double> colNote;

    private final FilmService filmService = new FilmService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFilm"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        rafraichirTable();
    }

    @FXML
    private void rafraichirTable() {
        try {
            List<Film> films = filmService.listerFilms();
            tableFilms.setItems(FXCollections.observableArrayList(films));
            lblMessage.setText("");
        } catch (SQLException e) {
            lblMessage.setText("Erreur base de données : " + e.getMessage());
        }
    }
}
