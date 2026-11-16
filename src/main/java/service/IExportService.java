package service;

import java.io.IOException;

/**
 * Interface de service pour l'export de rapports (CSV) et la sauvegarde PostgreSQL.
 */
public interface IExportService {

    boolean exporterCSV(String cheminFichier) throws Exception;

    boolean executerSauvegardeBDD(String dossierCible) throws IOException, InterruptedException;
}
