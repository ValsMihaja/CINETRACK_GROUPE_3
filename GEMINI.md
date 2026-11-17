# GEMINI.md — Règles et Mémoire Partagée du Projet CineTrack V2

> **Contexte :** Application Backoffice Desktop **CineTrack V2** pour le **Cinéma Le Lumière** (prestataire **DevCore**, BTS SIO SLAM — Groupe 3).  
> **Stack technique :** Java 21, JavaFX 21.0.2 (FXML + CSS), Maven (`mvn javafx:run` / `mvn test`), JDBC PostgreSQL (`42.7.3`), JUnit 5 (`5.10.2`).  
> **Base de données cible :** PostgreSQL `jdbc:postgresql://localhost:5432/cinetrack_groupe3` (8 tables métier, triggers et procédures stockées).

---

## 1. RÈGLES STRICTES POUR L'IA (OBLIGATOIRE)

- **Protection du code existant :** Ne **jamais** réécrire, supprimer ou refactoriser les fichiers et contrôleurs existants listés dans la section **« 3. CODE VERROUILLÉ (DÉJÀ IMPLÉMENTÉ) »**, sauf si l'utilisateur te donne une autorisation explicite sur un fichier précis.
- **Architecture MVC stricte :** Respecter impérativement l'architecture en couches du projet (JavaFX, Maven, PostgreSQL) et la séparation des packages :
  - `controller` : uniquement la logique UI JavaFX (`@FXML`), récupération/affichage des données ;
  - `service` : couche métier recevant l'interface DAO par injection de dépendances dans le constructeur ;
  - `dao` / `impl` : interfaces `*Dao` dans `dao/` et implémentations JDBC PostgreSQL `*DaoImpl` dans le package externe `impl/` par requêtes préparées (`PreparedStatement`) ;
  - `model` : entités métier POJO alignées sur les 8 tables PostgreSQL ;
  - `database` : gestionnaire de connexion JDBC centralisé (`DatabaseConnection`).
- **Base de données & Identifiants protégés :** Ne **jamais** modifier les identifiants de connexion dans `DatabaseConnection.java` ni les scripts SQL déjà validés (`sql/*.sql`) sans demande explicite de l'utilisateur.
- **Lancement & Tests :** Utiliser `mvn javafx:run` (ou `mvn clean javafx:run`) pour exécuter l'application JavaFX et `mvn test` pour exécuter les tests unitaires JUnit 5 (ne pas lancer `main.Main` directement sans `--module-path`).

---

## 2. AUTOMATISATION DE LA MÉMOIRE D'ÉQUIPE (À EXÉCUTER SANS DEMANDER)

- **Mise à jour systématique :** À chaque fois que tu termines d'implémenter une fonctionnalité, de créer un fichier ou de corriger un bug, tu **DOIS** automatiquement ajouter une ligne datée dans la section **« 4. JOURNAL DES MODIFICATIONS DE L'ÉQUIPE »** de ce fichier `GEMINI.md` en précisant :
  1. les fichiers créés ou modifiés ;
  2. ce qui a été fait ;
  3. les éventuels points d'attention pour les autres développeurs.
- **Rappel GitHub en fin de réponse :** Rappelle brièvement à l'utilisateur à la fin de ta réponse que le fichier `GEMINI.md` a été mis à jour et qu'il doit le **commit/push** avec son code.

---

## 3. CODE VERROUILLÉ (DÉJÀ IMPLÉMENTÉ)

### 3.1 Configuration, Documentation & Point d'entrée (`racine`, `main/`, `database/`)
| Élément | Rôle actuel |
| :--- | :--- |
| `pom.xml` | Configuration Maven Java 21 : dépendances `javafx-controls` et `javafx-fxml` (`21.0.2`), driver `postgresql` (`42.7.3`), `junit-jupiter` (`5.10.2`), plugins `maven-compiler-plugin` (`3.13.0`) et `javafx-maven-plugin` (`0.0.8`, `mainClass=main.Main`). |
| `README.md` | Documentation d'architecture MVC Étape 2 et commandes de lancement (`mvn clean javafx:run`, `mvn test`). |
| `docs/Conception_Etape1_CineTrackV2.md` | Dossier de conception Étape 1 (récapitulatif des diagrammes UML, MLD 8 tables, scripts SQL et jeu de données). |
| `src/main/java/main/Main.java` | Point d'entrée JavaFX (`Application`) : charge `main-view.fxml` et applique `/css/style.css`. |
| `src/main/java/database/DatabaseConnection.java` | Gestionnaire centralisé JDBC PostgreSQL (`jdbc:postgresql://localhost:5432/cinetrack_groupe3`) avec `getConnection()` et `testConnection()`. |

