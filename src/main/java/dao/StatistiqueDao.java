package dao;

import model.StatFilm;
import model.StatOccupation;

import java.sql.SQLException;
import java.util.List;

public interface StatistiqueDao {

    double getChiffreAffairesTotal() throws SQLException;

    int getTotalBilletsVendus() throws SQLException;

    int getTotalReservationsConfirmees() throws SQLException;

    List<StatFilm> getTopFilms() throws SQLException;

    List<StatOccupation> getTauxOccupationSeances() throws SQLException;
}
