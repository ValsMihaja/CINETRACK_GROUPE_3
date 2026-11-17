package model;

public class StatOccupation {

    private int idSeance;
    private String dateSeance;
    private String heure;
    private String salle;
    private String film;
    private int capacite;
    private int placesOccupees;
    private int placesDispo;
    private double tauxOccupation;

    public StatOccupation(int idSeance, String dateSeance, String heure, String salle, String film,
                          int capacite, int placesOccupees, int placesDispo, double tauxOccupation) {
        this.idSeance = idSeance;
        this.dateSeance = dateSeance;
        this.heure = heure;
        this.salle = salle;
        this.film = film;
        this.capacite = capacite;
        this.placesOccupees = placesOccupees;
        this.placesDispo = placesDispo;
        this.tauxOccupation = tauxOccupation;
    }

    public int getIdSeance() {
        return idSeance;
    }

    public String getDateSeance() {
        return dateSeance;
    }

    public String getHeure() {
        return heure;
    }

    public String getSalle() {
        return salle;
    }

    public String getFilm() {
        return film;
    }

    public int getCapacite() {
        return capacite;
    }

    public int getPlacesOccupees() {
        return placesOccupees;
    }

    public int getPlacesDispo() {
        return placesDispo;
    }

    public double getTauxOccupation() {
        return tauxOccupation;
    }

    public String getOccupationTexte() {
        return placesOccupees + " / " + capacite;
    }

    public String getTauxTexte() {
        return tauxOccupation + " %";
    }
}
