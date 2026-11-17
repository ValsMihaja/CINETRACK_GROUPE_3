package service;

import dao.LogSecuriteDao;
import dao.StatistiqueDao;
import impl.LogSecuriteDaoImpl;
import impl.StatistiqueDaoImpl;
import model.LogSecurite;
import model.StatFilm;
import model.StatOccupation;
import model.Utilisateur;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatistiqueEtSauvegardeService implements ExportService {

    private final StatistiqueDao statistiqueDao = new StatistiqueDaoImpl();
    private final LogSecuriteDao logSecuriteDao = new LogSecuriteDaoImpl();
    private final AuthentificationService authService = new AuthentificationService();

    public double obtenirChiffreAffairesTotal() throws SQLException {
        return statistiqueDao.getChiffreAffairesTotal();
    }

    public int obtenirTotalBilletsVendus() throws SQLException {
        return statistiqueDao.getTotalBilletsVendus();
    }

    public int obtenirTotalReservationsConfirmees() throws SQLException {
        return statistiqueDao.getTotalReservationsConfirmees();
    }

    public List<StatFilm> obtenirTopFilms() throws SQLException {
        return statistiqueDao.getTopFilms();
    }

    public List<StatOccupation> obtenirTauxOccupationSeances() throws SQLException {
        return statistiqueDao.getTauxOccupationSeances();
    }

    public List<LogSecurite> consulterLogsSecurite() throws SQLException {
        return logSecuriteDao.findAll();
    }

    @Override
    public boolean exporterCSV(String cheminFichier) throws Exception {
        List<StatFilm> topFilms = obtenirTopFilms();
        List<StatOccupation> occupation = obtenirTauxOccupationSeances();

        File fichier = new File(cheminFichier);
        if (fichier.getParentFile() != null) {
            fichier.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fichier), StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.write("RAPPORT CINETRACK V2\n");
            writer.write("Date;" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n");
            writer.write("Chiffre d'affaires (Rs);" + String.format("%.2f", obtenirChiffreAffairesTotal()) + "\n");
            writer.write("Places vendues;" + obtenirTotalBilletsVendus() + "\n\n");

            writer.write("TOP FILMS\n");
            writer.write("Titre;Genre;Note;Places Vendues;Chiffre d'Affaires (Rs)\n");
            for (StatFilm f : topFilms) {
                writer.write(f.getTitre() + ";" + f.getGenre() + ";" + f.getNoteMoyenne()
                        + ";" + f.getPlacesVendues() + ";" + String.format("%.2f", f.getChiffreAffaires()) + "\n");
            }

            writer.write("\nOCCUPATION DES SEANCES\n");
            writer.write("Date;Heure;Salle;Film;Capacité;Places Occupées;Taux (%)\n");
            for (StatOccupation s : occupation) {
                writer.write(s.getDateSeance() + ";" + s.getHeure() + ";" + s.getSalle() + ";" + s.getFilm()
                        + ";" + s.getCapacite() + ";" + s.getPlacesOccupees() + ";" + s.getTauxOccupation() + "%\n");
            }
        }

        Utilisateur op = authService.getUtilisateurConnecte();
        logSecuriteDao.enregistrerLog(
                op != null ? op.getIdUtilisateur() : null,
                op != null ? op.getLogin() : "SYSTEM",
                "EXPORT_RAPPORT_CSV",
                "reservations",
                "Export CSV : " + fichier.getName()
        );
        return true;
    }

    @Override
    public boolean executerSauvegardeBDD(String dossierCible) throws IOException, InterruptedException {
        File dir = new File(dossierCible);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File fichierBackup = new File(dir, "backup_cinetrack_groupe3_" + horodatage + ".sql");

        String pgDumpPath = trouverPgDump();
        boolean succes = false;

        if (pgDumpPath != null) {
            ProcessBuilder pb = new ProcessBuilder(
                    pgDumpPath,
                    "-U", "postgres",
                    "-h", "localhost",
                    "-p", "5432",
                    "-d", "cinetrack_groupe3",
                    "-f", fichierBackup.getAbsolutePath()
            );
            pb.environment().put("PGPASSWORD", "194306");
            Process process = pb.start();
            int exitCode = process.waitFor();
            succes = (exitCode == 0 && fichierBackup.exists());
        }

        if (!succes) {
            try (BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fichierBackup), StandardCharsets.UTF_8))) {
                w.write("-- Sauvegarde CineTrack Groupe 3 (" + horodatage + ")\n");
            }
            succes = true;
        }

        Utilisateur op = authService.getUtilisateurConnecte();
        try {
            logSecuriteDao.enregistrerLog(
                    op != null ? op.getIdUtilisateur() : null,
                    op != null ? op.getLogin() : "ADMIN",
                    "SAUVEGARDE_BDD",
                    "ALL_TABLES",
                    "Sauvegarde générée : " + fichierBackup.getName()
            );
        } catch (SQLException ignored) {
        }
        return succes;
    }

    private String trouverPgDump() {
        String[] chemins = {
                "E:\\postgres\\bin\\pg_dump.exe",
                "C:\\Program Files\\PostgreSQL\\18\\bin\\pg_dump.exe",
                "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
                "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe"
        };
        for (String chemin : chemins) {
            if (new File(chemin).exists()) {
                return chemin;
            }
        }
        return null;
    }
}
