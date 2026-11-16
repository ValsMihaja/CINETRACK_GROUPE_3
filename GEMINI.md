# GEMINI.md — Règles et Mémoire Partagée du Projet CineTrack V2

> **Contexte :** Application Backoffice Desktop **CineTrack V2** pour le **Cinéma Le Lumière** (prestataire **DevCore**, BTS SIO SLAM — Groupe 3).  
> **Stack technique :** Java 21, JavaFX 21.0.2 (FXML + CSS), Maven (`mvn javafx:run` / `mvn test`), JDBC PostgreSQL (`42.7.3`), JUnit 5 (`5.10.2`).  
> **Base de données cible :** PostgreSQL `jdbc:postgresql://localhost:5432/projetcinetrackv2` (8 tables métier, triggers et procédures stockées).

---

## 1. RÈGLES STRICTES POUR L'IA (OBLIGATOIRE)

- **Protection du code existant :** Ne **jamais** réécrire, supprimer ou refactoriser les fichiers et contrôleurs existants listés dans la section **« 3. CODE VERROUILLÉ (DÉJÀ IMPLÉMENTÉ) »**, sauf si l'utilisateur te donne une autorisation explicite sur un fichier précis.
- **Architecture MVC stricte :** Respecter impérativement l'architecture en couches du projet (JavaFX, Maven, PostgreSQL) et la séparation des packages :
  - `controller` : uniquement la logique UI JavaFX (`@FXML`), aucun SQL ni règle métier complexe ;
  - `service` : règles métier et validations, **zéro import JavaFX**, **zéro SQL direct**, injection des interfaces DAO par constructeur ;
  - `dao` / `dao.impl` : interfaces `IDAO<T>` / `I*DAO` et implémentations JDBC PostgreSQL exclusivement par requêtes préparées (`PreparedStatement` / `CallableStatement`) ;
  - `model` : entités métier POJO alignées sur les 8 tables PostgreSQL ;
  - `database` : gestionnaire de connexion JDBC centralisé ;
  - `exceptions` : hiérarchie d'exceptions métier héritant de `CineTrackException`.
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

### 3.1 Configuration, Documentation & Point d'entrée (`racine`, `main/`, `database/`, `exceptions/`)
| Élément | Rôle actuel |
| :--- | :--- |
| `pom.xml` | Configuration Maven Java 21 : dépendances `javafx-controls` et `javafx-fxml` (`21.0.2`), driver `postgresql` (`42.7.3`), `junit-jupiter` (`5.10.2`), plugins `maven-compiler-plugin` (`3.13.0`) et `javafx-maven-plugin` (`0.0.8`, `mainClass=main.Main`). |
| `README.md` | Documentation d'architecture MVC en couches, structure des dossiers, comptes de test (`caissier1`, `manager1`, `admin`) et commandes de lancement (`mvn clean javafx:run`, `mvn test`). |
| `docs/Conception_Etape1_CineTrackV2.md` | Dossier de conception Étape 1 (récapitulatif des diagrammes UML, MLD 8 tables, scripts SQL et jeu de données). |
| `src/main/java/main/Main.java` | Point d'entrée JavaFX (`Application`) : teste la connexion PostgreSQL au démarrage, charge `login-view.fxml` et `dashboard-view.fxml` via `AppContext`, applique `/css/style.css` et gère les transitions de scènes. |
| `src/main/java/main/AppContext.java` | Conteneur singleton d'injection de dépendances (IoC) : instancie les 8 `*DAOImpl`, les injecte dans les 4 services, et fournit une `ControllerFactory` à `FXMLLoader` pour injecter les services dans les 5 contrôleurs JavaFX. |
| `src/main/java/main/TestArchitectureMVC.java` | Classe utilitaire CLI de validation de bout en bout (connexion PostgreSQL, IoC `AppContext`, authentification des 3 rôles, lecture des 8 tables, calcul tarifaire, conflit horaire et disponibilité des places). |
| `src/main/java/database/DatabaseConnection.java` | Gestionnaire utilitaire centralisé JDBC PostgreSQL (`jdbc:postgresql://localhost:5432/projetcinetrackv2`, surchargeable via propriétés système `cinetrack.db.*`) avec `getConnection()` et `testConnection()`. |
| `src/main/java/exceptions/CineTrackException.java` | Exception métier racine portant un `codeErreur`. |
| `src/main/java/exceptions/AuthentificationException.java` | Levée lors d'identifiants invalides (`ERR_AUTHENTIFICATION`). |
| `src/main/java/exceptions/CompteBloqueException.java` | Levée lorsqu'un compte est verrouillé après 3 échecs (`ERR_COMPTE_BLOQUE`). |
| `src/main/java/exceptions/ConflitHoraireException.java` | Levée lorsqu'une séance chevauche une autre séance dans la même salle (`ERR_CONFLIT_HORAIRE`). |
| `src/main/java/exceptions/PlaceIndisponibleException.java` | Levée quand le nombre de places demandées dépasse `places_dispo` (`ERR_PLACES_INDISPONIBLES`). |

