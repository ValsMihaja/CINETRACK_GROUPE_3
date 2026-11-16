package service;

import exceptions.CineTrackException;
import exceptions.ConflitHoraireException;
import exceptions.PlaceIndisponibleException;
import main.AppContext;
import model.Tarif;
import model.Utilisateur;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires JUnit 5 vérifiant l'injection de dépendances et les règles métier sensibles :
 * 1. Calcul automatique du tarif
 * 2. Détection de conflit d'horaires
 * 3. Vérification de la disponibilité des places
 * 4. Authentification des rôles internes (Caissier, Manager, Administrateur)
 */
public class CineTrackArchitectureTest {

    private final AppContext context = AppContext.getInstance();

    @Test
    void testCalculAutomatiqueTarif() throws CineTrackException {
        BilletterieService service = context.getBilletterieService();
        Tarif tarifEtudiant = new Tarif(3, "TARIF_ETUDIANT", "Tarif Étudiant", 200.0, "Carte étudiante", true);
        double montant = service.calculerMontantTotal(tarifEtudiant, 4);
        assertEquals(800.0, montant, 0.001);
    }

    @Test
    void testDetectionConflitHoraire() {
        ProgrammationService service = context.getProgrammationService();
        assertThrows(ConflitHoraireException.class, () ->
                service.programmerSeance(1, 1, LocalDate.of(2026, 11, 6), LocalTime.of(10, 30))
        );
    }

    @Test
    void testVerificationDisponibilitePlaces() throws Exception {
        BilletterieService service = context.getBilletterieService();
        Utilisateur caissier = context.getAuthentificationService().authentifier("caissier1", "caissier123");
        assertThrows(PlaceIndisponibleException.class, () ->
                service.reserverPlaces(1, 1, 1, 500, caissier)
        );
    }

    @Test
    void testAuthentificationTroisRoles() throws Exception {
        AuthentificationService auth = context.getAuthentificationService();
        Utilisateur caissier = auth.authentifier("caissier1", "caissier123");
        assertEquals("CAISSIER", caissier.getRole());
        assertFalse(caissier.estManagerOuPlus());

        Utilisateur manager = auth.authentifier("manager1", "manager123");
        assertEquals("MANAGER", manager.getRole());
        assertTrue(manager.estManagerOuPlus());
        assertFalse(manager.estAdmin());

        Utilisateur admin = auth.authentifier("admin", "admin123");
        assertEquals("ADMIN", admin.getRole());
        assertTrue(admin.estAdmin());
    }
}
