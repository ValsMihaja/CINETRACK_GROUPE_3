package model;

import java.time.LocalDate;

/**
 * Entité Modèle représentant la table PostgreSQL 'clients'.
 */
public class Client {

    private int idClient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate dateInscription;

    public Client() {
    }

    public Client(int idClient, String nom, String prenom, String email, String telephone, LocalDate dateInscription) {
        this.idClient = idClient;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.dateInscription = dateInscription;
    }

    public Client(String nom, String prenom, String email, String telephone) {
        this(0, nom, prenom, email, telephone, LocalDate.now());
    }

    public String getNomComplet() {
        return ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    @Override
    public String toString() {
        return getNomComplet() + (email != null && !email.isEmpty() ? " <" + email + ">" : "");
    }
}
