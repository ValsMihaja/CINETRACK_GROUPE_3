package model;

/**
 * Entité Modèle représentant la table PostgreSQL 'films'.
 */
public class Film {

    private int idFilm;
    private String titre;
    private String genre;
    private int duree;
    private double note;
    private String synopsis;

    public Film() {
    }

    public Film(int idFilm, String titre, String genre, int duree, double note, String synopsis) {
        this.idFilm = idFilm;
        this.titre = titre;
        this.genre = genre;
        this.duree = duree;
        this.note = note;
        this.synopsis = synopsis;
    }

    public Film(String titre, String genre, int duree, double note, String synopsis) {
        this(0, titre, genre, duree, note, synopsis);
    }

    public int getIdFilm() {
        return idFilm;
    }

    public void setIdFilm(int idFilm) {
        this.idFilm = idFilm;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public double getNote() {
        return note;
    }

    public void setNote(double note) {
        this.note = note;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    @Override
    public String toString() {
        return titre + " (" + duree + " min - " + genre + ")";
    }
}
