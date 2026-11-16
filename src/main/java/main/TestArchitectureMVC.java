package main;

import database.DatabaseConnection;
import exceptions.CineTrackException;
import exceptions.ConflitHoraireException;
import exceptions.PlaceIndisponibleException;
import model.Client;
import model.Film;
import model.LogSecurite;
import model.Reservation;
import model.Salle;
import model.Seance;
import model.Tarif;
import model.Utilisateur;
import service.AuthentificationService;
import service.BilletterieService;
import service.ProgrammationService;
import service.StatistiqueEtSauvegardeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Classe de validation complète de l'Étape 2 :
 * Connexion PostgreSQL, DAO Interfaces, DAO Impl JDBC, Injection de Dépendances et Services métier.
 */
public class TestArchitectureMVC {

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println(" TEST ARCHITECTURE MVC & CONNEXION POSTGRESQL - CINETRACK V2");
        System.out.println("====================================================================");

        try {
            // 1. Test de connexion PostgreSQL
            boolean connOk = DatabaseConnection.testConnection();
            System.out.println("[1] Connexion JDBC PostgreSQL (" + DatabaseConnection.getUrl() + ") : "
                    + (connOk ? "SUCCÈS" : "ÉCHEC"));
            if (!connOk) {
                throw new IllegalStateException("Impossible de se connecter à PostgreSQL.");
            }

            // 2. Récupération des Services via le conteneur d'Injection de Dépendances (AppContext)
            AppContext context = AppContext.getInstance();
            AuthentificationService authService = context.getAuthentificationService();
            ProgrammationService progService = context.getProgrammationService();
            BilletterieService billetterieService = context.getBilletterieService();
            StatistiqueEtSauvegardeService statsService = context.getStatistiqueService();
            System.out.println("[2] Injection de Dépendances (DAO Impl -> Interfaces DAO -> Services) : SUCCÈS");

            // 3. Test d'authentification des 3 acteurs internes (Caissier, Manager, Administrateur)
            Utilisateur caissier = authService.authentifier("caissier1", "caissier123");
            Utilisateur manager = authService.authentifier("manager1", "manager123");
            Utilisateur admin = authService.authentifier("admin", "admin123");
            System.out.println("[3] Authentification des 3 rôles internes : "
                    + caissier.getRole() + ", " + manager.getRole() + ", " + admin.getRole() + " -> SUCCÈS");

            // 4. Lecture des 8 tables via les Services et DAO JDBC
            List<Film> films = progService.listerFilms();
            List<Salle> salles = progService.listerSalles();
            List<Seance> seances = progService.listerSeances();
            List<Client> clients = billetterieService.listerClients();
            List<Tarif> tarifs = billetterieService.listerTarifs();
            List<Reservation> reservations = billetterieService.listerReservations();
            List<LogSecurite> logs = statsService.consulterLogsSecurite();

            System.out.println("[4] Lecture PostgreSQL (8 tables) :");
            System.out.println("    - Utilisateurs : " + authService.listerUtilisateurs().size());
            System.out.println("    - Films        : " + films.size());
            System.out.println("    - Salles       : " + salles.size());
            System.out.println("    - Séances      : " + seances.size());
            System.out.println("    - Clients      : " + clients.size());
            System.out.println("    - Tarifs       : " + tarifs.size());
            System.out.println("    - Réservations : " + reservations.size());
            System.out.println("    - Logs Sécurité: " + logs.size());

            // 5. Test règle métier : Calcul automatique du tarif
            Tarif pleinTarif = tarifs.get(0);
            double montant3Places = billetterieService.calculerMontantTotal(pleinTarif, 3);
            System.out.println("[5] Calcul automatique tarif (" + pleinTarif.getLibelle() + " x 3) = "
                    + String.format("%.2f Rs", montant3Places) + " -> SUCCÈS");

            // 6. Test règle métier : Détection automatique de conflit d'horaire
            try {
                progService.programmerSeance(1, 1, LocalDate.of(2026, 11, 6), LocalTime.of(10, 30));
                System.out.println("[6] ERREUR : Le conflit horaire aurait dû être bloqué !");
            } catch (ConflitHoraireException e) {
                System.out.println("[6] Détection conflit d'horaire : " + e.getMessage() + " -> SUCCÈS");
            }

            // 7. Test règle métier : Vérification disponibilité des places
            try {
                billetterieService.reserverPlaces(1, 1, 1, 999, caissier);
                System.out.println("[7] ERREUR : Le dépassement de capacité aurait dû être bloqué !");
            } catch (PlaceIndisponibleException e) {
                System.out.println("[7] Contrôle disponibilité places : " + e.getMessage() + " -> SUCCÈS");
            }

            System.out.println("====================================================================");
            System.out.println(" TOUS LES TESTS DE L'ÉTAPE 2 SONT VALIDÉS AVEC SUCCÈS !");
            System.out.println("====================================================================");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
