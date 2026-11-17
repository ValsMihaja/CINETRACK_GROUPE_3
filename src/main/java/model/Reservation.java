package model;

import java.time.LocalDateTime;

/**
 * Entité Modèle représentant la table PostgreSQL 'reservations'.
 */
public class Reservation {

    private int idReservation;
    private String numeroBillet;
    private int idSeance;
    private String infoSeance;
    private int idClient;
    private String nomCompletClient;
    private int idTarif;
    private String libelleTarif;
    private Integer idUtilisateur;
    private int nbPlaces;
    private double montantTotal;
    private LocalDateTime dateReservation;
    private String statut; // CONFIRMEE, ANNULEE

    public Reservation() {
    }

    public Reservation(int idReservation, String numeroBillet, int idSeance, String infoSeance,
                       int idClient, String nomCompletClient, int idTarif, String libelleTarif,
                       Integer idUtilisateur, int nbPlaces, double montantTotal,
                       LocalDateTime dateReservation, String statut) {
        this.idReservation = idReservation;
        this.numeroBillet = numeroBillet;
        this.idSeance = idSeance;
        this.infoSeance = infoSeance;
        this.idClient = idClient;
        this.nomCompletClient = nomCompletClient;
        this.idTarif = idTarif;
        this.libelleTarif = libelleTarif;
        this.idUtilisateur = idUtilisateur;
        this.nbPlaces = nbPlaces;
        this.montantTotal = montantTotal;
        this.dateReservation = dateReservation;
        this.statut = statut;
    }

    public String genererRecapitulatifBillet() {
        return "========================================\n"
                + "         CINÉMA LE LUMIÈRE - BILLET     \n"
                + "========================================\n"
                + " N° Billet : " + numeroBillet + "\n"
                + " Client    : " + nomCompletClient + "\n"
                + " Séance    : " + infoSeance + "\n"
                + " Tarif     : " + libelleTarif + "\n"
                + " Places    : " + nbPlaces + "\n"
                + " Total     : " + String.format("%.2f Rs", montantTotal) + "\n"
                + " Statut    : " + statut + "\n"
                + "========================================";
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public String getNumeroBillet() {
        return numeroBillet;
    }

    public void setNumeroBillet(String numeroBillet) {
        this.numeroBillet = numeroBillet;
    }

    public int getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(int idSeance) {
        this.idSeance = idSeance;
    }

    public String getInfoSeance() {
        return infoSeance;
    }

    public void setInfoSeance(String infoSeance) {
        this.infoSeance = infoSeance;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public String getNomCompletClient() {
        return nomCompletClient;
    }

    public void setNomCompletClient(String nomCompletClient) {
        this.nomCompletClient = nomCompletClient;
    }

    public int getIdTarif() {
        return idTarif;
    }

    public void setIdTarif(int idTarif) {
        this.idTarif = idTarif;
    }

    public String getLibelleTarif() {
        return libelleTarif;
    }

    public void setLibelleTarif(String libelleTarif) {
        this.libelleTarif = libelleTarif;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public String getMontantTexte() {
        return String.format("%.2f Rs", montantTotal);
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
