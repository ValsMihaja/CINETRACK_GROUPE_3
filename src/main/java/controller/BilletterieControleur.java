package controller;

import exceptions.CineTrackException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Client;
import model.Reservation;
import model.Seance;
import model.Tarif;
import service.AuthentificationService;
import service.BilletterieService;
import service.ProgrammationService;

import java.sql.SQLException;

public class BilletterieControleur {

    @FXML
    private ComboBox<Seance> cbSeance;

    @FXML
    private ComboBox<Client> cbClient;

    @FXML
    private ComboBox<Tarif> cbTarif;

    @FXML
    private Spinner<Integer> spNbPlaces;

    @FXML
    private Label lblMontantCalcule;

    @FXML
    private Label lblPlacesRestantes;

    @FXML
    private TextArea txtBilletGenere;

    @FXML
    private Label lblMessageBilletterie;

    @FXML
    private TableView<Reservation> tableReservations;

    @FXML
    private TableColumn<Reservation, String> colNumBillet;

    @FXML
    private TableColumn<Reservation, String> colSeanceRes;

    @FXML
    private TableColumn<Reservation, String> colClientRes;

    @FXML
    private TableColumn<Reservation, String> colTarifRes;

    @FXML
    private TableColumn<Reservation, Integer> colNbPlacesRes;

    @FXML
    private TableColumn<Reservation, String> colMontantRes;

    @FXML
    private TableColumn<Reservation, String> colStatutRes;

    @FXML
    private TextField txtClientNom;

    @FXML
    private TextField txtClientPrenom;

    @FXML
    private TextField txtClientEmail;

    @FXML
    private TextField txtClientTelephone;

    @FXML
    private TextField txtRechercheClient;

    @FXML
    private TableView<Client> tableClients;

    @FXML
    private TableColumn<Client, Integer> colIdClient;

    @FXML
    private TableColumn<Client, String> colNomClient;

    @FXML
    private TableColumn<Client, String> colPrenomClient;

    @FXML
    private TableColumn<Client, String> colEmailClient;

    @FXML
    private TableColumn<Client, String> colTelClient;

    private final BilletterieService billetterieService = new BilletterieService();
    private final ProgrammationService programmationService = new ProgrammationService();
    private final AuthentificationService authService = new AuthentificationService();

