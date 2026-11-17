# GEMINI.md — Règles et Mémoire Partagée du Projet CineTrack V2

> **Contexte :** Application Backoffice Desktop **CineTrack V2** pour le **Cinéma Le Lumière** (prestataire **DevCore**, BTS SIO SLAM — Groupe 3).  
> **Stack technique :** Java 21, JavaFX 21.0.2 (FXML + CSS), Maven (`mvn javafx:run` / `mvn test`), JDBC PostgreSQL (`42.7.3`), JUnit 5 (`5.10.2`).  
> **Base de données cible :** PostgreSQL `jdbc:postgresql://localhost:5432/cinetrack_groupe3` (8 tables métier, triggers et procédures stockées).

---

## 1. RÈGLES STRICTES POUR L'IA (OBLIGATOIRE)

- **Protection du code existant :** Ne **jamais** réécrire, supprimer ou refactoriser les fichiers et contrôleurs existants listés dans la section **« 3. CODE VERROUILLÉ (DÉJÀ IMPLÉMENTÉ) »**, sauf si l'utilisateur te donne une autorisation explicite sur un fichier précis.
- **Architecture MVC stricte :** Respecter impérativement l'architecture en couches du projet (JavaFX, Maven, PostgreSQL) et la séparation des packages :
  - `controller` : uniquement la logique UI JavaFX (`@FXML`), aucun SQL ni règle métier complexe ;
  - `service` : règles métier et validations, **zéro import JavaFX**, **zéro SQL direct**, utilisation des interfaces `dao` / implémentations `impl` ;
  - `dao` / `impl` : interfaces `*Dao` dans `dao/` (sans préfixe `I`, sans `IDAO<T>`) et implémentations JDBC PostgreSQL `*DaoImpl` dans le package externe `impl/` par requêtes préparées (`PreparedStatement` / `CallableStatement`) ;
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
| `src/main/java/main/Main.java` | Point d'entrée JavaFX (`Application`) : teste la connexion PostgreSQL au démarrage, charge `login-view.fxml` et `dashboard-view.fxml` via `FXMLLoader.load(...)`, applique `/css/style.css` et gère les transitions de scènes. |
| `src/main/java/main/TestArchitectureMVC.java` | Classe utilitaire CLI de validation de bout en bout (connexion PostgreSQL `cinetrack_groupe3`, authentification des 3 rôles, lecture des 8 tables, calcul tarifaire, conflit horaire et disponibilité des places). |
| `src/main/java/database/DatabaseConnection.java` | Gestionnaire utilitaire centralisé JDBC PostgreSQL (`jdbc:postgresql://localhost:5432/cinetrack_groupe3`) avec `getConnection()` et `testConnection()`. |
| `src/main/java/exceptions/CineTrackException.java` | Exception métier racine portant un `codeErreur`. |
| `src/main/java/exceptions/AuthentificationException.java` | Levée lors d'identifiants invalides (`ERR_AUTHENTIFICATION`). |
| `src/main/java/exceptions/CompteBloqueException.java` | Levée lorsqu'un compte est verrouillé après 3 échecs (`ERR_COMPTE_BLOQUE`). |
| `src/main/java/exceptions/ConflitHoraireException.java` | Levée lorsqu'une séance chevauche une autre séance dans la même salle (`ERR_CONFLIT_HORAIRE`). |
| `src/main/java/exceptions/PlaceIndisponibleException.java` | Levée quand le nombre de places demandées dépasse `places_dispo` (`ERR_PLACES_INDISPONIBLES`). |

### 3.2 Couche Controller (`src/main/java/controller/`)
| Contrôleur | Vue associée & Rôle actuel |
| :--- | :--- |
| `AuthentificationControleur.java` | Contrôle `login-view.fxml` : affiche l'état de connexion PostgreSQL et vérifie les identifiants via `AuthentificationService` avant redirection vers le tableau de bord (sans identifiants de test affichés à l'écran). |
| `DashboardControleur.java` | Contrôle `dashboard-view.fxml` : affiche l'utilisateur connecté et son rôle, gère la navigation entre sous-vues (`billetterie-view.fxml`, `programmation-view.fxml`, `statistiques-view.fxml`), restreint l'accès aux statistiques aux rôles `MANAGER` / `ADMIN`, et gère la déconnexion. |
| `BilletterieControleur.java` | Contrôle `billetterie-view.fxml` : sélection séance/client/tarif, calcul dynamique du devis et des places restantes, réservation avec génération et affichage du billet unique récapitulatif, annulation de réservation, création et recherche de fiches clients. |
| `ProgrammationControleur.java` | Contrôle `programmation-view.fxml` : gestion multi-onglets des Séances (programmation, filtrage par date/salle/film, suppression, conflits horaires), Films (CRUD + recherche titre/genre), Salles (CRUD) et Tarifs (modification et archivage), avec désactivation des actions d'édition pour le `CAISSIER`. |
| `StatistiqueControleur.java` | Contrôle `statistiques-view.fxml` : KPI globaux (CA total, billets vendus, réservations confirmées), tableaux Top Films et Taux d'occupation par séance, export CSV (`exports/rapport_exploitation_cinetrack.csv`), et onglet Administration réservé à `ADMIN` (création/déblocage/suppression d'utilisateurs, consultation `logs_securite`, sauvegarde PostgreSQL `backups/`). |

