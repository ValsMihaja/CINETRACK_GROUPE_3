package main;

import database.DatabaseConnection;
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

public class TestArchitectureMVC {

    public static void main(String[] args) {
        try {
            boolean connOk = DatabaseConnection.testConnection();
            System.out.println("1. Connexion PostgreSQL (cinetrack_groupe3) : " + (connOk ? "OK" : "ECHEC"));
            if (!connOk) {
                throw new IllegalStateException("Connexion PostgreSQL impossible.");
            }

            AuthentificationService authService = new AuthentificationService();
            ProgrammationService progService = new ProgrammationService();
            BilletterieService billetterieService = new BilletterieService();
            StatistiqueEtSauvegardeService statsService = new StatistiqueEtSauvegardeService();

            Utilisateur caissier = authService.authentifier("caissier1", "caissier123");
            Utilisateur manager = authService.authentifier("manager1", "manager123");
            Utilisateur admin = authService.authentifier("admin", "admin123");
            System.out.println("2. Authentification rôles : "
                    + caissier.getRole() + ", " + manager.getRole() + ", " + admin.getRole());

            List<Film> films = progService.listerFilms();
            List<Salle> salles = progService.listerSalles();
            List<Seance> seances = progService.listerSeances();
            List<Client> clients = billetterieService.listerClients();
            List<Tarif> tarifs = billetterieService.listerTarifs();
            List<Reservation> reservations = billetterieService.listerReservations();
            List<LogSecurite> logs = statsService.consulterLogsSecurite();

            System.out.println("3. Tables lues : films=" + films.size()
                    + ", salles=" + salles.size()
                    + ", seances=" + seances.size()
                    + ", clients=" + clients.size()
                    + ", tarifs=" + tarifs.size()
                    + ", reservations=" + reservations.size()
                    + ", logs=" + logs.size());

            double montant = billetterieService.calculerMontantTotal(tarifs.get(0), 3);
            System.out.println("4. Calcul tarif : " + String.format("%.2f Rs", montant));

            try {
                progService.programmerSeance(1, 1, LocalDate.of(2026, 11, 6), LocalTime.of(10, 30));
            } catch (ConflitHoraireException e) {
                System.out.println("5. Conflit horaire détecté : OK");
            }

            try {
                billetterieService.reserverPlaces(1, 1, 1, 999, caissier);
            } catch (PlaceIndisponibleException e) {
                System.out.println("6. Contrôle places disponibles : OK");
            }

            System.out.println("Validation terminée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