### 3.2 Couche Controller (`src/main/java/controller/`)
| Contrôleur | Vue associée & Rôle actuel |
| :--- | :--- |
| `AuthentificationControleur.java` | Contrôle `login-view.fxml` : affiche l'état de connexion PostgreSQL, vérifie les identifiants via `AuthentificationService`, met à jour l'opérateur courant sur `StatistiqueEtSauvegardeService`, redirige vers le tableau de bord et propose les boutons de remplissage démo (`caissier1`, `manager1`, `admin`). |
| `DashboardControleur.java` | Contrôle `dashboard-view.fxml` : affiche l'utilisateur connecté et son rôle, gère la navigation entre sous-vues (`billetterie-view.fxml`, `programmation-view.fxml`, `statistiques-view.fxml`), restreint l'accès aux statistiques aux rôles `MANAGER` / `ADMIN`, et gère la déconnexion. |
| `BilletterieControleur.java` | Contrôle `billetterie-view.fxml` : sélection séance/client/tarif, calcul dynamique du devis et des places restantes, réservation avec génération et affichage du billet unique récapitulatif, annulation de réservation, création et recherche de fiches clients. |
| `ProgrammationControleur.java` | Contrôle `programmation-view.fxml` : gestion multi-onglets des Séances (programmation, filtrage par date/salle/film, suppression, conflits horaires), Films (CRUD + recherche titre/genre), Salles (CRUD) et Tarifs (modification et archivage), avec désactivation des actions d'édition pour le `CAISSIER`. |
| `StatistiqueControleur.java` | Contrôle `statistiques-view.fxml` : KPI globaux (CA total, billets vendus, réservations confirmées), tableaux Top Films et Taux d'occupation par séance, export CSV (`exports/rapport_exploitation_cinetrack.csv`), et onglet Administration réservé à `ADMIN` (création/déblocage/suppression d'utilisateurs, consultation `logs_securite`, sauvegarde PostgreSQL `backups/`). |

### 3.3 Couche Service (`src/main/java/service/`)
| Service / Interface | Rôle actuel |
| :--- | :--- |
| `IExportService.java` | Contrat de service pour l'export CSV (`exporterCSV`) et la sauvegarde BDD (`executerSauvegardeBDD`). |
| `AuthentificationService.java` | Authentification des 3 rôles (`ADMIN`, `MANAGER`, `CAISSIER`), blocage automatique après 3 tentatives échouées (`MAX_TENTATIVES = 3`), hachage SHA-256 formaté `$2a$12$` et compatibilité hashes BCrypt de démonstration, création/déblocage/suppression de comptes, et journalisation dans `logs_securite`. |
| `BilletterieService.java` | Calcul automatique du montant total (`calculerMontantTotal`), contrôle de disponibilité des places et réservation via procédure stockée `sp_creer_reservation`, annulation via `sp_annuler_reservation`, gestion/recherche des clients et modification des tarifs (réservée `MANAGER`/`ADMIN`). |
| `ProgrammationService.java` | CRUD et recherche sur les Films et les Salles avec validations métier et contrôle de droits (`estManagerOuPlus()`) sur les suppressions ; programmation/modification/suppression de Séances avec détection des conflits horaires applicative (`verifierConflitHoraire`) et interception du trigger SQL `trg_verif_conflit_seance`. |
| `StatistiqueEtSauvegardeService.java` | Implémente `IExportService` : agrégations d'exploitation (CA, billets, réservations, Top Films, occupation, CA par période), consultation de `logs_securite`, génération d'export CSV avec BOM UTF-8 et journalisation, et sauvegarde PostgreSQL via `pg_dump.exe` (avec repli applicatif si absent). |

