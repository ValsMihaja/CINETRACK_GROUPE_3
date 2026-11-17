package service;

import dao.ClientDao;
import dao.ReservationDao;
import dao.SeanceDao;
import dao.TarifDao;
import exceptions.CineTrackException;
import exceptions.PlaceIndisponibleException;
import impl.ClientDaoImpl;
import impl.ReservationDaoImpl;
import impl.SeanceDaoImpl;
import impl.TarifDaoImpl;
import model.Client;
import model.Reservation;
import model.Seance;
import model.Tarif;
import model.Utilisateur;

import java.sql.SQLException;
import java.util.List;

public class BilletterieService {

    private final ReservationDao reservationDao = new ReservationDaoImpl();
    private final SeanceDao seanceDao = new SeanceDaoImpl();
    private final ClientDao clientDao = new ClientDaoImpl();
    private final TarifDao tarifDao = new TarifDaoImpl();

    public double calculerMontantTotal(Tarif tarif, int nbPlaces) throws CineTrackException {
        if (tarif == null) {
            throw new CineTrackException("Veuillez sélectionner un tarif.");
        }
        if (nbPlaces <= 0) {
            throw new CineTrackException("Le nombre de places doit être supérieur à 0.");
        }
        return tarif.calculerMontant(nbPlaces);
    }

    public Reservation reserverPlaces(int idSeance, int idClient, int idTarif, int nbPlaces, Utilisateur caissier)
            throws CineTrackException, SQLException {
        if (nbPlaces <= 0) {
            throw new CineTrackException("Le nombre de places doit être supérieur à 0.");
        }

        Seance seance = seanceDao.findById(idSeance);
        if (seance == null) {
            throw new CineTrackException("Séance introuvable.");
        }
        if (seance.getPlacesDispo() < nbPlaces) {
            throw new PlaceIndisponibleException(nbPlaces, seance.getPlacesDispo());
        }

        Client client = clientDao.findById(idClient);
        if (client == null) {
            throw new CineTrackException("Client introuvable.");
        }

        Tarif tarif = tarifDao.findById(idTarif);
        if (tarif == null || !tarif.isActif()) {
            throw new CineTrackException("Tarif invalide.");
        }

        int idUtilisateur = caissier != null ? caissier.getIdUtilisateur() : 1;

        try {
            String numeroBillet = reservationDao.creerReservationSP(
                    idSeance, idClient, idTarif, idUtilisateur, nbPlaces
            );
            Reservation creee = reservationDao.findByNumeroBillet(numeroBillet);
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

    public void annulerReservation(int idReservation, Utilisateur operateur)
            throws CineTrackException, SQLException {
        Reservation existante = reservationDao.findById(idReservation);
        if (existante == null) {
            throw new CineTrackException("Réservation introuvable.");
        }
        if ("ANNULEE".equalsIgnoreCase(existante.getStatut())) {
            throw new CineTrackException("Ce billet est déjà annulé.");
        }

        int idUtilisateur = operateur != null ? operateur.getIdUtilisateur() : 1;
        reservationDao.annulerReservationSP(idReservation, idUtilisateur);
    }

    public List<Reservation> listerReservations() throws SQLException {
        return reservationDao.findAll();
    }

    public List<Seance> listerSeances() throws SQLException {
        return seanceDao.findAll();
    }

    public List<Client> listerClients() throws SQLException {
        return clientDao.findAll();
    }

    public List<Client> rechercherClients(String motCle) throws SQLException {
        return clientDao.rechercher(motCle);
    }

    public void ajouterClient(String nom, String prenom, String email, String telephone)
            throws CineTrackException, SQLException {
        if (nom == null || nom.trim().isEmpty() || prenom == null || prenom.trim().isEmpty()) {
            throw new CineTrackException("Le nom et le prénom sont obligatoires.");
        }
        String emailPropre = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
        if (emailPropre != null && !emailPropre.contains("@")) {
            throw new CineTrackException("Adresse email invalide.");
        }
        Client nouveau = new Client(
                nom.trim(),
                prenom.trim(),
                emailPropre,
                telephone != null ? telephone.trim() : ""
        );
        clientDao.insert(nouveau);
    }

    public List<Tarif> listerTarifs() throws SQLException {
        return tarifDao.findAll();
    }

    public List<Tarif> listerTarifsActifs() throws SQLException {
        return tarifDao.findActifs();
    }

    public void modifierTarif(int idTarif, String libelle, double nouveauPrix, String description,
                              boolean actif, Utilisateur operateur)
            throws CineTrackException, SQLException {
        if (operateur != null && !operateur.estManagerOuPlus()) {
            throw new CineTrackException("Accès refusé.");
        }
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new CineTrackException("Le libellé est obligatoire.");
        }
        if (nouveauPrix < 0) {
            throw new CineTrackException("Le prix ne peut pas être négatif.");
        }
        Tarif existant = tarifDao.findById(idTarif);
        if (existant == null) {
            throw new CineTrackException("Tarif introuvable.");
        }
        existant.setLibelle(libelle.trim());
        existant.setPrixUnitaire(nouveauPrix);
        existant.setDescription(description != null ? description.trim() : "");
        existant.setActif(actif);
        tarifDao.update(existant);
    }
}
