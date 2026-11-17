package model;

public class StatFilm {

    private int idFilm;
    private String titre;
    private String genre;
    private double noteMoyenne;
    private int nbReservations;
    private int placesVendues;
    private double chiffreAffaires;

    public StatFilm(int idFilm, String titre, String genre, double noteMoyenne,
                    int nbReservations, int placesVendues, double chiffreAffaires) {
        this.idFilm = idFilm;
        this.titre = titre;
        this.genre = genre;
        this.noteMoyenne = noteMoyenne;
        this.nbReservations = nbReservations;
        this.placesVendues = placesVendues;
        this.chiffreAffaires = chiffreAffaires;
    }

    public int getIdFilm() {
        return idFilm;
    }

    public String getTitre() {
        return titre;
    }

    public String getGenre() {
        return genre;
    }

    public double getNoteMoyenne() {
        return noteMoyenne;
    }

    public String getNoteTexte() {
        return noteMoyenne + " / 10";
    }

    public int getNbReservations() {
        return nbReservations;
    }

    public int getPlacesVendues() {
        return placesVendues;
    }

    public double getChiffreAffaires() {
        return chiffreAffaires;
    }

    public String getChiffreAffairesTexte() {
        return String.format("%.2f Rs", chiffreAffaires);
    }
}
