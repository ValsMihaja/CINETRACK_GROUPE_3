package dao;

import model.Reservation;

import java.sql.SQLException;

/**
 * Interface DAO pour la billetterie et la gestion des réservations.
 */
public interface IReservationDAO extends IDAO<Reservation> {

    /**
     * Appelle la procédure stockée PostgreSQL sp_creer_reservation
     * et retourne le numéro de billet unique généré.
     */
    String creerReservationSP(int idSeance, int idClient, int idTarif, int idUtilisateur, int nbPlaces) throws SQLException;

    /**
     * Appelle la procédure stockée PostgreSQL sp_annuler_reservation
     * qui remet à jour les places disponibles via le trigger SQL.
     */
    boolean annulerReservationSP(int idReservation, int idUtilisateur) throws SQLException;

    Reservation findByNumeroBillet(String numeroBillet) throws SQLException;
}
