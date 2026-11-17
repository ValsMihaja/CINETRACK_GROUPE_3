# CineTrack V2 — Cinéma Le Lumière (BTS SIO SLAM)

Application desktop JavaFX de gestion cinématographique développée selon l'architecture MVC avec PostgreSQL (JDBC).

## 1. Architecture MVC

```
View (src/main/resources/view/*.fxml + style.css)
   ↓
Controller (1 contrôleur simple par écran)
   ↓
Service (service.*Service)
   ↓
DAO Interface (dao.*Dao)
   ↓
DAO Impl JDBC (impl.*DaoImpl)
   ↓
DatabaseConnection -> PostgreSQL (cinetrack_groupe3 — 8 tables)
```

## 2. Structure du projet

- `src/main/java/main/` : `Main.java`, `TestArchitectureMVC.java`
- `src/main/java/database/` : `DatabaseConnection.java`
- `src/main/java/model/` : `Utilisateur`, `Film`, `Salle`, `Seance`, `Client`, `Tarif`, `Reservation`, `LogSecurite`, `StatFilm`, `StatOccupation`
- `src/main/java/dao/` : `ClientDao`, `FilmDao`, `SalleDao`, `SeanceDao`, `TarifDao`, `ReservationDao`, `UtilisateurDao`, `LogSecuriteDao`, `StatistiqueDao`
- `src/main/java/impl/` : implémentations JDBC (`*DaoImpl`)
- `src/main/java/service/` : `AuthentificationService`, `BilletterieService`, `ProgrammationService`, `StatistiqueEtSauvegardeService`, `ExportService`
- `src/main/java/controller/` : `AuthentificationControleur`, `DashboardControleur`, `BilletterieControleur`, `ClientControleur`, `SeanceControleur`, `FilmControleur`, `SalleControleur`, `TarifControleur`, `StatistiqueControleur`, `AdministrationControleur`
- `src/main/resources/view/` : `login-view.fxml`, `dashboard-view.fxml`, `billetterie-view.fxml`, `clients-view.fxml`, `seances-view.fxml`, `films-view.fxml`, `salles-view.fxml`, `tarifs-view.fxml`, `statistiques-view.fxml`, `administration-view.fxml`
- `src/main/resources/css/style.css` : feuille de style CSS
- `src/test/java/service/CineTrackArchitectureTest.java` : tests JUnit 5

## 3. Lancement

```bash
mvn clean javafx:run
```

Tests unitaires :

```bash
mvn test
```