### 3.4 Couche Modèle (`src/main/java/model/`)
| Modèle | Table PostgreSQL & Rôle actuel |
| :--- | :--- |
| `Utilisateur.java` | Table `utilisateurs` : identifiants, rôle (`ADMIN`, `MANAGER`, `CAISSIER`), `tentativesEchouees`, `actif`, `dateDerniereConnexion`, helpers `estAdmin()`, `estManagerOuPlus()`, `estCompteVerrouille()`, `getStatutTexte()`. |
| `Film.java` | Table `films` : `idFilm`, `titre`, `genre`, `duree` (minutes), `note`, `synopsis`. |
| `Salle.java` | Table `salles` : `idSalle`, `nom`, `capacite`, `equipement`. |
| `Seance.java` | Table `seances` (enrichie par jointure film/salle) : créneau `dateSeance`/`heure`, `placesDispo`, helpers `getHeureFin()`, `getPlacesOccupees()`, `getTauxOccupation()`. |
| `Client.java` | Table `clients` : `idClient`, `nom`, `prenom`, `email`, `telephone`, `dateInscription`, `getNomComplet()`. |
| `Tarif.java` | Table `tarifs` : `categorie` (`PLEIN_TARIF`, `TARIF_REDUIT`, `TARIF_ETUDIANT`, `ABONNEMENT`), `libelle`, `prixUnitaire`, `description`, `actif`, et `calculerMontant(nbPlaces)`. |
| `Reservation.java` | Table `reservations` : `numeroBillet`, références séance/client/tarif/utilisateur, `nbPlaces`, `montantTotal`, `dateReservation`, `statut` (`CONFIRMEE`/`ANNULEE`), et `genererRecapitulatifBillet()`. |
| `LogSecurite.java` | Table `logs_securite` (CC4) : `idLog`, `idUtilisateur`, `utilisateurLogin`, `action`, `tableConcernee`, `donneesConcernees`, `dateHeure`. |

### 3.5 Couche DAO (`src/main/java/dao/` & `src/main/java/dao/impl/`)
| Interface DAO | Implémentation JDBC | Rôle actuel |
| :--- | :--- | :--- |
| `IDAO<T>` | *(interface générique)* | Contrat CRUD générique (`findAll`, `findById`, `insert`, `update`, `delete`). |
| `IUtilisateurDAO` | `UtilisateurDAOImpl` | CRUD `utilisateurs`, recherche par login, incrément/réinitialisation des tentatives échouées et déblocage de compte. |
| `IFilmDAO` | `FilmDAOImpl` | CRUD `films`, recherche multicritères (`titre`, `genre`) et liste des genres distincts. |
| `ISalleDAO` | `SalleDAOImpl` | CRUD `salles` et recherche par nom (`findByNom`). |
| `ISeanceDAO` | `SeanceDAOImpl` | CRUD `seances` avec jointures `films`/`salles`, recherche par critères (`date`, `idSalle`, `idFilm`) et vérification SQL des chevauchements horaires (`verifierConflitHoraire`). |
| `IClientDAO` | `ClientDAOImpl` | CRUD `clients` et recherche par mot-clé (`rechercher`). |
| `ITarifDAO` | `TarifDAOImpl` | CRUD `tarifs`, filtrage des tarifs actifs (`findActifs`) et recherche par catégorie (`findByCategorie`). |
| `IReservationDAO` | `ReservationDAOImpl` | Lecture/CRUD `reservations`, recherche par `numeroBillet` et appels aux procédures stockées PostgreSQL `sp_creer_reservation` et `sp_annuler_reservation` via `CallableStatement`. |
| `ILogSecuriteDAO` | `LogSecuriteDAOImpl` | Insertion/lecture dans `logs_securite` (CC4) et requêtes d'indicateurs statistiques (CA total, billets, réservations confirmées, Top Films, taux d'occupation, CA par période). |

