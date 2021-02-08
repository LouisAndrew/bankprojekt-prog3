package verarbeitung;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.beans.PropertyChangeSupport;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse zum Testen der Klasse Girokonto.
 */
public class GirokontoTest {
    
    Kunde ich = new Kunde("Dorothea", "Hubrich", "zuhause", LocalDate.parse("1976-07-13"));
    Kunde andere = new Kunde("John", "Doe", "HTW", LocalDate.parse("2020-10-10"));

    long kontoNummer = 12345678;
    double dispo = 10;

    long kontoNummer2 = 2343414;
    double dispo2 = 12;

    Girokonto k = new Girokonto(ich, kontoNummer, dispo);
    Girokonto k2 = new Girokonto(andere, kontoNummer2, dispo2);

    PropertyChangeSupport propMock = Mockito.mock(PropertyChangeSupport.class);

    /**
     * Hilfsmethode zur Einstellung der PropertyChangeSupport des Kontos.
     */
    public void setupPropChangeSupport() {
        k.setProp(propMock);
        k2.setProp(propMock);
    }

    /**
     * Hilfsfunktion zur Einzahlung am Anfang eines Tests, sodass das Konto beim Testen Saldo hat.
     * @param konto wird von 10 Euro eingezahlt
     */
    public void anfangsEinzahlung(Konto konto) {
        konto.einzahlen(10);
    }

    /**
     * Testen beim erfolgreichen Anlegen eines Kontos
     */
    @Test
    public void kontoAnlegenErfolgreich() {
        Konto sample = new Girokonto();
    }

