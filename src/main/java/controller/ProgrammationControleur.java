package controller;

import exceptions.CineTrackException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Film;
import model.Salle;
import model.Seance;
import model.Tarif;
import model.Utilisateur;
import service.AuthentificationService;
import service.BilletterieService;
import service.ProgrammationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ProgrammationControleur {

    @FXML
    private ComboBox<Film> cbSeanceFilm;

    @FXML
    private ComboBox<Salle> cbSeanceSalle;

    @FXML
    private DatePicker dpSeanceDate;

    @FXML
    private TextField txtSeanceHeure;

    @FXML
    private DatePicker dpFiltreDate;

    @FXML
    private ComboBox<Salle> cbFiltreSalle;

    @FXML
    private ComboBox<Film> cbFiltreFilm;

    @FXML
    private TableView<Seance> tableSeances;

    @FXML
    private TableColumn<Seance, Integer> colIdSeance;

    @FXML
    private TableColumn<Seance, String> colFilmSeance;

    @FXML
    private TableColumn<Seance, String> colSalleSeance;

    @FXML
    private TableColumn<Seance, String> colDateSeance;

    @FXML
    private TableColumn<Seance, String> colHeureSeance;

    @FXML
    private TableColumn<Seance, Integer> colPlacesSeance;

    @FXML
    private Button btnProgrammerSeance;

    @FXML
    private Button btnSupprimerSeance;

    @FXML
    private TextField txtFilmTitre;

    @FXML
    private TextField txtFilmGenre;

    @FXML
    private TextField txtFilmDuree;

    @FXML
    private TextField txtFilmNote;

    @FXML
    private TextArea txtFilmSynopsis;

    @FXML
    private TextField txtRechercheFilmTitre;

    @FXML
    private TextField txtRechercheFilmGenre;

    @FXML
    private TableView<Film> tableFilms;

    @FXML
    private TableColumn<Film, Integer> colIdFilm;

    @FXML
    private TableColumn<Film, String> colTitreFilm;

    @FXML
    private TableColumn<Film, String> colGenreFilm;

    @FXML
    private TableColumn<Film, Integer> colDureeFilm;

    @FXML
    private TableColumn<Film, Double> colNoteFilm;

    @FXML
    private Button btnAjouterFilm;

    @FXML
    private Button btnModifierFilm;

    @FXML
    private Button btnSupprimerFilm;

    @FXML
    private TextField txtSalleNom;

    @FXML
    private TextField txtSalleCapacite;

    @FXML
    private TextField txtSalleEquipement;

    @FXML
    private TableView<Salle> tableSalles;

    @FXML
    private TableColumn<Salle, Integer> colIdSalle;

    @FXML
    private TableColumn<Salle, String> colNomSalle;

    @FXML
    private TableColumn<Salle, Integer> colCapaciteSalle;

    @FXML
    private TableColumn<Salle, String> colEquipementSalle;

    @FXML
    private Button btnAjouterSalle;

    @FXML
    private Button btnModifierSalle;

    @FXML
    private Button btnSupprimerSalle;

    @FXML
    private TextField txtTarifCategorie;

    @FXML
    private TextField txtTarifLibelle;

    @FXML
    private TextField txtTarifPrix;

    @FXML
    private TextField txtTarifDescription;

    @FXML
    private CheckBox chkTarifActif;

    @FXML
    private TableView<Tarif> tableTarifs;

    @FXML
    private TableColumn<Tarif, Integer> colIdTarif;

    @FXML
    private TableColumn<Tarif, String> colCatTarif;

    @FXML
    private TableColumn<Tarif, String> colLibelleTarif;

    @FXML
    private TableColumn<Tarif, String> colPrixTarif;

    @FXML
    private TableColumn<Tarif, String> colDescTarif;

    @FXML
    private Button btnModifierTarif;

    @FXML
    private Label lblMessageProg;

    private final ProgrammationService programmationService = new ProgrammationService();
    private final BilletterieService billetterieService = new BilletterieService();
    private final AuthentificationService authService = new AuthentificationService();

    @FXML
    public void initialize() {
        colIdSeance.setCellValueFactory(new PropertyValueFactory<>("idSeance"));
        colFilmSeance.setCellValueFactory(new PropertyValueFactory<>("titreFilm"));
        colSalleSeance.setCellValueFactory(new PropertyValueFactory<>("nomSalle"));
        colDateSeance.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDateSeance().toString()));
        colHeureSeance.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getHeure() + " - " + cell.getValue().getHeureFin()));
        colPlacesSeance.setCellValueFactory(new PropertyValueFactory<>("placesDispo"));

        colIdFilm.setCellValueFactory(new PropertyValueFactory<>("idFilm"));
        colTitreFilm.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colGenreFilm.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDureeFilm.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colNoteFilm.setCellValueFactory(new PropertyValueFactory<>("note"));

        tableFilms.getSelectionModel().selectedItemProperty().addListener((obs, oldV, f) -> {
            if (f != null) {
                txtFilmTitre.setText(f.getTitre());
                txtFilmGenre.setText(f.getGenre());
                txtFilmDuree.setText(String.valueOf(f.getDuree()));
                txtFilmNote.setText(String.valueOf(f.getNote()));
                txtFilmSynopsis.setText(f.getSynopsis());
            }
        });

        colIdSalle.setCellValueFactory(new PropertyValueFactory<>("idSalle"));
        colNomSalle.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCapaciteSalle.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colEquipementSalle.setCellValueFactory(new PropertyValueFactory<>("equipement"));

        tableSalles.getSelectionModel().selectedItemProperty().addListener((obs, oldV, s) -> {
            if (s != null) {
                txtSalleNom.setText(s.getNom());
                txtSalleCapacite.setText(String.valueOf(s.getCapacite()));
                txtSalleEquipement.setText(s.getEquipement());
            }
        });

        colIdTarif.setCellValueFactory(new PropertyValueFactory<>("idTarif"));
        colCatTarif.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colLibelleTarif.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colPrixTarif.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.2f Rs", cell.getValue().getPrixUnitaire())));
        colDescTarif.setCellValueFactory(new PropertyValueFactory<>("description"));

        tableTarifs.getSelectionModel().selectedItemProperty().addListener((obs, oldV, t) -> {
            if (t != null) {
                txtTarifCategorie.setText(t.getCategorie());
                txtTarifLibelle.setText(t.getLibelle());
                txtTarifPrix.setText(String.valueOf(t.getPrixUnitaire()));
                txtTarifDescription.setText(t.getDescription());
                chkTarifActif.setSelected(t.isActif());
            }
        });

        dpSeanceDate.setValue(LocalDate.now());
        txtSeanceHeure.setText("18:00");

        appliquerDroitsSelonRole();
        rafraichirTout();
    }

    private void appliquerDroitsSelonRole() {
        Utilisateur u = authService.getUtilisateurConnecte();
        boolean peutModifier = u != null && u.estManagerOuPlus();

        btnProgrammerSeance.setDisable(!peutModifier);
        btnSupprimerSeance.setDisable(!peutModifier);
        btnAjouterFilm.setDisable(!peutModifier);
        btnModifierFilm.setDisable(!peutModifier);
        btnSupprimerFilm.setDisable(!peutModifier);
        btnAjouterSalle.setDisable(!peutModifier);
        btnModifierSalle.setDisable(!peutModifier);
        btnSupprimerSalle.setDisable(!peutModifier);
        btnModifierTarif.setDisable(!peutModifier);
    }

    @FXML
    public void rafraichirTout() {
        try {
            ObservableList<Film> films = FXCollections.observableArrayList(programmationService.listerFilms());
            ObservableList<Salle> salles = FXCollections.observableArrayList(programmationService.listerSalles());
            ObservableList<Seance> seances = FXCollections.observableArrayList(programmationService.listerSeances());
            ObservableList<Tarif> tarifs = FXCollections.observableArrayList(billetterieService.listerTarifs());

            tableFilms.setItems(films);
            tableSalles.setItems(salles);
            tableSeances.setItems(seances);
            tableTarifs.setItems(tarifs);

            cbSeanceFilm.setItems(films);
            cbSeanceSalle.setItems(salles);
            cbFiltreFilm.setItems(films);
            cbFiltreSalle.setItems(salles);
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onProgrammerSeance() {
        Film film = cbSeanceFilm.getValue();
        Salle salle = cbSeanceSalle.getValue();
        LocalDate date = dpSeanceDate.getValue();
        String heureTexte = txtSeanceHeure.getText();

        if (film == null || salle == null || date == null) {
            afficherMessage("Veuillez choisir un film, une salle et une date.", true);
            return;
        }

        LocalTime heure;
        try {
            heure = LocalTime.parse(heureTexte.trim());
        } catch (DateTimeParseException e) {
            afficherMessage("Format d'heure invalide (HH:mm).", true);
            return;
        }

        try {
            programmationService.programmerSeance(film.getIdFilm(), salle.getIdSalle(), date, heure);
            afficherMessage("Séance programmée.", false);
            rafraichirTout();
        } catch (CineTrackException e) {
            afficherMessage(e.getMessage(), true);
        } catch (SQLException e) {
            afficherMessage("Erreur SQL : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onFiltrerSeances() {
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
    private void onReinitialiserFiltresSeances() {
        dpFiltreDate.setValue(null);
        cbFiltreSalle.setValue(null);
        cbFiltreFilm.setValue(null);
        rafraichirTout();
    }

    @FXML
    private void onSupprimerSeance() {
        Seance sel = tableSeances.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez une séance à supprimer.", true);
            return;
        }
        try {
            programmationService.supprimerSeance(sel.getIdSeance(), authService.getUtilisateurConnecte());
            afficherMessage("Séance #" + sel.getIdSeance() + " supprimée.", false);
            rafraichirTout();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onAjouterFilm() {
        try {
            int duree = Integer.parseInt(txtFilmDuree.getText().trim());
            double note = txtFilmNote.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtFilmNote.getText().trim());
            programmationService.ajouterFilm(
                    txtFilmTitre.getText(),
                    txtFilmGenre.getText(),
                    duree,
                    note,
                    txtFilmSynopsis.getText()
            );
            afficherMessage("Film ajouté.", false);
            rafraichirTout();
        } catch (NumberFormatException e) {
            afficherMessage("Durée ou note invalide.", true);
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onModifierFilm() {
        Film sel = tableFilms.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un film à modifier.", true);
            return;
        }
        try {
            int duree = Integer.parseInt(txtFilmDuree.getText().trim());
            double note = Double.parseDouble(txtFilmNote.getText().trim());
            programmationService.modifierFilm(
                    sel.getIdFilm(),
                    txtFilmTitre.getText(),
                    txtFilmGenre.getText(),
                    duree,
                    note,
                    txtFilmSynopsis.getText()
            );
            afficherMessage("Film mis à jour.", false);
            rafraichirTout();
        } catch (NumberFormatException e) {
            afficherMessage("Durée ou note invalide.", true);
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onSupprimerFilm() {
        Film sel = tableFilms.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un film à supprimer.", true);
            return;
        }
        try {
            programmationService.supprimerFilm(sel.getIdFilm(), authService.getUtilisateurConnecte());
            afficherMessage("Film supprimé.", false);
            rafraichirTout();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onRechercherFilms() {
        try {
            tableFilms.setItems(FXCollections.observableArrayList(
                    programmationService.rechercherFilms(txtRechercheFilmTitre.getText(), txtRechercheFilmGenre.getText())
            ));
        } catch (SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onAjouterSalle() {
        try {
            int cap = Integer.parseInt(txtSalleCapacite.getText().trim());
            programmationService.ajouterSalle(txtSalleNom.getText(), cap, txtSalleEquipement.getText());
            afficherMessage("Salle ajoutée.", false);
            rafraichirTout();
        } catch (NumberFormatException e) {
            afficherMessage("Capacité invalide.", true);
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onModifierSalle() {
        Salle sel = tableSalles.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez une salle à modifier.", true);
            return;
        }
        try {
            int cap = Integer.parseInt(txtSalleCapacite.getText().trim());
            programmationService.modifierSalle(sel.getIdSalle(), txtSalleNom.getText(), cap, txtSalleEquipement.getText());
            afficherMessage("Salle mise à jour.", false);
            rafraichirTout();
        } catch (NumberFormatException e) {
            afficherMessage("Capacité invalide.", true);
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onSupprimerSalle() {
        Salle sel = tableSalles.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez une salle à supprimer.", true);
            return;
        }
        try {
            programmationService.supprimerSalle(sel.getIdSalle(), authService.getUtilisateurConnecte());
            afficherMessage("Salle supprimée.", false);
            rafraichirTout();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onModifierTarif() {
        Tarif sel = tableTarifs.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un tarif dans le tableau.", true);
            return;
        }
        try {
            double prix = Double.parseDouble(txtTarifPrix.getText().trim());
            billetterieService.modifierTarif(
                    sel.getIdTarif(),
                    txtTarifLibelle.getText(),
                    prix,
                    txtTarifDescription.getText(),
                    chkTarifActif.isSelected(),
                    authService.getUtilisateurConnecte()
            );
            afficherMessage("Tarif mis à jour.", false);
            rafraichirTout();
        } catch (NumberFormatException e) {
            afficherMessage("Prix invalide.", true);
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean erreur) {
        lblMessageProg.setText(texte);
        lblMessageProg.getStyleClass().removeAll("msg-error", "msg-success");
        lblMessageProg.getStyleClass().add(erreur ? "msg-error" : "msg-success");
    }
}
