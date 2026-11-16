package exceptions;

/**
 * Exception métier racine de l'application CineTrack V2.
 * Permet de gérer proprement toutes les erreurs fonctionnelles sans interrompre l'application.
 */
public class CineTrackException extends Exception {

    private final String codeErreur;

    public CineTrackException(String message) {
        super(message);
        this.codeErreur = "ERR_CINETRACK";
    }

    public CineTrackException(String codeErreur, String message) {
        super(message);
        this.codeErreur = codeErreur;
    }

    public CineTrackException(String message, Throwable cause) {
        super(message, cause);
        this.codeErreur = "ERR_CINETRACK";
    }

    public String getCodeErreur() {
        return codeErreur;
    }
}
