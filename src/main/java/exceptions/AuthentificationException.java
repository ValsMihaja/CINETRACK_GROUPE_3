package exceptions;

/**
 * Exception levée en cas d'identifiants invalides lors de la connexion.
 */
public class AuthentificationException extends CineTrackException {

    public AuthentificationException(String message) {
        super("ERR_AUTHENTIFICATION", message);
    }
}
