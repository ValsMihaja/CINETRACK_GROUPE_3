# 🎬 CineTrack V2 — Cinéma Le Lumière (RP2 BTS SIO SLAM)

Application Backoffice Desktop de gestion cinématographique développée en **Java 21 / JavaFX (FXML + CSS)** selon l'architecture **MVC en couches** avec **Injection de Dépendances** et persistance **PostgreSQL (JDBC)**.

---

## 1. Architecture en Couches (Étape 2)

```
View (FXML + CSS)
   ↓ (Événements UI / @FXML)
Controller (controller.*Controleur)
   ↓ (Injection de dépendances via AppContext)
Service (service.*Service — Règles métier, zéro import JavaFX, zéro SQL)
   ↓ (Injection par constructeur des interfaces DAO)
DAO Interface (dao.IDAO<T>, dao.I*DAO)
   ↓ (Implémentation concrète JDBC)
DAO Impl JDBC/PostgreSQL (dao.impl.*DAOImpl — PreparedStatement / CallableStatement)
   ↓ (DatabaseConnection.getConnection())
PostgreSQL (Base projetcinetrackv2 — 8 tables, Triggers & Procédures Stockées)
```

---

## 2. Structure du Projet (`CINETRACK_GROUPE_3`)

- `pom.xml` : Configuration Maven (`javafx-controls`, `javafx-fxml`, `postgresql`, `junit-jupiter`).
- `src/main/java/main/` :
  - `Main.java` : Point d'entrée JavaFX.
  - `AppContext.java` : Conteneur d'Injection de Dépendances (assemble `DAO Impl` → `DAO Interface` → `Service` → `Controller`).
  - `TestArchitectureMVC.java` : Vérification complète de la chaîne MVC + PostgreSQL.
- `src/main/java/database/DatabaseConnection.java` : Connexion centralisée JDBC PostgreSQL.
- `src/main/java/model/` : Les **8 entités métier** correspondant aux 8 tables PostgreSQL (`Utilisateur`, `Film`, `Salle`, `Seance`, `Client`, `Tarif`, `Reservation`, `LogSecurite`).
- `src/main/java/dao/` : **Interfaces DAO** (`IDAO<T>`, `IUtilisateurDAO`, `IFilmDAO`, `ISalleDAO`, `ISeanceDAO`, `IClientDAO`, `ITarifDAO`, `IReservationDAO`, `ILogSecuriteDAO`).
- `src/main/java/dao/impl/` : **Implémentations JDBC/PostgreSQL** (`UtilisateurDAOImpl`, `FilmDAOImpl`, `SalleDAOImpl`, `SeanceDAOImpl`, `ClientDAOImpl`, `TarifDAOImpl`, `ReservationDAOImpl`, `LogSecuriteDAOImpl`).
- `src/main/java/service/` : **Couche Service Métier** (`AuthentificationService`, `ProgrammationService`, `BilletterieService`, `StatistiqueEtSauvegardeService`, `IExportService`).
- `src/main/java/controller/` : **Contrôleurs JavaFX** (`AuthentificationControleur`, `DashboardControleur`, `BilletterieControleur`, `ProgrammationControleur`, `StatistiqueControleur`).
- `src/main/java/exceptions/` : Hiérarchie d'exceptions métier (`CineTrackException`, `ConflitHoraireException`, `PlaceIndisponibleException`, `AuthentificationException`, `CompteBloqueException`).
- `src/main/resources/view/` : Vues **FXML** (`login-view.fxml`, `dashboard-view.fxml`, `billetterie-view.fxml`, `programmation-view.fxml`, `statistiques-view.fxml`).
- `src/main/resources/css/style.css` : Feuille de style **CSS** harmonisant l'interface.
- `src/test/java/service/CineTrackArchitectureTest.java` : Tests unitaires **JUnit 5**.

---

## 3. Comptes de Test (3 Rôles Internes)

| Rôle | Login | Mot de passe | Droits d'accès |
| :--- | :--- | :--- | :--- |
| **Caissier** | `caissier1` | `caissier123` | Billetterie, Réservations, Billets uniques, Annulations, Fiches Clients, Consultation du planning |
| **Manager** *(hérite de Caissier)* | `manager1` | `manager123` | + Catalogue des Films, Salles, Programmation des Séances (détection conflits), Grilles Tarifaires, Statistiques & Export CSV |
| **Administrateur** *(hérite de Manager)* | `admin` | `admin123` | + Gestion des Utilisateurs, Déblocage de comptes (3 tentatives), Journal d'audit `logs_securite` (CC4) & Sauvegarde BDD |

---

## 4. Lancement de l'Application

```bash
mvn clean javafx:run
```

Pour exécuter les tests unitaires JUnit 5 :
```bash
mvn test
```
