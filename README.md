# CineTrack V2 — Cinéma Le Lumière (BTS SIO SLAM)

Application desktop JavaFX de gestion cinématographique développée selon l'architecture MVC avec persistance PostgreSQL (JDBC).

## 1. Architecture MVC

```
View (src/main/resources/view/*.fxml + style.css)
   ↓
Controller (controller.*Controleur)
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
- `src/main/java/model/` : `Utilisateur`, `Film`, `Salle`, `Seance`, `Client`, `Tarif`, `Reservation`, `LogSecurite`
- `src/main/java/dao/` : interfaces `ClientDao`, `FilmDao`, `SalleDao`, `SeanceDao`, `TarifDao`, `ReservationDao`, `UtilisateurDao`, `LogSecuriteDao`
- `src/main/java/impl/` : implémentations JDBC (`ClientDaoImpl`, `FilmDaoImpl`, `SalleDaoImpl`, `SeanceDaoImpl`, `TarifDaoImpl`, `ReservationDaoImpl`, `UtilisateurDaoImpl`, `LogSecuriteDaoImpl`)
- `src/main/java/service/` : `AuthentificationService`, `BilletterieService`, `ProgrammationService`, `StatistiqueEtSauvegardeService`, `ExportService`
- `src/main/java/controller/` : `AuthentificationControleur`, `DashboardControleur`, `BilletterieControleur`, `ProgrammationControleur`, `StatistiqueControleur`
- `src/main/java/exceptions/` : `CineTrackException`, `AuthentificationException`, `CompteBloqueException`, `ConflitHoraireException`, `PlaceIndisponibleException`
- `src/main/resources/view/` : `login-view.fxml`, `dashboard-view.fxml`, `billetterie-view.fxml`, `programmation-view.fxml`, `statistiques-view.fxml`
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
