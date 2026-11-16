package exceptions;

/**
 * Exception levée lorsqu'une séance programmée chevauche une autre séance
 * dans la même salle de projection.
 */
public class ConflitHoraireException extends CineTrackException {

    public ConflitHoraireException(String message) {
        super("ERR_CONFLIT_HORAIRE", message);
    }
}