### 3.6 Ressources Vues FXML & CSS (`src/main/resources/`)
| Fichier | Rôle actuel |
| :--- | :--- |
| `view/login-view.fxml` | Écran de connexion (état BDD, champs login/mot de passe, boutons de démonstration par rôle). |
| `view/dashboard-view.fxml` | Conteneur principal `BorderPane` avec barre latérale de navigation par rôle et `StackPane` central dynamique. |
| `view/billetterie-view.fxml` | Interface de réservation/annulation, aperçu du billet unique et gestion/recherche des clients. |
| `view/programmation-view.fxml` | Interface à onglets pour la programmation des séances (avec filtres), gestion des films, salles et tarifs. |
| `view/statistiques-view.fxml` | Interface KPI, tableaux Top Films / occupation, export CSV, gestion des utilisateurs (`ADMIN`), logs CC4 et sauvegarde BDD. |
| `css/style.css` | Feuille de style globale (thème Bleu Nuit / Or Lumière / Bordeaux : sidebar, cartes, boutons, badges de statut). |

### 3.7 Base de Données SQL (`sql/`) & Tests (`src/test/`)
| Fichier | Rôle actuel |
| :--- | :--- |
| `sql/v2_create_tables.sql` | Script DDL PostgreSQL créant les 8 tables métier (`utilisateurs`, `salles`, `films`, `seances`, `clients`, `tarifs`, `reservations`, `logs_securite`) et les index associés. |
| `sql/v2_triggers_procedures.sql` | Déclencheurs (`trg_verif_conflit_seance`, `trg_maj_places_reservation`, `trg_historique_tarifs`, `trg_log_suppression_film`, `trg_log_suppression_salle`) et procédures stockées (`sp_creer_reservation`, `sp_annuler_reservation`, `sp_enregistrer_tentative_connexion`). |
| `sql/v2_insert_data.sql` | Jeu de données de test initial (utilisateurs, salles, films, séances, clients, tarifs, réservations, logs). |
| `sql/v2_requetes_statistiques.sql` | Requêtes SQL d'exploitation et de statistiques (films les plus réservés, taux d'occupation, CA jour/mois, audit sécurité). |
| `sql/v2_script_complet_pgadmin.sql` | Script tout-en-un consolidé pour exécution directe dans pgAdmin 4. |
| `src/test/java/service/CineTrackArchitectureTest.java` | Suite de tests JUnit 5 validant `AppContext`, le calcul automatique de tarif, la détection de conflit horaire, le contrôle de disponibilité des places et l'authentification des 3 rôles. |

---

## 4. JOURNAL DES MODIFICATIONS DE L'ÉQUIPE

- **2026-11-16 — Initialisation du projet & de `GEMINI.md` :** État initial complet de CineTrack V2 consolidé et vérifié : architecture MVC Java 21 / JavaFX 21.0.2 / Maven / PostgreSQL implémentée (`5` contrôleurs, `5` fichiers service, `9` interfaces DAO, `8` implémentations JDBC, `8` modèles, `5` exceptions métier, `AppContext`, `Main`, `TestArchitectureMVC`, `5` vues FXML + `style.css`, `5` scripts SQL, tests JUnit 5 `CineTrackArchitectureTest`), diagrammes UML Visual Paradigm (`diagramme vpp/CineTrackV2_Complet.vpp` et `diagrammes/CineTrackV2_Complet.vpp`) réparés (boîtes englobantes, points acteurs et centrage des libellés sur 98 connecteurs), et création de `GEMINI.md` comme mémoire partagée d'équipe. **Points d'attention :** lancer l'application avec `mvn javafx:run` (et non `Run` direct sur `Main.java` sans `--module-path`) ; s'assurer que PostgreSQL tourne sur `localhost:5432` avec la base `projetcinetrackv2` initialisée ; vérifier le chemin `pg_dump.exe` (`PostgreSQL\18`) dans `StatistiqueEtSauvegardeService` selon le poste de développement.
