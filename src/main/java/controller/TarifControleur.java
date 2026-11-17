package controller;

import exceptions.CineTrackException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import model.Tarif;
import model.Utilisateur;
import service.AuthentificationService;
import service.BilletterieService;

import java.sql.SQLException;

public class TarifControleur {

    @FXML
    private TextField txtCategorie;

    @FXML
    private TextField txtLibelle;

    @FXML
    private TextField txtPrix;

    @FXML
    private TextField txtDescription;

    @FXML
    private CheckBox chkActif;

    @FXML
    private Button btnModifier;

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

    private final BilletterieService billetterieService = new BilletterieService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idTarif"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixTexte"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        tableTarifs.getSelectionModel().selectedItemProperty().addListener((obs, ancien, t) -> {
            if (t != null) {
                txtCategorie.setText(t.getCategorie());
                txtLibelle.setText(t.getLibelle());
                txtPrix.setText(String.valueOf(t.getPrixUnitaire()));
                txtDescription.setText(t.getDescription());
                chkActif.setSelected(t.isActif());
            }
        });

        Utilisateur u = AuthentificationService.getSession();
        btnModifier.setDisable(u == null || !u.estManagerOuPlus());

        rafraichirTable();
    }

    @FXML
    private void onModifier() {
        Tarif sel = tableTarifs.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherMessage("Sélectionnez un tarif dans le tableau.", true);
            return;
        }

        double prix;
        try {
            prix = Double.parseDouble(txtPrix.getText().trim());
        } catch (NumberFormatException e) {
            afficherMessage("Prix invalide.", true);
            return;
        }

        try {
            billetterieService.modifierTarif(
                    sel.getIdTarif(),
                    txtLibelle.getText(),
                    prix,
                    txtDescription.getText(),
                    chkActif.isSelected(),
                    AuthentificationService.getSession()
            );
            afficherMessage("Tarif mis à jour.", false);
            rafraichirTable();
        } catch (CineTrackException | SQLException e) {
            afficherMessage(e.getMessage(), true);
        }
    }

    private void rafraichirTable() {
        try {
            tableTarifs.setItems(FXCollections.observableArrayList(billetterieService.listerTarifs()));
        } catch (SQLException e) {
            afficherMessage("Erreur chargement : " + e.getMessage(), true);
        }
    }

    private void afficherMessage(String texte, boolean estErreur) {
        lblMessage.setText(texte);
        lblMessage.setTextFill(estErreur ? Color.RED : Color.DARKGREEN);
    }
}
