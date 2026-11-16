package model;

import java.time.LocalDateTime;

/**
 * Entité Modèle représentant la table PostgreSQL 'logs_securite' (CC4 - Cybersécurité).
 */
public class LogSecurite {

    private int idLog;
    private Integer idUtilisateur;
    private String utilisateurLogin;
    private String action;
    private String tableConcernee;
    private String donneesConcernees;
    private LocalDateTime dateHeure;

    public LogSecurite() {
    }

    public LogSecurite(int idLog, Integer idUtilisateur, String utilisateurLogin,
                       String action, String tableConcernee, String donneesConcernees,
                       LocalDateTime dateHeure) {
        this.idLog = idLog;
        this.idUtilisateur = idUtilisateur;
        this.utilisateurLogin = utilisateurLogin;
        this.action = action;
        this.tableConcernee = tableConcernee;
        this.donneesConcernees = donneesConcernees;
        this.dateHeure = dateHeure;
    }

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getUtilisateurLogin() {
        return utilisateurLogin;
    }

    public void setUtilisateurLogin(String utilisateurLogin) {
        this.utilisateurLogin = utilisateurLogin;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTableConcernee() {
        return tableConcernee;
    }

    public void setTableConcernee(String tableConcernee) {
        this.tableConcernee = tableConcernee;
    }

    public String getDonneesConcernees() {
        return donneesConcernees;
    }

    public void setDonneesConcernees(String donneesConcernees) {
        this.donneesConcernees = donneesConcernees;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }
}
