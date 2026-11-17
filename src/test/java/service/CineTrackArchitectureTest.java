package service;

import exceptions.CineTrackException;
import exceptions.ConflitHoraireException;
import exceptions.PlaceIndisponibleException;
import model.Tarif;
import model.Utilisateur;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class CineTrackArchitectureTest {

    @Test
    void testCalculAutomatiqueTarif() throws CineTrackException {
        BilletterieService service = new BilletterieService();
        Tarif tarifEtudiant = new Tarif(3, "TARIF_ETUDIANT", "Tarif Étudiant", 200.0, "Carte étudiante", true);
        double montant = service.calculerMontantTotal(tarifEtudiant, 4);
        assertEquals(800.0, montant, 0.001);
    }

    @Test
    void testDetectionConflitHoraire() {
        ProgrammationService service = new ProgrammationService();
        assertThrows(ConflitHoraireException.class, () ->
                service.programmerSeance(1, 1, LocalDate.of(2026, 11, 6), LocalTime.of(10, 30))
        );
    }

    @Test
    void testVerificationDisponibilitePlaces() throws Exception {
        BilletterieService billetterieService = new BilletterieService();
        AuthentificationService authService = new AuthentificationService();
        Utilisateur caissier = authService.authentifier("caissier1", "caissier123");
        assertThrows(PlaceIndisponibleException.class, () ->
                billetterieService.reserverPlaces(1, 1, 1, 500, caissier)
        );
    }

    @Test
    void testAuthentificationTroisRoles() throws Exception {
        AuthentificationService auth = new AuthentificationService();
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
