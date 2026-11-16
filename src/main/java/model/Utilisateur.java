package model;

import java.time.LocalDateTime;

/**
 * Entité Modèle représentant la table PostgreSQL 'utilisateurs'.
 * Rôles supportés : ADMIN, MANAGER, CAISSIER.
 */
public class Utilisateur {

    private int idUtilisateur;
    private String login;
    private String motDePasse;
    private String nom;
    private String prenom;
    private String role; // ADMIN, MANAGER, CAISSIER
    private int tentativesEchouees;
    private boolean actif;
    private LocalDateTime dateDerniereConnexion;

    public Utilisateur() {
    }

    public Utilisateur(int idUtilisateur, String login, String motDePasse, String nom, String prenom,
                       String role, int tentativesEchouees, boolean actif, LocalDateTime dateDerniereConnexion) {
        this.idUtilisateur = idUtilisateur;
        this.login = login;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.tentativesEchouees = tentativesEchouees;
        this.actif = actif;
        this.dateDerniereConnexion = dateDerniereConnexion;
    }

    public Utilisateur(String login, String motDePasse, String nom, String prenom, String role) {
        this(0, login, motDePasse, nom, prenom, role, 0, true, null);
    }

    public boolean estAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean estManagerOuPlus() {
        return "ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role);
    }

    public boolean estCompteVerrouille() {
        return !actif || tentativesEchouees >= 3;
    }

    public String getNomComplet() {
        return ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();
    }

    public String getStatutTexte() {
        return estCompteVerrouille() ? "BLOQUÉ (" + tentativesEchouees + "/3)" : "ACTIF";
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getTentativesEchouees() {
        return tentativesEchouees;
    }

    public void setTentativesEchouees(int tentativesEchouees) {
        this.tentativesEchouees = tentativesEchouees;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateDerniereConnexion() {
        return dateDerniereConnexion;
    }

    public void setDateDerniereConnexion(LocalDateTime dateDerniereConnexion) {
        this.dateDerniereConnexion = dateDerniereConnexion;
    }

    @Override
    public String toString() {
        return getNomComplet() + " (" + login + " - " + role + ")";
    }
}
