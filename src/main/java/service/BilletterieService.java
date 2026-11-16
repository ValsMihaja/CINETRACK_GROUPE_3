package service;

import dao.IClientDAO;
import dao.ILogSecuriteDAO;
import dao.IReservationDAO;
import dao.ISeanceDAO;
import dao.ITarifDAO;
import dao.impl.ClientDAOImpl;
import dao.impl.LogSecuriteDAOImpl;
import dao.impl.ReservationDAOImpl;
import dao.impl.SeanceDAOImpl;
import dao.impl.TarifDAOImpl;
import exceptions.CineTrackException;
import exceptions.PlaceIndisponibleException;
import model.Client;
import model.Reservation;
import model.Seance;
import model.Tarif;
import model.Utilisateur;

import java.sql.SQLException;
import java.util.List;

/**
 * ====================================================================
 * COUCHE : SERVICE (Billetterie, Clients, Tarifs & Réservations)
 * ====================================================================
 * RÔLE :
 * - Calcule automatiquement le montant total selon le tarif et le nombre de places.
 * - Vérifie la disponibilité des places avant toute réservation.
 * - Délègue la création et l'annulation à la procédure stockée PostgreSQL
 *   et aux triggers associés.
 * - Gère le fichier des clients et les grilles tarifaires.
 * - Utilise l'injection de dépendances par constructeur sur les interfaces DAO.
 * - Ne contient AUCUN import JavaFX ni AUCUNE requête SQL directe.
 */
public class BilletterieService {

    private final IReservationDAO reservationDAO;
    private final ISeanceDAO seanceDAO;
    private final IClientDAO clientDAO;
    private final ITarifDAO tarifDAO;
    private final ILogSecuriteDAO logSecuriteDAO;

    /**
     * Constructeur avec injection de dépendances (inversion de contrôle).
     */
    public BilletterieService(IReservationDAO reservationDAO, ISeanceDAO seanceDAO,
                              IClientDAO clientDAO, ITarifDAO tarifDAO,
                              ILogSecuriteDAO logSecuriteDAO) {
        this.reservationDAO = reservationDAO;
        this.seanceDAO = seanceDAO;
        this.clientDAO = clientDAO;
        this.tarifDAO = tarifDAO;
        this.logSecuriteDAO = logSecuriteDAO;
    }

    /**
     * Constructeur par défaut utilisant les implémentations JDBC PostgreSQL.
     */
    public BilletterieService() {
        this(
                new ReservationDAOImpl(),
                new SeanceDAOImpl(),
                new ClientDAOImpl(),
                new TarifDAOImpl(),
                new LogSecuriteDAOImpl()
        );
    }

    // ====================================================================
    // 1. CALCUL TARIFAIRE & BILLETTERIE (RÉSERVATION / ANNULATION)
    // ====================================================================

    /**
     * Calcule automatiquement le montant total d'une réservation.
     */
    public double calculerMontantTotal(Tarif tarif, int nbPlaces) throws CineTrackException {
        if (tarif == null) {
            throw new CineTrackException("Veuillez sélectionner une catégorie tarifaire.");
        }
        if (nbPlaces <= 0) {
            throw new CineTrackException("Le nombre de places doit être supérieur à 0.");
        }
        return tarif.calculerMontant(nbPlaces);
    }

