package controller;

import exceptions.CineTrackException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.Client;
import model.Reservation;
import model.Seance;
import model.Tarif;
import service.AuthentificationService;
import service.BilletterieService;

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
    private Label lblPlacesRestantes;

    @FXML
    private Label lblMontantCalcule;

    @FXML
    private TextArea txtBilletGenere;

    @FXML
    private Label lblMessage;

    @FXML
    private TableView<Reservation> tableReservations;

    @FXML
    private TableColumn<Reservation, String> colNumBillet;

    @FXML
    private TableColumn<Reservation, String> colSeance;

    @FXML
    private TableColumn<Reservation, String> colClient;

    @FXML
    private TableColumn<Reservation, String> colTarif;

    @FXML
    private TableColumn<Reservation, Integer> colNbPlaces;

    @FXML
    private TableColumn<Reservation, String> colMontant;

    @FXML
    private TableColumn<Reservation, String> colStatut;

    private final BilletterieService billetterieService = new BilletterieService();

    @FXML
    public void initialize() {
        spNbPlaces.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));

        colNumBillet.setCellValueFactory(new PropertyValueFactory<>("numeroBillet"));
        colSeance.setCellValueFactory(new PropertyValueFactory<>("infoSeance"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("nomCompletClient"));
        colTarif.setCellValueFactory(new PropertyValueFactory<>("libelleTarif"));
        colNbPlaces.setCellValueFactory(new PropertyValueFactory<>("nbPlaces"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montantTexte"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        cbSeance.setOnAction(e -> mettreAJourCalcul());
        cbTarif.setOnAction(e -> mettreAJourCalcul());
        spNbPlaces.valueProperty().addListener((obs, ancien, nouveau) -> mettreAJourCalcul());

        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, ancien, res) -> {
            if (res != null) {
                txtBilletGenere.setText(res.genererRecapitulatifBillet());
            }
        });

        rafraichirTable();
    }

    @FXML
    private void onReserver() {
        Seance seance = cbSeance.getValue();
        Client client = cbClient.getValue();
        Tarif tarif = cbTarif.getValue();
        Integer nbPlaces = spNbPlaces.getValue();

        if (seance == null || client == null || tarif == null || nbPlaces == null) {
            afficherMessage("Veuillez remplir tous les champs.", true);
            return;
        }

        try {
            Reservation billet = billetterieService.reserverPlaces(
                    seance.getIdSeance(),
                    client.getIdClient(),
                    tarif.getIdTarif(),
                    nbPlaces,
                    AuthentificationService.getSession()
            );
            txtBilletGenere.setText(billet.genererRecapitulatifBillet());
            afficherMessage("Réservation confirmée : " + billet.getNumeroBillet(), false);
            rafraichirTable();
        } catch (CineTrackException e) {
            afficherMessage(e.getMessage(), true);
        } catch (SQLException e) {
            afficherMessage("Erreur base de données : " + e.getMessage(), true);
        }
    }

    @FXML
    private void onAnnuler() {
        Reservation selection = tableReservations.getSelectionModel().getSelectedItem();
        if (selection == null) {
            afficherMessage("Sélectionnez une réservation dans le tableau.", true);
            return;
        }

        try {
            billetterieService.annulerReservation(
                    selection.getIdReservation(),
                    AuthentificationService.getSession()
            );
            afficherMessage("Réservation annulée.", false);
            rafraichirTable();
        } catch (CineTrackException e) {
            afficherMessage(e.getMessage(), true);
        } catch (SQLException e) {
            afficherMessage("Erreur base de données : " + e.getMessage(), true);
        }
    }

    @FXML
    private void rafraichirTable() {
        try {
            cbSeance.setItems(FXCollections.observableArrayList(billetterieService.listerSeances()));
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
            mettreAJourCalcul();
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void mettreAJourCalcul() {
        Seance s = cbSeance.getValue();
        if (s != null) {
            lblPlacesRestantes.setText(s.getPlacesDispo() + " / " + s.getCapaciteSalle());
        } else {
            lblPlacesRestantes.setText("--");
        }

        Tarif t = cbTarif.getValue();
        Integer nb = spNbPlaces.getValue();
        if (t != null && nb != null) {
            try {
                double total = billetterieService.calculerMontantTotal(t, nb);
                lblMontantCalcule.setText(String.format("%.2f Rs", total));
            } catch (CineTrackException e) {
                lblMontantCalcule.setText("0.00 Rs");
            }
        }
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
