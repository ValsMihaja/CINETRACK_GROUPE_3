package service;

import dao.FilmDao;
import dao.LogSecuriteDao;
import dao.SalleDao;
import dao.SeanceDao;
import exceptions.CineTrackException;
import exceptions.ConflitHoraireException;
import impl.FilmDaoImpl;
import impl.LogSecuriteDaoImpl;
import impl.SalleDaoImpl;
import impl.SeanceDaoImpl;
import model.Film;
import model.Salle;
import model.Seance;
import model.Utilisateur;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ProgrammationService {

    private final FilmDao filmDao = new FilmDaoImpl();
    private final SalleDao salleDao = new SalleDaoImpl();
    private final SeanceDao seanceDao = new SeanceDaoImpl();
    private final LogSecuriteDao logSecuriteDao = new LogSecuriteDaoImpl();

    public List<Film> listerFilms() throws SQLException {
        return filmDao.findAll();
    }

    public List<Film> rechercherFilms(String motCleTitre, String genre) throws SQLException {
        return filmDao.rechercher(motCleTitre, genre);
    }

    public List<String> listerGenres() throws SQLException {
        return filmDao.findAllGenres();
    }

    public void ajouterFilm(String titre, String genre, int duree, double note, String synopsis)
            throws CineTrackException, SQLException {
        validerDonneesFilm(titre, genre, duree, note);
        Film film = new Film(titre.trim(), genre.trim(), duree, note, synopsis != null ? synopsis.trim() : "");
        filmDao.insert(film);
    }

    public void modifierFilm(int idFilm, String titre, String genre, int duree, double note, String synopsis)
            throws CineTrackException, SQLException {
        if (idFilm <= 0) {
            throw new CineTrackException("Veuillez sélectionner un film à modifier.");
        }
        validerDonneesFilm(titre, genre, duree, note);
        Film film = new Film(idFilm, titre.trim(), genre.trim(), duree, note, synopsis != null ? synopsis.trim() : "");
        filmDao.update(film);
    }

    public void supprimerFilm(int idFilm, Utilisateur operateur) throws CineTrackException, SQLException {
        if (operateur == null || !operateur.estManagerOuPlus()) {
            throw new CineTrackException("Accès refusé : seul un Manager ou un Administrateur peut supprimer un film.");
        }
        Film existant = filmDao.findById(idFilm);
        if (existant == null) {
            throw new CineTrackException("Film introuvable (ID #" + idFilm + ").");
        }
        filmDao.delete(idFilm);
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

    public List<Salle> listerSalles() throws SQLException {
        return salleDao.findAll();
    }

    public void ajouterSalle(String nom, int capacite, String equipement)
            throws CineTrackException, SQLException {
        validerDonneesSalle(nom, capacite);
        if (salleDao.findByNom(nom.trim()) != null) {
            throw new CineTrackException("Une salle portant le nom '" + nom.trim() + "' existe déjà.");
        }
        Salle salle = new Salle(nom.trim(), capacite, equipement != null ? equipement.trim() : "Standard");
        salleDao.insert(salle);
    }

    public void modifierSalle(int idSalle, String nom, int capacite, String equipement)
            throws CineTrackException, SQLException {
        if (idSalle <= 0) {
            throw new CineTrackException("Veuillez sélectionner une salle à modifier.");
        }
        validerDonneesSalle(nom, capacite);
        Salle salle = new Salle(idSalle, nom.trim(), capacite, equipement != null ? equipement.trim() : "Standard");
        salleDao.update(salle);
    }

    public void supprimerSalle(int idSalle, Utilisateur operateur) throws CineTrackException, SQLException {
        if (operateur == null || !operateur.estManagerOuPlus()) {
            throw new CineTrackException("Accès refusé : seul un Manager ou un Administrateur peut supprimer une salle.");
        }
        Salle existante = salleDao.findById(idSalle);
        if (existante == null) {
            throw new CineTrackException("Salle introuvable (ID #" + idSalle + ").");
        }
        salleDao.delete(idSalle);
    }

    private void validerDonneesSalle(String nom, int capacite) throws CineTrackException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new CineTrackException("Le nom de la salle est obligatoire.");
        }
        if (capacite <= 0 || capacite > 2000) {
            throw new CineTrackException("La capacité de la salle doit être comprise entre 1 et 2000 places.");
        }
    }

    public List<Seance> listerSeances() throws SQLException {
        return seanceDao.findAll();
    }

    public List<Seance> rechercherSeances(LocalDate date, Integer idSalle, Integer idFilm) throws SQLException {
        return seanceDao.rechercherParCriteres(date, idSalle, idFilm);
    }

    public void programmerSeance(int idFilm, int idSalle, LocalDate dateSeance, LocalTime heureDebut)
            throws CineTrackException, SQLException {
        if (dateSeance == null || heureDebut == null) {
            throw new CineTrackException("La date et l'heure de la séance sont obligatoires.");
        }

        Film film = filmDao.findById(idFilm);
        if (film == null) {
            throw new CineTrackException("Veuillez sélectionner un film valide.");
        }

        Salle salle = salleDao.findById(idSalle);
        if (salle == null) {
            throw new CineTrackException("Veuillez sélectionner une salle valide.");
        }

        boolean conflit = seanceDao.verifierConflitHoraire(
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
            seanceDao.insert(nouvelle);
        } catch (SQLException e) {
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
        Film film = filmDao.findById(idFilm);
        Salle salle = salleDao.findById(idSalle);
        if (film == null || salle == null) {
            throw new CineTrackException("Film ou salle invalide.");
        }
        if (placesDispo < 0 || placesDispo > salle.getCapacite()) {
            throw new CineTrackException(
                    "Le nombre de places disponibles doit être compris entre 0 et " + salle.getCapacite() + "."
            );
        }

        boolean conflit = seanceDao.verifierConflitHoraire(
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
        seanceDao.update(maj);
    }

    public void supprimerSeance(int idSeance, Utilisateur operateur) throws CineTrackException, SQLException {
        if (operateur == null || !operateur.estManagerOuPlus()) {
            throw new CineTrackException("Accès refusé : seul un Manager ou un Administrateur peut supprimer une séance.");
        }
        seanceDao.delete(idSeance);
        logSecuriteDao.enregistrerLog(
                operateur.getIdUtilisateur(),
                operateur.getLogin(),
                "SUPPRESSION_SEANCE",
                "seances",
                "Suppression de la séance #" + idSeance
        );
    }
}