    /**
     * Testen beim fehlerhaften Anlegen eines Kontos
     * -> Der Parameter Inhaber auf null gesetzt, um einen Fehler beim anlegen einzuführen
     */
    @Test
    public void kontoAnlegenFehler() {
        try {
            Konto sample = new Girokonto((Kunde) null, kontoNummer, dispo);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true); // Wenn ein Exception geworfen wird, ist dann die Implementation richtig.
        }
    }

    /**
     * Testen beim Anlegen eines Kontos, wobei der Inhaber zur Verfügung gestellt.
     * 
     * Testen ob der zurückgelieferte Inhaber des Kontos richtig ist.
     */
    @Test
    public void kontoMitInhaberAnlegen() {
        Kunde kunde = k.getInhaber();

        // 0 -> ist gleich, anders als 0 -> unterschiedlich
        assertEquals(k.getInhaber(), ich);
    }

    /**
     * Testen die Währung des Kontos. (Am Anfang sollte Euro sein, und bei Veränderung(-en) sollte auch funktioniert.
     */
    @Test
    public void kontoWaehrungTest() {
        assertSame(k.getWaehrung(), Waehrung.EUR);
    }

    /**
     * Testen das Abheben eines Kontos
     */
    @Test
    public void abhebenTest() throws GesperrtException {
        setupPropChangeSupport(); // PropertyChangeSupport ist in die Konten eingestellt
        anfangsEinzahlung(k); // anfangs 10Eur

        // heb 5 Eur ab
        boolean abgehoben;
        abgehoben = k.abheben(5.0); // 5Eur

        Mockito.verify(propMock).firePropertyChange("Kontostand", 10.0, 5.0);
        assertTrue(abgehoben); // Erfolgreiches Abheben.

        abgehoben = k.abheben(20.0); // sollte nicht erfolgreich sein

        assertFalse(abgehoben);
        assertEquals(5.0, k.getKontostand());
        // Mockito.verify(propMock, Mockito.atMost(1)).firePropertyChange(ArgumentMatchers.matches("Kontostand"), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble());
    }

    /**
     * Testen das Abheben eines Kontos, das mit anderer Währung geführt wird.
      */
    @Test
    public void abhebenAndererWaehrungTest() throws GesperrtException {
        anfangsEinzahlung(k); // 10Eur.

        Waehrung bgn = Waehrung.BGN;
        Waehrung km = Waehrung.KM;

        double funfEurInBgn = bgn.euroInWaehrungUmrechnen(5); // 5 Eur in BGN.
        double zwanzigEurInKm = km.euroInWaehrungUmrechnen(20); // 20 Eur in KM

        boolean abgehoben;

        abgehoben = k.abheben(funfEurInBgn, bgn);

        assertTrue(abgehoben); // Erfolgreiches Abheben

        abgehoben = k.abheben(zwanzigEurInKm, km);

        assertFalse(abgehoben);
        assertEquals(k.getKontostand(), 5);
    }

    /**
     * Testen das Abheben, wenn das Konto gesperrt ist
     */
    @Test
    public void abhebenGesperrtTest() {
        anfangsEinzahlung(k);
        k.sperren(); // Konto ist ab jetzt gesperrt
        setupPropChangeSupport(); // PropertyChangeSupport ist in die Konten eingestellt

        try {
            k.abheben(10);
            fail(); // Wenn dieser Zeil dran kommt, ist ein Fehler aufgetreten
        } catch (GesperrtException e) {
            assertTrue(true);
        }

        Mockito.verifyNoInteractions(propMock);
    }

    /**
     * Testen das erfolgreiche Einzahlen ins Konto
     */
    @Test
    public void einzahlungTest() {
        setupPropChangeSupport(); // PropertyChangeSupport ist in die Konten eingestellt
        double betrag = 10.5;

        k.einzahlen(betrag);
        assertEquals(k.getKontostand(), betrag);
        Mockito.verify(propMock).firePropertyChange("Kontostand", 0.0, betrag);
    }

    /**
     * Testen ds nicht erfolgreivhe Einzahlen ins Konto
     */
    @Test
    public void einzahlungFehlerTest() {

        setupPropChangeSupport();

        try {
            double betrag = -10.5;

            k.einzahlen(betrag); // Negativer Betrag, sollte nicht erfolgreich
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true); // Exception geworfen: Läuft die Methode ganz gut.
        }

        Mockito.verifyNoInteractions(propMock);
    }

    /**
     * Testen das Einzahlen ins Konto mit einer anderer Währung
     */
    @Test
    public void einzahlungAndererWaehrungTest() {
        double betrag = 10.5;
        double betragInBGN = Waehrung.BGN.euroInWaehrungUmrechnen(betrag); // einzuzahlender Betrag in anderer Währung umrechnen

        k.einzahlen(betragInBGN, Waehrung.BGN);
        assertEquals(k.getKontostand(), betrag); // Kontostand sollte in EUR sein.
    }

    /**
     * Testen die Methode zum Währungwechseln
     */
    @Test
    public void waehrungWechselTest() {
        anfangsEinzahlung(k); // 10 Eur
        setupPropChangeSupport();
        Waehrung w = Waehrung.BGN; // wird in die Währung gewechselt

        k.waehrungswechsel(w);

        assertEquals(k.getAktuelleWaehrung(), w);
        assertEquals(k.getKontostand(), w.euroInWaehrungUmrechnen(10));
        Mockito.verify(propMock).firePropertyChange("Waehrung", Waehrung.EUR, w);
    }

    /**
     * Testen die erfolgreiche Überweisungmethoden
     */
    @Test
    public void ueberweisungAbsendenTest() throws GesperrtException {
        anfangsEinzahlung(k);
        boolean erfolgreich;

        erfolgreich = k.ueberweisungAbsenden(10, andere.getName(), k2.getKontonummer(), 1, "Testen");

        assertEquals(k.getKontostand(), 0);
        assertTrue(erfolgreich);

        erfolgreich = k.ueberweisungAbsenden(20, andere.getName(),  k2.getKontonummer(), 1, "Testen");

        assertFalse(erfolgreich);
    }

    /**
     * Testen fehlerhafte Überweisung (Konto gesperrt)
     */
    @Test
    public void ueberweisungAbsendenGesperrtTest() {
        anfangsEinzahlung(k);
        k.sperren();

        try {
            k.ueberweisungAbsenden(10, andere.getName(), k2.getKontonummer(), 1, "Testen");
            fail();
        } catch (GesperrtException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen Überweisungempfangen
     */
    @Test
    public void ueberweisungEmpfangenTest() {

        k.ueberweisungEmpfangen(10, andere.getName(), k2.getKontonummer(), 1, "Testen");

        assertEquals(k.getKontostand(), 10);
    }
}