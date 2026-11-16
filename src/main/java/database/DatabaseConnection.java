package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ====================================================================
 * COUCHE : DATABASE (Gestionnaire technique de connexion JDBC PostgreSQL)
 * ====================================================================
 * RÔLE :
 * Fournit une méthode centralisée pour obtenir une connexion active vers PostgreSQL.
 * - Aucune requête SQL métier ne se trouve ici (les requêtes sont dans dao.impl).
 * - Aucun code JavaFX ne se trouve ici.
 */
public class DatabaseConnection {

    private static final String URL = System.getProperty(
            "cinetrack.db.url",
            "jdbc:postgresql://localhost:5432/projetcinetrackv2"
    );
    private static final String USER = System.getProperty("cinetrack.db.user", "postgres");
    private static final String PASSWORD = System.getProperty("cinetrack.db.password", "194306");

    private DatabaseConnection() {
        // Constructeur privé (classe utilitaire)
    }

    /**
     * Retourne une nouvelle connexion JDBC ouverte vers PostgreSQL.
     * À utiliser systématiquement dans un bloc try-with-resources.
     *
     * @return Connection active vers la base PostgreSQL
     * @throws SQLException en cas d'erreur de connexion
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Teste la disponibilité de la connexion PostgreSQL.
     *
     * @return true si la connexion est valide, false sinon
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }
}
