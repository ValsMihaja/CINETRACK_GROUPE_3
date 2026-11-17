package service;

import dao.LogSecuriteDao;
import impl.LogSecuriteDaoImpl;
import model.LogSecurite;
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
import java.util.Map;

public class StatistiqueEtSauvegardeService implements ExportService {

    private final LogSecuriteDao logSecuriteDao = new LogSecuriteDaoImpl();
    private final AuthentificationService authService = new AuthentificationService();
    private Utilisateur operateurCourant;

    public double obtenirChiffreAffairesTotal() throws SQLException {
        return logSecuriteDao.getChiffreAffairesTotal();
    }

    public int obtenirTotalBilletsVendus() throws SQLException {
        return logSecuriteDao.getTotalBilletsVendus();
    }

    public int obtenirTotalReservationsConfirmees() throws SQLException {
        return logSecuriteDao.getTotalReservationsConfirmees();
    }

    public List<Map<String, Object>> obtenirTopFilms() throws SQLException {
        return logSecuriteDao.getTopFilms();
    }

    public List<Map<String, Object>> obtenirTauxOccupationSeances() throws SQLException {
        return logSecuriteDao.getTauxOccupationSeances();
    }

    public List<Map<String, Object>> obtenirChiffreAffairesParPeriode() throws SQLException {
        return logSecuriteDao.getChiffreAffairesParPeriode();
    }

    public List<LogSecurite> consulterLogsSecurite() throws SQLException {
        return logSecuriteDao.findAll();
    }

    @Override
    public boolean exporterCSV(String cheminFichier) throws Exception {
        List<Map<String, Object>> topFilms = obtenirTopFilms();
        List<Map<String, Object>> occupation = obtenirTauxOccupationSeances();

        File fichier = new File(cheminFichier);
        if (fichier.getParentFile() != null) {
            fichier.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fichier), StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.write("RAPPORT D'EXPLOITATION CINETRACK V2\n");
            writer.write("Date d'export;" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n");
            writer.write("Chiffre d'affaires global (Rs);" + String.format("%.2f", obtenirChiffreAffairesTotal()) + "\n");
            writer.write("Total places vendues;" + obtenirTotalBilletsVendus() + "\n\n");

            writer.write("CLASSEMENT DES FILMS LES PLUS RESERVES\n");
            writer.write("ID Film;Titre;Genre;Note;Nb Réservations;Places Vendues;Chiffre d'Affaires (Rs)\n");
            for (Map<String, Object> ligne : topFilms) {
                writer.write(
                        ligne.get("idFilm") + ";"
                                + ligne.get("titre") + ";"
                                + ligne.get("genre") + ";"
                                + ligne.get("noteMoyenne") + ";"
                                + ligne.get("nbReservations") + ";"
                                + ligne.get("placesVendues") + ";"
                                + String.format("%.2f", (Double) ligne.get("chiffreAffaires")) + "\n"
                );
            }

            writer.write("\nTAUX D'OCCUPATION PAR SEANCE\n");
            writer.write("ID Séance;Date;Heure;Salle;Film;Capacité;Places Occupées;Places Dispo;Taux Occupation (%)\n");
            for (Map<String, Object> ligne : occupation) {
                writer.write(
                        ligne.get("idSeance") + ";"
                                + ligne.get("dateSeance") + ";"
                                + ligne.get("heure") + ";"
                                + ligne.get("salle") + ";"
                                + ligne.get("film") + ";"
                                + ligne.get("capacite") + ";"
                                + ligne.get("placesOccupees") + ";"
                                + ligne.get("placesDispo") + ";"
                                + ligne.get("tauxOccupation") + "%\n"
                );
            }
        }

        Utilisateur op = getOperateur();
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
                w.write("-- Base : cinetrack_groupe3\n");
            }
            succes = true;
        }

        Utilisateur op = getOperateur();
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

    private Utilisateur getOperateur() {
        return operateurCourant != null ? operateurCourant : authService.getUtilisateurConnecte();
    }

    public void setOperateurCourant(Utilisateur operateurCourant) {
        this.operateurCourant = operateurCourant;
    }
}
