package model;

/**
 * Entité Modèle représentant la table PostgreSQL 'tarifs'.
 * Catégories : PLEIN_TARIF, TARIF_REDUIT, TARIF_ETUDIANT, ABONNEMENT.
 */
public class Tarif {

    private int idTarif;
    private String categorie;
    private String libelle;
    private double prixUnitaire;
    private String description;
    private boolean actif;

    public Tarif() {
    }

    public Tarif(int idTarif, String categorie, String libelle, double prixUnitaire, String description, boolean actif) {
        this.idTarif = idTarif;
        this.categorie = categorie;
        this.libelle = libelle;
        this.prixUnitaire = prixUnitaire;
        this.description = description;
        this.actif = actif;
    }

    public double calculerMontant(int nbPlaces) {
        return Math.round(prixUnitaire * nbPlaces * 100.0) / 100.0;
    }

    public int getIdTarif() {
        return idTarif;
    }

    public void setIdTarif(int idTarif) {
        this.idTarif = idTarif;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public String getPrixTexte() {
        return String.format("%.2f Rs", prixUnitaire);
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return libelle + " (" + String.format("%.2f Rs", prixUnitaire) + ")";
    }
}
