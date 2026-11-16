package exceptions;

/**
 * Exception levée lorsqu'un compte utilisateur est bloqué après 3 tentatives échouées.
 */
public class CompteBloqueException extends CineTrackException {

    public CompteBloqueException(String login) {
        super(
                "ERR_COMPTE_BLOQUE",
                "Le compte '" + login + "' est verrouillé suite à 3 tentatives échouées. Contactez l'administrateur."
        );
    }
}
