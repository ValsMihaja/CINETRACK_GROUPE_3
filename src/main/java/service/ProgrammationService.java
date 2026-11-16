package service;

import dao.IFilmDAO;
import dao.ILogSecuriteDAO;
import dao.ISalleDAO;
import dao.ISeanceDAO;
import dao.impl.FilmDAOImpl;
import dao.impl.LogSecuriteDAOImpl;
import dao.impl.SalleDAOImpl;
import dao.impl.SeanceDAOImpl;
import exceptions.CineTrackException;
import exceptions.ConflitHoraireException;
import model.Film;
import model.Salle;
import model.Seance;
import model.Utilisateur;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * ====================================================================
 * COUCHE : SERVICE (Catalogue de Films, Salles & Programmation des Séances)
 * ====================================================================
 * RÔLE :
 * - Applique toutes les règles métier sur les Films, les Salles et les Séances.
 * - Détecte automatiquement les conflits d'horaires dans une même salle en tenant
 *   compte de la durée du film.
 * - Vérifie les droits d'accès (Manager / Administrateur) avant suppression.
 * - Utilise l'injection de dépendances par constructeur sur les interfaces DAO.
 * - Ne contient AUCUN import JavaFX ni AUCUNE requête SQL directe.
 */
public class ProgrammationService {

    private final IFilmDAO filmDAO;
    private final ISalleDAO salleDAO;
    private final ISeanceDAO seanceDAO;
    private final ILogSecuriteDAO logSecuriteDAO;

    /**
     * Constructeur avec injection de dépendances (inversion de contrôle).
     */
    public ProgrammationService(IFilmDAO filmDAO, ISalleDAO salleDAO,
                                ISeanceDAO seanceDAO, ILogSecuriteDAO logSecuriteDAO) {
        this.filmDAO = filmDAO;
        this.salleDAO = salleDAO;
        this.seanceDAO = seanceDAO;
        this.logSecuriteDAO = logSecuriteDAO;
    }

    /**
     * Constructeur par défaut utilisant les implémentations JDBC PostgreSQL.
     */
    public ProgrammationService() {
        this(new FilmDAOImpl(), new SalleDAOImpl(), new SeanceDAOImpl(), new LogSecuriteDAOImpl());
    }

    // ====================================================================
    // 1. GESTION DES FILMS
    // ====================================================================

    public List<Film> listerFilms() throws SQLException {
        return filmDAO.findAll();
    }

    public List<Film> rechercherFilms(String motCleTitre, String genre) throws SQLException {
        return filmDAO.rechercher(motCleTitre, genre);
    }

    public List<String> listerGenres() throws SQLException {
        return filmDAO.findAllGenres();
    }

    public void ajouterFilm(String titre, String genre, int duree, double note, String synopsis)
            throws CineTrackException, SQLException {
        validerDonneesFilm(titre, genre, duree, note);
        Film film = new Film(titre.trim(), genre.trim(), duree, note, synopsis != null ? synopsis.trim() : "");
        filmDAO.insert(film);
    }

    public void modifierFilm(int idFilm, String titre, String genre, int duree, double note, String synopsis)
            throws CineTrackException, SQLException {
        if (idFilm <= 0) {
            throw new CineTrackException("Veuillez sélectionner un film à modifier.");
        }
        validerDonneesFilm(titre, genre, duree, note);
        Film film = new Film(idFilm, titre.trim(), genre.trim(), duree, note, synopsis != null ? synopsis.trim() : "");
        filmDAO.update(film);
    }

    public void supprimerFilm(int idFilm, Utilisateur operateur) throws CineTrackException, SQLException {
        if (operateur == null || !operateur.estManagerOuPlus()) {
            throw new CineTrackException("Accès refusé : seul un Manager ou un Administrateur peut supprimer un film.");
        }
        Film existant = filmDAO.findById(idFilm);
        if (existant == null) {
            throw new CineTrackException("Film introuvable (ID #" + idFilm + ").");
        }
        filmDAO.delete(idFilm);
    }

    private void validerDonneesFilm(String titre, String genre, int duree, double note) throws CineTrackException {
        if (titre == null || titre.trim().isEmpty()) {
            throw new CineTrackException("Le titre du film ne peut pas être vide.");
        }
        if (genre == null || genre.trim().isEmpty()) {
            throw new CineTrackException("Le genre du film est obligatoire.");
        }
        if (duree <= 0 || duree > 600) {
            throw new CineTrackException("La durée du film doit être comprise entre 1 et 600 minutes.");
        }
        if (note < 0.0 || note > 10.0) {
            throw new CineTrackException("La note du film doit être comprise entre 0.0 et 10.0.");
        }
    }

    // ====================================================================
    // 2. GESTION DES SALLES
    // ====================================================================

    public List<Salle> listerSalles() throws SQLException {
        return salleDAO.findAll();
    }

    public void ajouterSalle(String nom, int capacite, String equipement)
            throws CineTrackException, SQLException {
        validerDonneesSalle(nom, capacite);
        if (salleDAO.findByNom(nom.trim()) != null) {
            throw new CineTrackException("Une salle portant le nom '" + nom.trim() + "' existe déjà.");
        }
        Salle salle = new Salle(nom.trim(), capacite, equipement != null ? equipement.trim() : "Standard");
        salleDAO.insert(salle);
    }

