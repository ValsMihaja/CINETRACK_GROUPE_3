package controller;

import exceptions.CineTrackException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.Film;
import model.Utilisateur;
import service.AuthentificationService;
import service.ProgrammationService;

import java.sql.SQLException;

public class FilmControleur {

    @FXML
    private TextField txtTitre;

    @FXML
    private TextField txtGenre;

    @FXML
    private TextField txtDuree;

    @FXML
    private TextField txtNote;

    @FXML
    private TextArea txtSynopsis;

    @FXML
    private TextField txtRechercheTitre;

    @FXML
    private TextField txtRechercheGenre;

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

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

    private final ProgrammationService programmationService = new ProgrammationService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFilm"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        tableFilms.getSelectionModel().selectedItemProperty().addListener((obs, ancien, f) -> {
            if (f != null) {
                txtTitre.setText(f.getTitre());
                txtGenre.setText(f.getGenre());
                txtDuree.setText(String.valueOf(f.getDuree()));
                txtNote.setText(String.valueOf(f.getNote()));
                txtSynopsis.setText(f.getSynopsis());
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
        int duree;
        double note;
        try {
            duree = Integer.parseInt(txtDuree.getText().trim());
            note = txtNote.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtNote.getText().trim());
        } catch (NumberFormatException e) {
            afficherMessage("La durée et la note doivent être numériques.", true);
            return;
        }

        try {
            programmationService.ajouterFilm(
                    txtTitre.getText(), txtGenre.getText(), duree, note, txtSynopsis.getText()
            );
            viderChamps();
            afficherMessage("Film ajouté.", false);
            rafraichirTable();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onModifier() {
        Film sel = tableFilms.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un film dans le tableau.", true);
            return;
        }

        int duree;
        double note;
        try {
            duree = Integer.parseInt(txtDuree.getText().trim());
            note = Double.parseDouble(txtNote.getText().trim());
        } catch (NumberFormatException e) {
            afficherMessage("Durée ou note invalide.", true);
            return;
        }

        try {
            programmationService.modifierFilm(
                    sel.getIdFilm(), txtTitre.getText(), txtGenre.getText(), duree, note, txtSynopsis.getText()
            );
            afficherMessage("Film modifié.", false);
            rafraichirTable();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onSupprimer() {
        Film sel = tableFilms.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un film à supprimer.", true);
            return;
        }

        try {
            programmationService.supprimerFilm(sel.getIdFilm(), AuthentificationService.getSession());
            viderChamps();
            afficherMessage("Film supprimé.", false);
            rafraichirTable();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onRechercher() {
        try {
            tableFilms.setItems(FXCollections.observableArrayList(
                    programmationService.rechercherFilms(txtRechercheTitre.getText(), txtRechercheGenre.getText())
            ));
        } catch (SQLException e) {
            afficherMessage("Erreur recherche : " + e.getMessage(), true);
        }
    }

    private void rafraichirTable() {
        try {
            tableFilms.setItems(FXCollections.observableArrayList(programmationService.listerFilms()));
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void viderChamps() {
        txtTitre.clear();
        txtGenre.clear();
        txtDuree.clear();
        txtNote.clear();
        txtSynopsis.clear();
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