### 3.2 Couche Controller (`src/main/java/controller/`)
| Contrôleur | Vue associée & Rôle actuel |
| :--- | :--- |
| `MainControleur.java` | Contrôle `main-view.fxml` : navigation entre les vues (`films`, `salles`, `seances`, `clients`, `tarifs`). |
| `FilmControleur.java` | Contrôle `films-view.fxml` : affiche la liste des films via `FilmService`. |
| `SalleControleur.java` | Contrôle `salles-view.fxml` : affiche la liste des salles via `SalleService`. |
| `SeanceControleur.java` | Contrôle `seances-view.fxml` : affiche la liste des séances via `SeanceService`. |
| `ClientControleur.java` | Contrôle `clients-view.fxml` : affiche la liste des clients via `ClientService`. |
| `TarifControleur.java` | Contrôle `tarifs-view.fxml` : affiche la grille tarifaire via `TarifService`. |

### 3.3 Couche Service (`src/main/java/service/`)
| Service | Rôle actuel |
| :--- | :--- |
| `FilmService.java` | Reçoit `FilmDao` par constructeur (défaut `FilmDaoImpl`) et expose `listerFilms()`. |
| `SalleService.java` | Reçoit `SalleDao` par constructeur (défaut `SalleDaoImpl`) et expose `listerSalles()`. |
| `SeanceService.java` | Reçoit `SeanceDao` par constructeur (défaut `SeanceDaoImpl`) et expose `listerSeances()`. |
| `ClientService.java` | Reçoit `ClientDao` par constructeur (défaut `ClientDaoImpl`) et expose `listerClients()`. |
| `TarifService.java` | Reçoit `TarifDao` par constructeur (défaut `TarifDaoImpl`) et expose `listerTarifs()`. |

### 3.4 Couche Modèle (`src/main/java/model/`)
| Modèle | Table PostgreSQL & Rôle actuel |
| :--- | :--- |
| `Utilisateur.java` | Entité POJO table `utilisateurs`. |
| `Film.java` | Entité POJO table `films`. |
| `Salle.java` | Entité POJO table `salles`. |
| `Seance.java` | Entité POJO table `seances`. |
| `Client.java` | Entité POJO table `clients`. |
| `Tarif.java` | Entité POJO table `tarifs`. |
| `Reservation.java` | Entité POJO table `reservations`. |
| `LogSecurite.java` | Entité POJO table `logs_securite`. |

### 3.5 Couche DAO (`src/main/java/dao/` & `src/main/java/impl/`)
| Interface DAO (`dao/`) | Implémentation JDBC (`impl/`) | Rôle actuel |
| :--- | :--- | :--- |
| `FilmDao` | `FilmDaoImpl` | Lecture JDBC `findAll()` sur `films`. |
| `SalleDao` | `SalleDaoImpl` | Lecture JDBC `findAll()` sur `salles`. |
| `SeanceDao` | `SeanceDaoImpl` | Lecture JDBC `findAll()` sur `seances` avec jointures `films` et `salles`. |
| `ClientDao` | `ClientDaoImpl` | Lecture JDBC `findAll()` sur `clients`. |
| `TarifDao` | `TarifDaoImpl` | Lecture JDBC `findAll()` sur `tarifs`. |

### 3.6 Ressources Vues FXML & CSS (`src/main/resources/`)
| Fichier | Rôle actuel |
| :--- | :--- |
| `view/main-view.fxml` | Fenêtre principale avec menu latéral et conteneur central. |
| `view/films-view.fxml` | Vue d'affichage des films. |
| `view/salles-view.fxml` | Vue d'affichage des salles. |
| `view/seances-view.fxml` | Vue d'affichage des séances. |
| `view/clients-view.fxml` | Vue d'affichage des clients. |
| `view/tarifs-view.fxml` | Vue d'affichage des tarifs. |
| `css/style.css` | Feuille de style minimaliste. |

