package exceptions;

/**
 * Exception levée lorsque le nombre de places demandées pour une réservation
 * dépasse le nombre de places disponibles pour la séance.
 */
public class PlaceIndisponibleException extends CineTrackException {

    private final int placesDemandees;
    private final int placesDisponibles;

    public PlaceIndisponibleException(int placesDemandees, int placesDisponibles) {
        super(
                "ERR_PLACES_INDISPONIBLES",
                "Capacité insuffisante : " + placesDemandees + " place(s) demandée(s), mais seulement "
                        + placesDisponibles + " place(s) disponible(s)."
        );
        this.placesDemandees = placesDemandees;
        this.placesDisponibles = placesDisponibles;
    }

    public PlaceIndisponibleException(String message) {
        super("ERR_PLACES_INDISPONIBLES", message);
        this.placesDemandees = 0;
        this.placesDisponibles = 0;
    }

    public int getPlacesDemandees() {
        return placesDemandees;
    }

    public int getPlacesDisponibles() {
        return placesDisponibles;
    }
}
