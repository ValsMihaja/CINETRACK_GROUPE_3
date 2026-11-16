package dao;

import model.Seance;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Interface DAO pour la programmation et la recherche des séances.
 */
public interface ISeanceDAO extends IDAO<Seance> {

    List<Seance> rechercherParCriteres(LocalDate date, Integer idSalle, Integer idFilm) throws SQLException;

    boolean verifierConflitHoraire(int idSalle, LocalDate dateSeance, LocalTime heureDebut,
                                   int dureeMinutes, int idSeanceExclue) throws SQLException;
}
