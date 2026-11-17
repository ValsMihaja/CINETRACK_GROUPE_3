package main;

import database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Utilisateur;
import service.AuthentificationService;

import java.net.URL;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        afficherEcranConnexion();
    }

    public static void afficherEcranConnexion() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/view/login-view.fxml"));
            Scene scene = new Scene(root, 900, 560);
            appliquerFeuilleDeStyle(scene);

            primaryStage.setTitle("CineTrack - Connexion");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void afficherTableauDeBord() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/view/dashboard-view.fxml"));
            Scene scene = new Scene(root, 1220, 760);
            appliquerFeuilleDeStyle(scene);

            Utilisateur u = new AuthentificationService().getUtilisateurConnecte();
            String role = u != null ? u.getRole() : "";
            primaryStage.setTitle("CineTrack - Gestion Cinéma (" + role + ")");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void appliquerFeuilleDeStyle(Scene scene) {
        URL cssUrl = Main.class.getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    public static void main(String[] args) {
        System.out.println("Connexion PostgreSQL : " + (DatabaseConnection.testConnection() ? "OK" : "ECHEC"));
        launch(args);
    }
}