### 3.7 Base de Données SQL (`sql/`) & Tests (`src/test/`)
| Fichier | Rôle actuel |
| :--- | :--- |
| `sql/v2_create_tables.sql` | Script DDL PostgreSQL créant les 8 tables métier (`utilisateurs`, `salles`, `films`, `seances`, `clients`, `tarifs`, `reservations`, `logs_securite`) et les index associés. |
| `sql/v2_triggers_procedures.sql` | Déclencheurs (`trg_verif_conflit_seance`, `trg_maj_places_reservation`, `trg_historique_tarifs`, `trg_log_suppression_film`, `trg_log_suppression_salle`) et procédures stockées (`sp_creer_reservation`, `sp_annuler_reservation`, `sp_enregistrer_tentative_connexion`). |
| `sql/v2_insert_data.sql` | Jeu de données de test initial (utilisateurs, salles, films, séances, clients, tarifs, réservations, logs). |
| `sql/v2_requetes_statistiques.sql` | Requêtes SQL d'exploitation et de statistiques. |
| `sql/v2_script_complet_pgadmin.sql` | Script tout-en-un consolidé pour exécution directe dans pgAdmin 4. |
| `src/test/java/service/CineTrackArchitectureTest.java` | Tests JUnit 5 validant la connexion PostgreSQL (`cinetrack_groupe3`), l'injection de dépendances par constructeur et la lecture MVC via les 5 services. |

---

## 4. JOURNAL DES MODIFICATIONS DE L'ÉQUIPE

- **2026-11-17 — Recentrage strict sur l'Étape 2 uniquement (Interface JavaFX + Architecture MVC + Injection + PostgreSQL) :**
  - **Fichiers créés / modifiés / supprimés :**
    - Suppression des fonctionnalités des étapes ultérieures (CRUDs Étapes 3-5, Authentification/Cybersécurité Étape 6, Statistiques/Exports Étape 7, `exceptions/`, `TestArchitectureMVC.java`) pour ne garder que le périmètre de l'Étape 2.
    - Mise en place d'une base MVC simple et lisible : `main-view.fxml` (`MainControleur`) + 5 vues de consultation (`films-view.fxml`, `salles-view.fxml`, `seances-view.fxml`, `clients-view.fxml`, `tarifs-view.fxml`) avec leurs contrôleurs dédiés (`*Controleur`), services avec injection de dépendance par constructeur (`*Service(XxxDao)`), interfaces `dao/*Dao.java` et implémentations JDBC `impl/*DaoImpl.java` (`findAll()`) connectées à `cinetrack_groupe3`.
    - Mise à jour de `CineTrackArchitectureTest.java`, `README.md` et `style.css`.
  - **Vérification :** `mvn clean test` (tests Étape 2 exécutés sur `cinetrack_groupe3`).
  - **Points d'attention :** les CRUDs, validations/exceptions, billetterie, authentification et statistiques seront ajoutés progressivement lors des Étapes 3 à 7.

- **2026-11-17 — Découpage clair 1 contrôleur / 1 vue par écran & suppression du badge BDD sur le login :**
  - **Fichiers créés / modifiés :**
    - Suppression de `lblEtatBdd` (« Base PostgreSQL connectée ») dans `view/login-view.fxml` et `AuthentificationControleur.java`, ainsi que de l'affichage console de connexion BDD au démarrage dans `Main.java`.
    - Découpage en contrôleurs et vues simples et séparés (sans `TabPane` ni `Map<String, Object>` ni lambdas dans les tableaux) : `BilletterieControleur` (`billetterie-view.fxml`), `ClientControleur` (`clients-view.fxml`), `SeanceControleur` (`seances-view.fxml`), `FilmControleur` (`films-view.fxml`), `SalleControleur` (`salles-view.fxml`), `TarifControleur` (`tarifs-view.fxml`), `StatistiqueControleur` (`statistiques-view.fxml`) et `AdministrationControleur` (`administration-view.fxml`). Suppression de `ProgrammationControleur.java` et `programmation-view.fxml`.
    - Séparation de `StatistiqueDao` / `StatistiqueDaoImpl` (avec modèles `StatFilm` et `StatOccupation`) et de `LogSecuriteDao` / `LogSecuriteDaoImpl`, et utilisation de `PropertyValueFactory` dans tous les tableaux.
  - **Vérification :** `mvn clean test` (4/4 tests JUnit 5 passés, 0 échec) et `main.TestArchitectureMVC` exécuté avec succès sur `cinetrack_groupe3`.
  - **Points d'attention :** chaque écran possède désormais son propre fichier `.fxml` dans `src/main/resources/view/` et son propre contrôleur dans `src/main/java/controller/`.