    /**
     * Crée une réservation, vérifie la disponibilité des places, génère un billet unique
     * et met à jour les places disponibles (via sp_creer_reservation + trigger SQL).
     */
    public Reservation reserverPlaces(int idSeance, int idClient, int idTarif, int nbPlaces, Utilisateur caissier)
            throws CineTrackException, SQLException {
        if (nbPlaces <= 0) {
            throw new CineTrackException("Le nombre de places à réserver doit être supérieur à 0.");
        }

        Seance seance = seanceDAO.findById(idSeance);
        if (seance == null) {
            throw new CineTrackException("Veuillez sélectionner une séance valide.");
        }

        // Règle métier : vérification de la capacité restante
        if (seance.getPlacesDispo() < nbPlaces) {
            throw new PlaceIndisponibleException(nbPlaces, seance.getPlacesDispo());
        }

        Client client = clientDAO.findById(idClient);
        if (client == null) {
            throw new CineTrackException("Veuillez sélectionner un client valide.");
        }

        Tarif tarif = tarifDAO.findById(idTarif);
        if (tarif == null || !tarif.isActif()) {
            throw new CineTrackException("Veuillez sélectionner un tarif actif valide.");
        }

        int idUtilisateur = caissier != null ? caissier.getIdUtilisateur() : 1;

        try {
            String numeroBillet = reservationDAO.creerReservationSP(
                    idSeance, idClient, idTarif, idUtilisateur, nbPlaces
            );
            Reservation creee = reservationDAO.findByNumeroBillet(numeroBillet);
            if (creee != null) {
                return creee;
            }
            return new Reservation(
                    0, numeroBillet, idSeance, seance.toString(),
                    idClient, client.getNomComplet(), idTarif, tarif.getLibelle(),
                    idUtilisateur, nbPlaces, calculerMontantTotal(tarif, nbPlaces),
                    null, "CONFIRMEE"
            );
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("PLACES_INDISPONIBLES")) {
                throw new PlaceIndisponibleException(e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Annule une réservation confirmée et remet à jour les places disponibles.
     */
    public void annulerReservation(int idReservation, Utilisateur operateur)
            throws CineTrackException, SQLException {
        Reservation existante = reservationDAO.findById(idReservation);
        if (existante == null) {
            throw new CineTrackException("Réservation introuvable (#" + idReservation + ").");
        }
        if ("ANNULEE".equalsIgnoreCase(existante.getStatut())) {
            throw new CineTrackException("Le billet " + existante.getNumeroBillet() + " est déjà annulé.");
        }

        int idUtilisateur = operateur != null ? operateur.getIdUtilisateur() : 1;
        reservationDAO.annulerReservationSP(idReservation, idUtilisateur);
    }

    public List<Reservation> listerReservations() throws SQLException {
        return reservationDAO.findAll();
    }

    // ====================================================================
    // 2. GESTION DES FICHES CLIENTS
    // ====================================================================

    public List<Client> listerClients() throws SQLException {
        return clientDAO.findAll();
    }

    public List<Client> rechercherClients(String motCle) throws SQLException {
        return clientDAO.rechercher(motCle);
    }

    public void ajouterClient(String nom, String prenom, String email, String telephone)
            throws CineTrackException, SQLException {
        if (nom == null || nom.trim().isEmpty() || prenom == null || prenom.trim().isEmpty()) {
            throw new CineTrackException("Le nom et le prénom du client sont obligatoires.");
        }
        String emailPropre = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
        if (emailPropre != null && !emailPropre.contains("@")) {
            throw new CineTrackException("L'adresse email du client est invalide.");
        }
        Client nouveau = new Client(
                nom.trim(),
                prenom.trim(),
                emailPropre,
                telephone != null ? telephone.trim() : ""
        );
        clientDAO.insert(nouveau);
    }

    // ====================================================================
    // 3. GESTION DES GRILLES TARIFAIRES
    // ====================================================================

    public List<Tarif> listerTarifs() throws SQLException {
        return tarifDAO.findAll();
    }

    public List<Tarif> listerTarifsActifs() throws SQLException {
        return tarifDAO.findActifs();
    }

    public void modifierTarif(int idTarif, String libelle, double nouveauPrix, String description,
                              boolean actif, Utilisateur operateur)
            throws CineTrackException, SQLException {
        if (operateur != null && !operateur.estManagerOuPlus()) {
            throw new CineTrackException("Accès refusé : seul un Manager ou un Administrateur peut modifier les tarifs.");
        }
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new CineTrackException("Le libellé du tarif est obligatoire.");
        }
        if (nouveauPrix < 0) {
            throw new CineTrackException("Le prix unitaire ne peut pas être négatif.");
        }
        Tarif existant = tarifDAO.findById(idTarif);
        if (existant == null) {
            throw new CineTrackException("Tarif introuvable.");
        }
        existant.setLibelle(libelle.trim());
        existant.setPrixUnitaire(nouveauPrix);
        existant.setDescription(description != null ? description.trim() : "");
        existant.setActif(actif);
        tarifDAO.update(existant);
        // Note : Le trigger PostgreSQL trg_historique_tarifs journalise automatiquement la modification dans logs_securite
    }
}
