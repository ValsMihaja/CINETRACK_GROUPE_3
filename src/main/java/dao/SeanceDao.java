package dao;

import model.Seance;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SeanceDao {

    List<Seance> findAll() throws SQLException;

    Seance findById(int id) throws SQLException;

    boolean insert(Seance seance) throws SQLException;

    boolean update(Seance seance) throws SQLException;

    boolean delete(int id) throws SQLException;

    List<Seance> rechercherParCriteres(LocalDate date, Integer idSalle, Integer idFilm) throws SQLException;

    boolean verifierConflitHoraire(int idSalle, LocalDate dateSeance, LocalTime heureDebut, int dureeMinutes,
                                   int idSeanceExclue) throws SQLException;
}