    public void modifierSalle(int idSalle, String nom, int capacite, String equipement)
            throws CineTrackException, SQLException {
        if (idSalle <= 0) {
            throw new CineTrackException("Veuillez sélectionner une salle à modifier.");
        }
        validerDonneesSalle(nom, capacite);
        Salle salle = new Salle(idSalle, nom.trim(), capacite, equipement != null ? equipement.trim() : "Standard");
        salleDAO.update(salle);
    }

    public void supprimerSalle(int idSalle, Utilisateur operateur) throws CineTrackException, SQLException {
        if (operateur == null || !operateur.estManagerOuPlus()) {
            throw new CineTrackException("Accès refusé : seul un Manager ou un Administrateur peut supprimer une salle.");
        }
        Salle existante = salleDAO.findById(idSalle);
        if (existante == null) {
            throw new CineTrackException("Salle introuvable (ID #" + idSalle + ").");
        }
        salleDAO.delete(idSalle);
    }

    private void validerDonneesSalle(String nom, int capacite) throws CineTrackException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new CineTrackException("Le nom de la salle est obligatoire.");
        }
        if (capacite <= 0 || capacite > 2000) {
            throw new CineTrackException("La capacité de la salle doit être comprise entre 1 et 2000 places.");
        }
    }

    // ====================================================================
    // 3. PROGRAMMATION DES SÉANCES & DÉTECTION DES CONFLITS D'HORAIRES
    // ====================================================================

    public List<Seance> listerSeances() throws SQLException {
        return seanceDAO.findAll();
    }

    public List<Seance> rechercherSeances(LocalDate date, Integer idSalle, Integer idFilm) throws SQLException {
        return seanceDAO.rechercherParCriteres(date, idSalle, idFilm);
    }

    public void programmerSeance(int idFilm, int idSalle, LocalDate dateSeance, LocalTime heureDebut)
            throws CineTrackException, SQLException {
        if (dateSeance == null || heureDebut == null) {
            throw new CineTrackException("La date et l'heure de la séance sont obligatoires.");
        }

        Film film = filmDAO.findById(idFilm);
        if (film == null) {
            throw new CineTrackException("Veuillez sélectionner un film valide.");
        }

        Salle salle = salleDAO.findById(idSalle);
        if (salle == null) {
            throw new CineTrackException("Veuillez sélectionner une salle valide.");
        }

        // Vérification applicative du conflit d'horaire avant l'insertion
        boolean conflit = seanceDAO.verifierConflitHoraire(
                idSalle, dateSeance, heureDebut, film.getDuree(), -1
        );
        if (conflit) {
            LocalTime heureFin = heureDebut.plusMinutes(film.getDuree());
            throw new ConflitHoraireException(
                    "Conflit d'horaire détecté dans '" + salle.getNom() + "' le " + dateSeance
                            + " sur le créneau [" + heureDebut + " - " + heureFin + "]."
            );
        }

        Seance nouvelle = new Seance(idFilm, idSalle, dateSeance, heureDebut, salle.getCapacite());
        try {
            seanceDAO.insert(nouvelle);
        } catch (SQLException e) {
            // Interception du trigger PostgreSQL trg_verif_conflit_seance si déclenché
            if (e.getMessage() != null && e.getMessage().contains("CONFLIT_HORAIRE")) {
                throw new ConflitHoraireException(e.getMessage());
            }
            throw e;
        }
    }

    public void modifierSeance(int idSeance, int idFilm, int idSalle, LocalDate dateSeance,
                               LocalTime heureDebut, int placesDispo)
            throws CineTrackException, SQLException {
        if (idSeance <= 0) {
            throw new CineTrackException("Veuillez sélectionner une séance à modifier.");
        }
        Film film = filmDAO.findById(idFilm);
        Salle salle = salleDAO.findById(idSalle);
        if (film == null || salle == null) {
            throw new CineTrackException("Film ou salle invalide.");
        }
        if (placesDispo < 0 || placesDispo > salle.getCapacite()) {
            throw new CineTrackException(
                    "Le nombre de places disponibles doit être compris entre 0 et " + salle.getCapacite() + "."
            );
        }

        boolean conflit = seanceDAO.verifierConflitHoraire(
                idSalle, dateSeance, heureDebut, film.getDuree(), idSeance
        );
        if (conflit) {
            LocalTime heureFin = heureDebut.plusMinutes(film.getDuree());
            throw new ConflitHoraireException(
                    "Conflit d'horaire détecté dans '" + salle.getNom() + "' le " + dateSeance
                            + " sur le créneau [" + heureDebut + " - " + heureFin + "]."
            );
        }

        Seance maj = new Seance(idSeance, idFilm, film.getTitre(), film.getDuree(),
                idSalle, salle.getNom(), salle.getCapacite(), dateSeance, heureDebut, placesDispo);
        seanceDAO.update(maj);
    }

    public void supprimerSeance(int idSeance, Utilisateur operateur) throws CineTrackException, SQLException {
        if (operateur == null || !operateur.estManagerOuPlus()) {
            throw new CineTrackException("Accès refusé : seul un Manager ou un Administrateur peut supprimer une séance.");
        }
        seanceDAO.delete(idSeance);
        logSecuriteDAO.enregistrerLog(
                operateur.getIdUtilisateur(),
                operateur.getLogin(),
                "SUPPRESSION_SEANCE",
                "seances",
                "Suppression de la séance #" + idSeance
        );
    }
}
