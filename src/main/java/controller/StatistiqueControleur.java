package controller;

import exceptions.CineTrackException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.LogSecurite;
import model.Utilisateur;
import service.AuthentificationService;
import service.StatistiqueEtSauvegardeService;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StatistiqueControleur {

    @FXML
    private Label lblKpiChiffreAffaires;

    @FXML
    private Label lblKpiBilletsVendus;

    @FXML
    private Label lblKpiReservations;

    @FXML
    private TableView<Map<String, Object>> tableTopFilms;

    @FXML
    private TableColumn<Map<String, Object>, String> colTopTitre;

    @FXML
    private TableColumn<Map<String, Object>, String> colTopGenre;

    @FXML
    private TableColumn<Map<String, Object>, String> colTopNote;

    @FXML
    private TableColumn<Map<String, Object>, String> colTopPlaces;

    @FXML
    private TableColumn<Map<String, Object>, String> colTopCA;

    @FXML
    private TableView<Map<String, Object>> tableOccupation;

    @FXML
    private TableColumn<Map<String, Object>, String> colOccDate;

    @FXML
    private TableColumn<Map<String, Object>, String> colOccHeure;

    @FXML
    private TableColumn<Map<String, Object>, String> colOccSalle;

    @FXML
    private TableColumn<Map<String, Object>, String> colOccFilm;

    @FXML
    private TableColumn<Map<String, Object>, String> colOccPlaces;

    @FXML
    private TableColumn<Map<String, Object>, String> colOccTaux;

    @FXML
    private Tab tabAdministration;

    @FXML
    private TextField txtUserLogin;

    @FXML
    private PasswordField txtUserMdp;

    @FXML
    private TextField txtUserNom;

    @FXML
    private TextField txtUserPrenom;

    @FXML
    private ComboBox<String> cbUserRole;

    @FXML
    private TableView<Utilisateur> tableUtilisateurs;

    @FXML
    private TableColumn<Utilisateur, Integer> colUserId;

    @FXML
    private TableColumn<Utilisateur, String> colUserLogin;

    @FXML
    private TableColumn<Utilisateur, String> colUserNomComplet;

    @FXML
    private TableColumn<Utilisateur, String> colUserRole;

    @FXML
    private TableColumn<Utilisateur, String> colUserStatut;

    @FXML
    private TableView<LogSecurite> tableLogs;

    @FXML
    private TableColumn<LogSecurite, Integer> colLogId;

    @FXML
    private TableColumn<LogSecurite, String> colLogDate;

    @FXML
    private TableColumn<LogSecurite, String> colLogUser;

    @FXML
    private TableColumn<LogSecurite, String> colLogAction;

    @FXML
    private TableColumn<LogSecurite, String> colLogDetails;

    @FXML
    private Button btnSauvegardeBdd;

    @FXML
    private Label lblMessageStats;

    private final StatistiqueEtSauvegardeService statistiqueService = new StatistiqueEtSauvegardeService();
    private final AuthentificationService authService = new AuthentificationService();

    @FXML
    public void initialize() {
        colTopTitre.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("titre"))));
        colTopGenre.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("genre"))));
        colTopNote.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get("noteMoyenne") + " / 10"));
        colTopPlaces.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("placesVendues"))));
        colTopCA.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f Rs", (Double) c.getValue().get("chiffreAffaires"))));

        colOccDate.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("dateSeance"))));
        colOccHeure.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("heure"))));
        colOccSalle.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("salle"))));
        colOccFilm.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("film"))));
        colOccPlaces.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().get("placesOccupees") + " / " + c.getValue().get("capacite")));
        colOccTaux.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get("tauxOccupation") + " %"));

        cbUserRole.setItems(FXCollections.observableArrayList("CAISSIER", "MANAGER", "ADMIN"));
        cbUserRole.getSelectionModel().selectFirst();

        colUserId.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colUserLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colUserNomComplet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomComplet()));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatutTexte()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        colLogId.setCellValueFactory(new PropertyValueFactory<>("idLog"));
        colLogDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateHeure() != null ? c.getValue().getDateHeure().format(fmt) : ""));
        colLogUser.setCellValueFactory(new PropertyValueFactory<>("utilisateurLogin"));
        colLogAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colLogDetails.setCellValueFactory(new PropertyValueFactory<>("donneesConcernees"));

        Utilisateur u = authService.getUtilisateurConnecte();
        if (tabAdministration != null) {
            tabAdministration.setDisable(u == null || !u.estAdmin());
        }

        rafraichirStatistiques();
    }

    @FXML
    public void rafraichirStatistiques() {
        try {
            lblKpiChiffreAffaires.setText(String.format("%.2f Rs", statistiqueService.obtenirChiffreAffairesTotal()));
            lblKpiBilletsVendus.setText(String.valueOf(statistiqueService.obtenirTotalBilletsVendus()));
            lblKpiReservations.setText(String.valueOf(statistiqueService.obtenirTotalReservationsConfirmees()));

            tableTopFilms.setItems(FXCollections.observableArrayList(statistiqueService.obtenirTopFilms()));
            tableOccupation.setItems(FXCollections.observableArrayList(statistiqueService.obtenirTauxOccupationSeances()));

            tableUtilisateurs.setItems(FXCollections.observableArrayList(authService.listerUtilisateurs()));
            tableLogs.setItems(FXCollections.observableArrayList(statistiqueService.consulterLogsSecurite()));
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onExporterRapportCSV() {
        try {
            File fichier = new File("exports/rapport_exploitation_cinetrack.csv");
            statistiqueService.exporterCSV(fichier.getAbsolutePath());
            afficherMessage("Rapport exporté : " + fichier.getName(), false);
            rafraichirStatistiques();
        } catch (Exception e) {
            afficherMessage("Erreur export CSV : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onCreerUtilisateur() {
        try {
            authService.ajouterUtilisateur(
                    txtUserLogin.getText(),
                    txtUserMdp.getText(),
                    txtUserNom.getText(),
                    txtUserPrenom.getText(),
                    cbUserRole.getValue()
            );
            txtUserLogin.clear();
            txtUserMdp.clear();
            txtUserNom.clear();
            txtUserPrenom.clear();
            afficherMessage("Utilisateur créé.", false);
            rafraichirStatistiques();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onDebloquerUtilisateur() {
        Utilisateur sel = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un utilisateur.", true);
            return;
        }
        try {
            authService.debloquerUtilisateur(sel.getIdUtilisateur());
            afficherMessage("Compte '" + sel.getLogin() + "' débloqué.", false);
            rafraichirStatistiques();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onSupprimerUtilisateur() {
        Utilisateur sel = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un utilisateur.", true);
            return;
        }
        try {
            authService.supprimerUtilisateur(sel.getIdUtilisateur());
            afficherMessage("Utilisateur supprimé.", false);
            rafraichirStatistiques();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onExecuterSauvegarde() {
        try {
            File dossier = new File("backups");
            boolean ok = statistiqueService.executerSauvegardeBDD(dossier.getAbsolutePath());
            if (ok) {
                afficherMessage("Sauvegarde générée dans : " + dossier.getName(), false);
                rafraichirStatistiques();
            }
        } catch (Exception e) {
            afficherMessage("Erreur sauvegarde : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean erreur) {
        lblMessageStats.setText(texte);
        lblMessageStats.getStyleClass().removeAll("msg-error", "msg-success");
        lblMessageStats.getStyleClass().add(erreur ? "msg-error" : "msg-success");
    }
}
