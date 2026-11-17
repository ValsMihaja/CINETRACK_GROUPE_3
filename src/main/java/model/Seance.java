package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entité Modèle représentant la table PostgreSQL 'seances'.
 */
public class Seance {

    private int idSeance;
    private int idFilm;
    private String titreFilm;
    private int dureeFilm;
    private int idSalle;
    private String nomSalle;
    private int capaciteSalle;
    private LocalDate dateSeance;
    private LocalTime heure;
    private int placesDispo;

    public Seance() {
    }

    public Seance(int idSeance, int idFilm, String titreFilm, int dureeFilm,
                  int idSalle, String nomSalle, int capaciteSalle,
                  LocalDate dateSeance, LocalTime heure, int placesDispo) {
        this.idSeance = idSeance;
        this.idFilm = idFilm;
        this.titreFilm = titreFilm;
        this.dureeFilm = dureeFilm;
        this.idSalle = idSalle;
        this.nomSalle = nomSalle;
        this.capaciteSalle = capaciteSalle;
        this.dateSeance = dateSeance;
        this.heure = heure;
        this.placesDispo = placesDispo;
    }

    public Seance(int idFilm, int idSalle, LocalDate dateSeance, LocalTime heure, int placesDispo) {
        this(0, idFilm, "", 0, idSalle, "", 0, dateSeance, heure, placesDispo);
    }

    public LocalTime getHeureFin() {
        return heure != null && dureeFilm > 0 ? heure.plusMinutes(dureeFilm) : heure;
    }

    public String getDateTexte() {
        return dateSeance != null ? dateSeance.toString() : "";
    }

    public String getHeureTexte() {
        return heure != null ? heure + " - " + getHeureFin() : "";
    }

    public int getPlacesOccupees() {
        return Math.max(0, capaciteSalle - placesDispo);
    }

    public double getTauxOccupation() {
        if (capaciteSalle <= 0) {
            return 0.0;
        }
        return Math.round((getPlacesOccupees() * 10000.0) / capaciteSalle) / 100.0;
    }

    public int getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(int idSeance) {
        this.idSeance = idSeance;
    }

    public int getIdFilm() {
        return idFilm;
    }

    public void setIdFilm(int idFilm) {
        this.idFilm = idFilm;
    }

    public String getTitreFilm() {
        return titreFilm;
    }

    public void setTitreFilm(String titreFilm) {
        this.titreFilm = titreFilm;
    }

    public int getDureeFilm() {
        return dureeFilm;
    }

    public void setDureeFilm(int dureeFilm) {
        this.dureeFilm = dureeFilm;
    }

    public int getIdSalle() {
        return idSalle;
    }

    public void setIdSalle(int idSalle) {
        this.idSalle = idSalle;
    }

    public String getNomSalle() {
        return nomSalle;
    }

    public void setNomSalle(String nomSalle) {
        this.nomSalle = nomSalle;
    }

    public int getCapaciteSalle() {
        return capaciteSalle;
    }

    public void setCapaciteSalle(int capaciteSalle) {
        this.capaciteSalle = capaciteSalle;
    }

    public LocalDate getDateSeance() {
        return dateSeance;
    }

    public void setDateSeance(LocalDate dateSeance) {
        this.dateSeance = dateSeance;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public int getPlacesDispo() {
        return placesDispo;
    }

    public void setPlacesDispo(int placesDispo) {
        this.placesDispo = placesDispo;
    }

    @Override
    public String toString() {
        return "#" + idSeance + " | " + dateSeance + " " + heure + " - " + titreFilm
                + " (" + nomSalle + " | " + placesDispo + " pl. dispo)";
    }
}
