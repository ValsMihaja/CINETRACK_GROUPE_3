package dao;

import model.Reservation;

import java.sql.SQLException;
import java.util.List;

public interface ReservationDao {

    List<Reservation> findAll() throws SQLException;

    Reservation findById(int id) throws SQLException;

    Reservation findByNumeroBillet(String numeroBillet) throws SQLException;

    String creerReservationSP(int idSeance, int idClient, int idTarif, int idUtilisateur, int nbPlaces)
            throws SQLException;

    boolean annulerReservationSP(int idReservation, int idUtilisateur) throws SQLException;

    boolean insert(Reservation reservation) throws SQLException;

    boolean update(Reservation reservation) throws SQLException;

    boolean delete(int id) throws SQLException;
}