### 3.3 Couche Service (`src/main/java/service/`)
| Service / Interface | Rôle actuel |
| :--- | :--- |
| `ExportService.java` | Contrat de service pour l'export CSV (`exporterCSV`) et la sauvegarde BDD (`executerSauvegardeBDD`). |
| `AuthentificationService.java` | Authentification des 3 rôles (`ADMIN`, `MANAGER`, `CAISSIER`), session utilisateur partagée (`utilisateurConnecte`), blocage automatique après 3 tentatives échouées, hachage SHA-256 formaté `$2a$12$`, gestion des comptes et journalisation dans `logs_securite`. |
| `BilletterieService.java` | Calcul automatique du montant total (`calculerMontantTotal`), contrôle de disponibilité des places et réservation via procédure stockée `sp_creer_reservation`, annulation via `sp_annuler_reservation`, gestion/recherche des clients et modification des tarifs (réservée `MANAGER`/`ADMIN`). |
| `ProgrammationService.java` | CRUD et recherche sur les Films et les Salles avec validations métier et contrôle de droits (`estManagerOuPlus()`) sur les suppressions ; programmation/modification/suppression de Séances avec détection des conflits horaires applicative (`verifierConflitHoraire`) et interception du trigger SQL `trg_verif_conflit_seance`. |
| `StatistiqueEtSauvegardeService.java` | Implémente `ExportService` : agrégations d'exploitation (CA, billets, réservations, Top Films, occupation, CA par période), consultation de `logs_securite`, génération d'export CSV avec BOM UTF-8 et sauvegarde PostgreSQL (`cinetrack_groupe3`) via `pg_dump.exe`. |

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

### 3.5 Couche DAO (`src/main/java/dao/` & `src/main/java/impl/`)
| Interface DAO (`dao/`) | Implémentation JDBC (`impl/`) | Rôle actuel |
| :--- | :--- | :--- |
| `UtilisateurDao` | `UtilisateurDaoImpl` | CRUD `utilisateurs`, recherche par login, incrément/réinitialisation des tentatives échouées et déblocage de compte. |
| `FilmDao` | `FilmDaoImpl` | CRUD `films`, recherche multicritères (`titre`, `genre`) et liste des genres distincts. |
| `SalleDao` | `SalleDaoImpl` | CRUD `salles` et recherche par nom (`findByNom`). |
| `SeanceDao` | `SeanceDaoImpl` | CRUD `seances` avec jointures `films`/`salles`, recherche par critères (`date`, `idSalle`, `idFilm`) et vérification SQL des chevauchements horaires (`verifierConflitHoraire`). |
| `ClientDao` | `ClientDaoImpl` | CRUD `clients` et recherche par mot-clé (`rechercher`). |
| `TarifDao` | `TarifDaoImpl` | CRUD `tarifs`, filtrage des tarifs actifs (`findActifs`) et recherche par catégorie (`findByCategorie`). |
| `ReservationDao` | `ReservationDaoImpl` | Lecture/CRUD `reservations`, recherche par `numeroBillet` et appels aux procédures stockées PostgreSQL `sp_creer_reservation` et `sp_annuler_reservation` via `CallableStatement`. |
| `LogSecuriteDao` | `LogSecuriteDaoImpl` | Insertion/lecture dans `logs_securite` (CC4) et requêtes d'indicateurs statistiques (CA total, billets, réservations confirmées, Top Films, taux d'occupation, CA par période). |

### 3.6 Ressources Vues FXML & CSS (`src/main/resources/`)
| Fichier | Rôle actuel |
| :--- | :--- |
| `view/login-view.fxml` | Écran de connexion minimaliste (état BDD, champs login/mot de passe, sans comptes de test affichés). |
| `view/dashboard-view.fxml` | Conteneur principal `BorderPane` avec barre latérale de navigation par rôle et `StackPane` central dynamique. |
| `view/billetterie-view.fxml` | Interface de réservation/annulation, aperçu du billet unique et gestion/recherche des clients. |
| `view/programmation-view.fxml` | Interface à onglets pour la programmation des séances (avec filtres), gestion des films, salles et tarifs. |
| `view/statistiques-view.fxml` | Interface KPI, tableaux Top Films / occupation, export CSV, gestion des utilisateurs (`ADMIN`), logs CC4 et sauvegarde BDD. |
| `css/style.css` | Feuille de style minimaliste professionnelle (fond clair `#F4F6F9`, cartes blanches `#FFFFFF`, barre latérale `#1E293B`, accent `#2563EB`). |

### 3.7 Base de Données SQL (`sql/`) & Tests (`src/test/`)
| Fichier | Rôle actuel |
| :--- | :--- |
| `sql/v2_create_tables.sql` | Script DDL PostgreSQL créant les 8 tables métier (`utilisateurs`, `salles`, `films`, `seances`, `clients`, `tarifs`, `reservations`, `logs_securite`) et les index associés. |
| `sql/v2_triggers_procedures.sql` | Déclencheurs (`trg_verif_conflit_seance`, `trg_maj_places_reservation`, `trg_historique_tarifs`, `trg_log_suppression_film`, `trg_log_suppression_salle`) et procédures stockées (`sp_creer_reservation`, `sp_annuler_reservation`, `sp_enregistrer_tentative_connexion`). |
| `sql/v2_insert_data.sql` | Jeu de données de test initial (utilisateurs, salles, films, séances, clients, tarifs, réservations, logs). |
| `sql/v2_requetes_statistiques.sql` | Requêtes SQL d'exploitation et de statistiques (films les plus réservés, taux d'occupation, CA jour/mois, audit sécurité). |
| `sql/v2_script_complet_pgadmin.sql` | Script tout-en-un consolidé pour exécution directe dans pgAdmin 4. |
| `src/test/java/service/CineTrackArchitectureTest.java` | Suite de tests JUnit 5 validant le calcul automatique de tarif, la détection de conflit horaire, le contrôle de disponibilité des places et l'authentification des 3 rôles. |

---

## 4. JOURNAL DES MODIFICATIONS DE L'ÉQUIPE

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