- **2026-11-17 — Simplification niveau L2 BTS SIO, package `impl/` externe, vues minimalistes & BDD `cinetrack_groupe3` :**
  - **Fichiers créés / modifiés :**
    - Suppression de `IDAO.java`, `I*DAO.java`, `dao/impl/*`, `IExportService.java` et `main/AppContext.java`.
    - Création des 8 interfaces DAO sans préfixe `I` ni `extends IDAO<T>` dans `src/main/java/dao/` (`ClientDao.java`, `FilmDao.java`, `SalleDao.java`, `SeanceDao.java`, `TarifDao.java`, `ReservationDao.java`, `UtilisateurDao.java`, `LogSecuriteDao.java`) et déplacement des 8 implémentations JDBC dans le package externe `src/main/java/impl/` (`*DaoImpl.java`).
    - Simplification de `DatabaseConnection.java` vers `jdbc:postgresql://localhost:5432/cinetrack_groupe3`, et création + initialisation de la base PostgreSQL `cinetrack_groupe3` avec `sql/v2_script_complet_pgadmin.sql` (8 tables, triggers, 3 procédures stockées, données de test).
    - Simplification de `ExportService.java`, `AuthentificationService.java`, `BilletterieService.java`, `ProgrammationService.java`, `StatistiqueEtSauvegardeService.java`, des 5 contrôleurs (`controller/*.java`), de `Main.java`, `TestArchitectureMVC.java`, `CineTrackArchitectureTest.java` et `README.md` (instanciation directe des services/DAO sans `AppContext` ni `var`).
    - Refonte minimaliste de `src/main/resources/css/style.css` et des 5 vues FXML (`src/main/resources/view/*.fxml`) ; suppression des boutons de remplissage démo et des identifiants sur `login-view.fxml`.
  - **Vérification :** `mvn clean test` (4/4 tests JUnit 5 passés, 0 échec) et `main.TestArchitectureMVC` exécuté avec succès sur `cinetrack_groupe3` ; diagrammes `.vpp` non modifiés.
  - **Points d'attention :** lancer l'application avec `mvn javafx:run` ; les vues FXML se trouvent dans `src/main/resources/view/` et la base cible est désormais `cinetrack_groupe3` sur `localhost:5432`.

