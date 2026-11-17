package service;

public interface ExportService {

    boolean exporterCSV(String cheminFichier) throws Exception;

    boolean executerSauvegardeBDD(String dossierCible) throws Exception;
}
