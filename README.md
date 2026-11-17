# CineTrack V2 — Cinéma Le Lumière (BTS SIO SLAM)

Application desktop JavaFX développée selon l'architecture MVC avec injection de dépendances par constructeur et connexion PostgreSQL (`cinetrack_groupe3`).

## 1. Architecture MVC (Étape 2)

```
View (src/main/resources/view/*.fxml + style.css)
   ↓
Controller (controller.*Controleur)
   ↓
Service (service.*Service — injection DAO par constructeur)
   ↓
DAO Interface (dao.*Dao)
   ↓
DAO Impl JDBC (impl.*DaoImpl)
   ↓
DatabaseConnection -> PostgreSQL (cinetrack_groupe3)
```

## 2. Structure du projet

- `src/main/java/main/Main.java` : point d'entrée JavaFX
- `src/main/java/database/DatabaseConnection.java` : connexion PostgreSQL (`cinetrack_groupe3`)
- `src/main/java/model/` : entités POJO (`Film`, `Salle`, `Seance`, `Client`, `Tarif`, `Reservation`, `Utilisateur`, `LogSecurite`)
- `src/main/java/dao/` : interfaces `FilmDao`, `SalleDao`, `SeanceDao`, `ClientDao`, `TarifDao`
- `src/main/java/impl/` : implémentations JDBC (`FilmDaoImpl`, `SalleDaoImpl`, `SeanceDaoImpl`, `ClientDaoImpl`, `TarifDaoImpl`)
- `src/main/java/service/` : `FilmService`, `SalleService`, `SeanceService`, `ClientService`, `TarifService`
- `src/main/java/controller/` : `MainControleur`, `FilmControleur`, `SalleControleur`, `SeanceControleur`, `ClientControleur`, `TarifControleur`
- `src/main/resources/view/` : `main-view.fxml`, `films-view.fxml`, `salles-view.fxml`, `seances-view.fxml`, `clients-view.fxml`, `tarifs-view.fxml`
- `src/main/resources/css/style.css` : feuille de style CSS
- `src/test/java/service/CineTrackArchitectureTest.java` : tests JUnit 5

## 3. Lancement

```bash
mvn clean javafx:run
```

Tests :

```bash
mvn test
```
