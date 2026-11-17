package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainControleur {

    @FXML
    private Button btnFilms;

    @FXML
    private Button btnSalles;

    @FXML
    private Button btnSeances;

    @FXML
    private Button btnClients;

    @FXML
    private Button btnTarifs;

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        onAfficherFilms();
    }

    @FXML
    private void onAfficherFilms() {
        chargerVue("/view/films-view.fxml", btnFilms);
    }

    @FXML
    private void onAfficherSalles() {
        chargerVue("/view/salles-view.fxml", btnSalles);
    }

    @FXML
    private void onAfficherSeances() {
        chargerVue("/view/seances-view.fxml", btnSeances);
    }

    @FXML
    private void onAfficherClients() {
        chargerVue("/view/clients-view.fxml", btnClients);
    }

    @FXML
    private void onAfficherTarifs() {
        chargerVue("/view/tarifs-view.fxml", btnTarifs);
    }

    private void chargerVue(String fxmlPath, Button boutonActif) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(vue);

            Button[] boutons = {btnFilms, btnSalles, btnSeances, btnClients, btnTarifs};
            for (Button btn : boutons) {
                btn.getStyleClass().remove("nav-btn-active");
            }
            if (boutonActif != null) {
                boutonActif.getStyleClass().add("nav-btn-active");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
