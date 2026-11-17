package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.StatFilm;
import model.StatOccupation;
import service.StatistiqueEtSauvegardeService;

import java.io.File;
import java.sql.SQLException;

public class StatistiqueControleur {

    @FXML
    private Label lblKpiChiffreAffaires;

    @FXML
    private Label lblKpiBilletsVendus;

    @FXML
    private Label lblKpiReservations;

    @FXML
    private Label lblMessage;

    @FXML
    private TableView<StatFilm> tableTopFilms;

    @FXML
    private TableColumn<StatFilm, String> colTopTitre;

    @FXML
    private TableColumn<StatFilm, String> colTopGenre;

    @FXML
    private TableColumn<StatFilm, String> colTopNote;

    @FXML
    private TableColumn<StatFilm, Integer> colTopPlaces;

    @FXML
    private TableColumn<StatFilm, String> colTopCA;

    @FXML
    private TableView<StatOccupation> tableOccupation;

    @FXML
    private TableColumn<StatOccupation, String> colOccDate;

    @FXML
    private TableColumn<StatOccupation, String> colOccHeure;

    @FXML
    private TableColumn<StatOccupation, String> colOccSalle;

    @FXML
    private TableColumn<StatOccupation, String> colOccFilm;

    @FXML
    private TableColumn<StatOccupation, String> colOccPlaces;

    @FXML
    private TableColumn<StatOccupation, String> colOccTaux;

    private final StatistiqueEtSauvegardeService statistiqueService = new StatistiqueEtSauvegardeService();

    @FXML
    public void initialize() {
        colTopTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTopGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colTopNote.setCellValueFactory(new PropertyValueFactory<>("noteTexte"));
        colTopPlaces.setCellValueFactory(new PropertyValueFactory<>("placesVendues"));
        colTopCA.setCellValueFactory(new PropertyValueFactory<>("chiffreAffairesTexte"));

        colOccDate.setCellValueFactory(new PropertyValueFactory<>("dateSeance"));
        colOccHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colOccSalle.setCellValueFactory(new PropertyValueFactory<>("salle"));
        colOccFilm.setCellValueFactory(new PropertyValueFactory<>("film"));
        colOccPlaces.setCellValueFactory(new PropertyValueFactory<>("occupationTexte"));
        colOccTaux.setCellValueFactory(new PropertyValueFactory<>("tauxTexte"));

        rafraichir();
    }

    @FXML
    private void onExporterCSV() {
        try {
            File fichier = new File("exports/rapport_exploitation_cinetrack.csv");
            statistiqueService.exporterCSV(fichier.getAbsolutePath());
            afficherMessage("Export CSV créé : " + fichier.getPath(), false);
        } catch (Exception e) {
            afficherMessage("Erreur export CSV : " + e.getMessage(), true);
        }
    }

    @FXML
    private void rafraichir() {
        try {
            lblKpiChiffreAffaires.setText(String.format("%.2f Rs", statistiqueService.obtenirChiffreAffairesTotal()));
            lblKpiBilletsVendus.setText(String.valueOf(statistiqueService.obtenirTotalBilletsVendus()));
            lblKpiReservations.setText(String.valueOf(statistiqueService.obtenirTotalReservationsConfirmees()));

            tableTopFilms.setItems(FXCollections.observableArrayList(statistiqueService.obtenirTopFilms()));
            tableOccupation.setItems(FXCollections.observableArrayList(statistiqueService.obtenirTauxOccupationSeances()));
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