- **2026-11-16 — Initialisation du projet & de `GEMINI.md` :** État initial complet de CineTrack V2 consolidé et vérifié : architecture MVC Java 21 / JavaFX 21.0.2 / Maven / PostgreSQL implémentée (`5` contrôleurs, `5` fichiers service, `9` interfaces DAO, `8` implémentations JDBC, `8` modèles, `5` exceptions métier, `AppContext`, `Main`, `TestArchitectureMVC`, `5` vues FXML + `style.css`, `5` scripts SQL, tests JUnit 5 `CineTrackArchitectureTest`), diagrammes UML Visual Paradigm (`diagramme vpp/CineTrackV2_Complet.vpp` et `diagrammes/CineTrackV2_Complet.vpp`) réparés (boîtes englobantes, points acteurs et centrage des libellés sur 98 connecteurs), et création de `GEMINI.md` comme mémoire partagée d'équipe. **Points d'attention :** lancer l'application avec `mvn javafx:run` (et non `Run` direct sur `Main.java` sans `--module-path`) ; s'assurer que PostgreSQL tourne sur `localhost:5432` avec la base `projetcinetrackv2` initialisée ; vérifier le chemin `pg_dump.exe` (`PostgreSQL\18`) dans `StatistiqueEtSauvegardeService` selon le poste de développement.
- **2026-11-17 — Correction de la nouvelle version des diagrammes `.vpp` (108 connecteurs) :** Suite au remplacement de `diagramme vpp/CineTrackV2_Complet.vpp` et `diagrammes/CineTrackV2_Complet.vpp` par la version `958 Ko` (avec `S'authentifier` unifié et 14 associations acteurs), correction de `Caissier` (`x=50`), recalcul des points d'ancrage sur les bords des formes dans `1_UseCase_CineTrackV2`, et recalcul des boîtes englobantes (`x`, `y`, `width`, `height`) + centrage `_captionUIModel` sur l'ensemble des **108 connecteurs** des 6 diagrammes (`box_err=0`, `bound_err=0`, SHA-256 `d95ffca14d5c5736`) ; nettoyage des verrous/autosaves Visual Paradigm (`vsparadigme/tmp`, `.autoSaveProjectInfo`). **Points d'attention :** toujours ouvrir directement `diagramme vpp/CineTrackV2_Complet.vpp` dans Visual Paradigm (et non `.systemproject` / l'autosave) et fermer Visual Paradigm avant de commit/push le `.vpp`.
- **2026-11-17 — Simplification UML & réorganisation propre de `1_UseCase_CineTrackV2` (`95` connecteurs) :** Passage à la version UML allégée dans `diagramme vpp/CineTrackV2_Complet.vpp` et `diagrammes/CineTrackV2_Complet.vpp` : suppression des 14 liens `<<Include>>` redondants vers `S'authentifier` et ajout de l'association directe `Caissier -> S'authentifier` (héritée par `Manager` et `Administrateur`), validation de l'orientation Visual Paradigm des 2 généralisations acteurs (`from=Caissier -> to=Manager`, `from=Manager -> to=Administrateur` afin que les triangles `△` pointent vers le haut sur les acteurs parents), et réorganisation en 3 bandes / 3 colonnes propres avec `0` chevauchement et `0` traversée de bulle (`total_conns=95`, `box_err=0`, `bound_err=0`, `overlaps=0`, `crossings=0`). **Points d'attention :** dans Visual Paradigm, le diagramme `1_UseCase_CineTrackV2` reflète désormais l'accès à `S'authentifier` par héritage depuis `Caissier` plutôt que par 14 `<<Include>>` ; penser à fermer Visual Paradigm avant tout `git commit/push`.
- **2026-11-17 — Validation Étape 1 (Base PostgreSQL `projetcinetrackv2`, 8 tables) & Tests JUnit 5 :** Création de la base PostgreSQL `projetcinetrackv2` sur `localhost:5432` (`E:\postgres\bin\psql.exe`) et exécution complète de `sql/v2_script_complet_pgadmin.sql` (8 tables métier respectant la contrainte 5–10 tables : `utilisateurs`, `salles`, `films`, `seances`, `clients`, `tarifs`, `reservations`, `logs_securite`, triggers, 3 procédures stockées et jeu de données de test), en conservant intact le `1_UseCase_CineTrackV2` modifié manuellement dans `diagramme vpp/CineTrackV2_Complet.vpp` ainsi que les 5 autres diagrammes UML (`box_err=0` sur `2_Classes_Entites_BDD`, `3_Classes_Architecture_MVC_Services` et les 3 diagrammes de séquence). Exécution de `mvn test` (`CineTrackArchitectureTest`) : **4/4 tests passés** (`Failures: 0, Errors: 0`). **Points d'attention :** sur ce poste PostgreSQL 18 est installé dans `E:\postgres\bin\` (et non `C:\Program Files\PostgreSQL\18\bin\`) ; `diagramme vpp/CineTrackV2_Complet.vpp` contient les dernières modifs manuelles du Use Case (pensez à copier vers `diagrammes/CineTrackV2_Complet.vpp` après fermeture de Visual Paradigm si besoin de synchronisation).
