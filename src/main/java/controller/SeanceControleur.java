package controller;

import exceptions.CineTrackException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.Film;
import model.Salle;
import model.Seance;
import model.Utilisateur;
import service.AuthentificationService;
import service.ProgrammationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class SeanceControleur {

    @FXML
    private ComboBox<Film> cbFilm;

    @FXML
    private ComboBox<Salle> cbSalle;

    @FXML
    private DatePicker dpDate;

    @FXML
    private TextField txtHeure;

    @FXML
    private DatePicker dpFiltreDate;

    @FXML
    private ComboBox<Salle> cbFiltreSalle;

    @FXML
    private ComboBox<Film> cbFiltreFilm;

    @FXML
    private Button btnProgrammer;

    @FXML
    private Button btnSupprimer;

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

    private final ProgrammationService programmationService = new ProgrammationService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idSeance"));
        colFilm.setCellValueFactory(new PropertyValueFactory<>("titreFilm"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("nomSalle"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateTexte"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureTexte"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("placesDispo"));

        dpDate.setValue(LocalDate.now());
        txtHeure.setText("18:00");

        Utilisateur u = AuthentificationService.getSession();
        boolean peutModifier = u != null && u.estManagerOuPlus();
        btnProgrammer.setDisable(!peutModifier);
        btnSupprimer.setDisable(!peutModifier);

        rafraichirTable();
    }

    @FXML
    private void onProgrammer() {
        Film film = cbFilm.getValue();
        Salle salle = cbSalle.getValue();
        LocalDate date = dpDate.getValue();

        if (film == null || salle == null || date == null) {
            afficherMessage("Choisissez un film, une salle et une date.", true);
            return;
        }

        LocalTime heure;
        try {
            heure = LocalTime.parse(txtHeure.getText().trim());
        } catch (DateTimeParseException e) {
            afficherMessage("Format d'heure invalide (ex: 18:00).", true);
            return;
        }

        try {
            programmationService.programmerSeance(film.getIdFilm(), salle.getIdSalle(), date, heure);
            afficherMessage("Séance programmée.", false);
            rafraichirTable();
        } catch (CineTrackException e) {
            afficherMessage(e.getMessage(), true);
        } catch (SQLException e) {
            afficherMessage("Erreur base de données : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onSupprimer() {
        Seance sel = tableSeances.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez une séance dans le tableau.", true);
            return;
        }

        try {
            programmationService.supprimerSeance(sel.getIdSeance(), AuthentificationService.getSession());
            afficherMessage("Séance supprimée.", false);
            rafraichirTable();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onFiltrer() {
        try {
            LocalDate date = dpFiltreDate.getValue();
            Integer idSalle = cbFiltreSalle.getValue() != null ? cbFiltreSalle.getValue().getIdSalle() : null;
            Integer idFilm = cbFiltreFilm.getValue() != null ? cbFiltreFilm.getValue().getIdFilm() : null;
            tableSeances.setItems(FXCollections.observableArrayList(
                    programmationService.rechercherSeances(date, idSalle, idFilm)
            ));
        } catch (SQLException e) {
            afficherMessage("Erreur filtrage : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onReinitialiserFiltres() {
        dpFiltreDate.setValue(null);
        cbFiltreSalle.setValue(null);
        cbFiltreFilm.setValue(null);
        rafraichirTable();
    }

    private void rafraichirTable() {
        try {
            cbFilm.setItems(FXCollections.observableArrayList(programmationService.listerFilms()));
            cbSalle.setItems(FXCollections.observableArrayList(programmationService.listerSalles()));
            cbFiltreFilm.setItems(cbFilm.getItems());
            cbFiltreSalle.setItems(cbSalle.getItems());
            tableSeances.setItems(FXCollections.observableArrayList(programmationService.listerSeances()));
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
