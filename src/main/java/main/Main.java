package main;

import database.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * ====================================================================
 * CLASSE PRINCIPALE : Point d'entrée de l'application JavaFX CineTrack V2
 * ====================================================================
 * Architecture :
 * View (FXML + CSS) -> Controller -> Service -> DAO Interface -> DAO Impl JDBC -> PostgreSQL
 */
public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        afficherEcranConnexion();
    }

    public static void afficherEcranConnexion() {
        try {
            Parent root = AppContext.getInstance().chargerVue("/view/login-view.fxml");
            Scene scene = new Scene(root, 960, 620);
            appliquerFeuilleDeStyle(scene);

            primaryStage.setTitle("CineTrack V2 — Cinéma Le Lumière (Authentification)");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void afficherTableauDeBord() {
        try {
            Parent root = AppContext.getInstance().chargerVue("/view/dashboard-view.fxml");
            Scene scene = new Scene(root, 1280, 780);
            appliquerFeuilleDeStyle(scene);

            String role = AppContext.getInstance().getAuthentificationService().getUtilisateurConnecte() != null
                    ? AppContext.getInstance().getAuthentificationService().getUtilisateurConnecte().getRole()
                    : "";
            primaryStage.setTitle("CineTrack V2 — Cinéma Le Lumière | Backoffice [" + role + "]");
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
        System.out.println("====================================================");
        System.out.println(" Démarrage de CineTrack V2 - Cinéma Le Lumière");
        System.out.println(" Connexion PostgreSQL : " + (DatabaseConnection.testConnection() ? "OK" : "ÉCHEC"));
        System.out.println("====================================================");
        launch(args);
    }
}