    @FXML
    public void initialize() {
        spNbPlaces.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));

        colNumBillet.setCellValueFactory(new PropertyValueFactory<>("numeroBillet"));
        colSeanceRes.setCellValueFactory(new PropertyValueFactory<>("infoSeance"));
        colClientRes.setCellValueFactory(new PropertyValueFactory<>("nomCompletClient"));
        colTarifRes.setCellValueFactory(new PropertyValueFactory<>("libelleTarif"));
        colNbPlacesRes.setCellValueFactory(new PropertyValueFactory<>("nbPlaces"));
        colMontantRes.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.2f Rs", cell.getValue().getMontantTotal())));
        colStatutRes.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colIdClient.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        colNomClient.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenomClient.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmailClient.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelClient.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        cbSeance.valueProperty().addListener((obs, oldV, newV) -> mettreAJourDevis());
        cbTarif.valueProperty().addListener((obs, oldV, newV) -> mettreAJourDevis());
        spNbPlaces.valueProperty().addListener((obs, oldV, newV) -> mettreAJourDevis());

        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldV, res) -> {
            if (res != null) {
                txtBilletGenere.setText(res.genererRecapitulatifBillet());
            }
        });

        rafraichirDonnees();
    }

    @FXML
    public void rafraichirDonnees() {
        try {
            cbSeance.setItems(FXCollections.observableArrayList(programmationService.listerSeances()));
            cbClient.setItems(FXCollections.observableArrayList(billetterieService.listerClients()));
            cbTarif.setItems(FXCollections.observableArrayList(billetterieService.listerTarifsActifs()));

            if (!cbSeance.getItems().isEmpty() && cbSeance.getValue() == null) {
                cbSeance.getSelectionModel().selectFirst();
            }
            if (!cbClient.getItems().isEmpty() && cbClient.getValue() == null) {
                cbClient.getSelectionModel().selectFirst();
            }
            if (!cbTarif.getItems().isEmpty() && cbTarif.getValue() == null) {
                cbTarif.getSelectionModel().selectFirst();
            }

            tableReservations.setItems(FXCollections.observableArrayList(billetterieService.listerReservations()));
            tableClients.setItems(FXCollections.observableArrayList(billetterieService.listerClients()));
            mettreAJourDevis();
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void mettreAJourDevis() {
        Seance s = cbSeance.getValue();
        if (s != null) {
            lblPlacesRestantes.setText(s.getPlacesDispo() + " / " + s.getCapaciteSalle() + " places");
        } else {
            lblPlacesRestantes.setText("--");
        }

        Tarif t = cbTarif.getValue();
        Integer nb = spNbPlaces.getValue();
        if (t != null && nb != null && nb > 0) {
            try {
                double total = billetterieService.calculerMontantTotal(t, nb);
                lblMontantCalcule.setText(String.format("%.2f Rs", total));
            } catch (CineTrackException e) {
                lblMontantCalcule.setText("0.00 Rs");
            }
        } else {
            lblMontantCalcule.setText("0.00 Rs");
        }
    }

    @FXML
    private void onReserverPlaces() {
        Seance seance = cbSeance.getValue();
        Client client = cbClient.getValue();
        Tarif tarif = cbTarif.getValue();
        Integer nbPlaces = spNbPlaces.getValue();

        if (seance == null || client == null || tarif == null || nbPlaces == null) {
            afficherMessage("Veuillez sélectionner une séance, un client, un tarif et un nombre de places.", true);
            return;
        }

        try {
            Reservation billet = billetterieService.reserverPlaces(
                    seance.getIdSeance(),
                    client.getIdClient(),
                    tarif.getIdTarif(),
                    nbPlaces,
                    authService.getUtilisateurConnecte()
            );

            txtBilletGenere.setText(billet.genererRecapitulatifBillet());
            afficherMessage("Réservation confirmée : " + billet.getNumeroBillet(), false);
            rafraichirDonnees();
        } catch (CineTrackException e) {
            afficherMessage(e.getMessage(), true);
        } catch (SQLException e) {
            afficherMessage("Erreur SQL : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onAnnulerReservation() {
        Reservation selection = tableReservations.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Sélectionnez une réservation dans le tableau.", true);
            return;
        }

        try {
            billetterieService.annulerReservation(
                    selection.getIdReservation(),
                    authService.getUtilisateurConnecte()
            );
            afficherMessage("Billet " + selection.getNumeroBillet() + " annulé.", false);
            rafraichirDonnees();
        } catch (CineTrackException e) {
            afficherMessage(e.getMessage(), true);
        } catch (SQLException e) {
            afficherMessage("Erreur SQL : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onAjouterClient() {
        try {
            billetterieService.ajouterClient(
                    txtClientNom.getText(),
                    txtClientPrenom.getText(),
                    txtClientEmail.getText(),
                    txtClientTelephone.getText()
            );
            txtClientNom.clear();
            txtClientPrenom.clear();
            txtClientEmail.clear();
            txtClientTelephone.clear();
            afficherMessage("Client enregistré.", false);
            rafraichirDonnees();
        } catch (CineTrackException e) {
            afficherMessage(e.getMessage(), true);
        } catch (SQLException e) {
            afficherMessage("Erreur SQL : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onRechercherClient() {
        try {
            String motCle = txtRechercheClient.getText();
            tableClients.setItems(FXCollections.observableArrayList(billetterieService.rechercherClients(motCle)));
        } catch (SQLException e) {
            afficherMessage("Erreur recherche : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean erreur) {
        lblMessageBilletterie.setText(texte);
        lblMessageBilletterie.getStyleClass().removeAll("msg-error", "msg-success");
        lblMessageBilletterie.getStyleClass().add(erreur ? "msg-error" : "msg-success");
    }
}
