package bank;

import org.junit.jupiter.api.Test;
import verarbeitung.Girokonto;
import verarbeitung.Konto;
import verarbeitung.Kunde;
import verarbeitung.Sparbuch;

import java.time.LocalDate;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class BankTest {

    long blz = 12;
    Bank b = new Bank(blz);

    Kunde kunde1 = new Kunde("John", "Doe", "HTW", LocalDate.now());

    /**
     * Testen das Anlegen einer Bank, wenn die angegebene BLZ negativ ist
     */
    @Test
    public void bankAnlegenFehlerTest() {
        try {
            Bank bf = new Bank(-1);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen das Anlegen eines Girokontos UND die zurückgegebene Kontonummer.
     */
    @Test
    public void girokontoErstellenTest() {
        long nummer = b.girokontoErstellen(kunde1);
        assertEquals(nummer, 1);
    }

    /**
     * Testen das Anlegen eines Girokontos, wenn der Inhaber null ist.
     */
    @Test
    public void girokontoErstellenFehleTest() {
        try {
            b.girokontoErstellen(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen das Anlegen eines Sparbuchs UND die zurückgegebene Kontonummer
     */
    @Test
    public void sparbuchErstellenTest() {
        long nummer = b.sparbuchErstellen(kunde1);
        assertEquals(nummer, 1);
    }

    /**
     * Testen das Anlegen eines Sparbuchs, wenn der Inhaber null ist
     */
    @Test
    public void sparbuchErstellenFehlerTest() {
        try {
            b.sparbuchErstellen(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen die zurückgegebene Kontonummern, wenn einige Konten angelegt werden
     */
    @Test
    public void gegebeneKontonummerTest() {
        long nummer1 = b.girokontoErstellen(kunde1);
        long nummer2 = b.sparbuchErstellen(kunde1);
        long nummer3 = b.girokontoErstellen(new Kunde());

        assertEquals(nummer1, 1);
        assertEquals(nummer2, 2);
        assertEquals(nummer3, 3);
    }

    /**
     * Testen die Geldabhebung eines Kontos
     */
    @Test
    public void geldAbhebenTest() {
        long nummer = b.girokontoErstellen(kunde1);

        try {

            // max Abhebung = 20EUR.
            boolean erfolgreich = b.geldAbheben(nummer, 20);
            assertTrue(erfolgreich);

            erfolgreich = b.geldAbheben(nummer, 50); // Groesser als Dispo
            assertFalse(erfolgreich);
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Testen die Geldabhebung eines nicht existierenden Kontos
     */
    @Test
    public void geldAbhebenFehlerTest() {
        try {
            b.geldAbheben(1, 20); // Konto mit der Nr. 1 ecistiert nicht.
            fail();
        } catch (KontoNichtExistiertException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen die Geldeinzahlung eines Kontos
     */
    @Test
    public void geldEinzahlenTest() {

        long nummer = b.girokontoErstellen(kunde1);

        try {
            assertEquals(b.getKontostand(nummer), 0);

            double neuerBetrag = 35.0;
            b.geldEinzahlen(nummer, neuerBetrag);

            assertEquals(b.getKontostand(nummer), neuerBetrag);
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Testen die Geldeinzahlung eines nicht existierednen Kontos
     */
    @Test
    public void geldEinzahlenFehlerTest() {
        try {
            b.geldEinzahlen(2, 20);
            fail();
        } catch (KontoNichtExistiertException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen das Löschen eines Kontos.
     */
    @Test
    public void kontoLoeschenTest() {
        long nummer = b.girokontoErstellen(kunde1);
        boolean erfolgreich = b.kontoLoeschen(nummer);

        try {
            assertTrue(erfolgreich);

            // versucht mal Geld aus diesem Konto abzuheben (wenn die Abhebung nicht geklappt, das Löschen ist erfolgreich)
            b.geldEinzahlen(nummer, 20);
            fail();
        } catch (KontoNichtExistiertException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen das Löschen eines nicht existierenden Kontos
     */
    @Test
    public void kontoLoeschenFehlerTest() {
        boolean erfolgreich = b.kontoLoeschen(1);
        assertFalse(erfolgreich);
    }

    /**
     * Testen die Überprüfung vom Kontostand eines Kontos
     */
    @Test
    public void getKontostandTest() throws KontoNichtExistiertException {
        long nummer = b.girokontoErstellen(kunde1);

        assertEquals(b.getKontostand(nummer), 0);
        double betrag = 10;

        b.geldEinzahlen(nummer, betrag);
        assertEquals(b.getKontostand(nummer), betrag);
    }

    /**
     * Testen die Überprüfung vom Kontostands eines nicht existierenden Kontos
     */
    @Test
    public void getKontostandFehlerTest() {
        try {
            b.getKontostand(1);
            fail();
        } catch (KontoNichtExistiertException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen der erfolgreichen Überweisung
     */
    @Test
    public void ueberweisungTest() throws KontoNichtExistiertException {
        long giro = b.girokontoErstellen(kunde1); // überweisungsfähig
        long sparbuch = b.sparbuchErstellen(kunde1); // überweisungsunfähig

        boolean erfolgreich = b.geldUeberweisen(giro, sparbuch, 20, "Hello, World!");
        assertTrue(erfolgreich);

        // Testen die Überweisung, wenn der Betrag groesser als Dispo ist.
        erfolgreich = b.geldUeberweisen(giro, sparbuch, 50, "Hello, World!");
    }

    /**
     * Testen die Überweisung mit fehlerhaften Parametern.
     */
    public void ueberweisungFehlerhaftTest() throws KontoNichtExistiertException {
        long giro = b.girokontoErstellen(kunde1); // überweisungsfähig
        long sparbuch = b.sparbuchErstellen(kunde1); // überweisungsunfähig

        boolean erfolgreich = b.geldUeberweisen(giro, sparbuch, -10, null);
        assertFalse(erfolgreich);

        try {
            b.geldUeberweisen(12, sparbuch, 10, "");
            fail();
        } catch (KontoNichtExistiertException e) {
            assertTrue(true);
        }
    }

    /**
     * Testen die Überweisung von Überweisungsunfähigem
     */
    public void ueberweisungVonUnfaehigTest() throws KontoNichtExistiertException {
        long sparbuch1 = b.sparbuchErstellen(kunde1); // überweisungsunfähig
        long sparbuch2 = b.sparbuchErstellen(new Kunde()); // auch unfaehig.

        boolean erfolgreich = b.geldUeberweisen(sparbuch1, sparbuch2, 5, "");
        assertFalse(erfolgreich);
    }

    /**
     * Testen die Überweisung nach Überweisungsfähigem
     */
    public void ueberweisungNachFaehigTest() throws KontoNichtExistiertException {
        long giro1 = b.girokontoErstellen(kunde1); // überweisungsfähig
        long giro2 = b.girokontoErstellen(new Kunde()); // auch ueberweisungsfaehig

        boolean erfolgreich = b.geldUeberweisen(giro1, giro2, 5, "");
        assertFalse(erfolgreich);
    }

    /**
     * Testen die Überweisung von Überweisungsunfähigem nach Überweisungsfähigem
     */
    public void ueberweisungVonUnfaehigNachFaehigTest() throws KontoNichtExistiertException {
        long giro = b.girokontoErstellen(kunde1); // überweisungsfähig
        long sparbuch = b.sparbuchErstellen(kunde1); // überweisungsunfähig

        boolean erfolgreich = b.geldUeberweisen(sparbuch, giro, 5, "");
        assertFalse(erfolgreich);
    }
}
