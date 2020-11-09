package bank;

import org.junit.jupiter.api.Test;
import verarbeitung.Kunde;

import java.time.LocalDate;
import java.util.List;

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
            // leer
        }
    }

    /**
     * Testen das Anlegen eines Girokontos UND die zurückgegebene Kontonummer.
     */
    @Test
    public void girokontoErstellenTest() throws KontoNichtExistiertException {
        long nummer = b.girokontoErstellen(kunde1);
        List<Long> konten = b.getAlleKontonummern();

        assertTrue(konten.contains(nummer));
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
            // leer
        }
    }

    /**
     * Testen das Anlegen eines Sparbuchs UND die zurückgegebene Kontonummer
     */
    @Test
    public void sparbuchErstellenTest() throws KontoNichtExistiertException {
        long nummer = b.sparbuchErstellen(kunde1);
        List<Long> konten = b.getAlleKontonummern();

        assertTrue(konten.contains(nummer));
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
            // leer
        }
    }

    /**
     * Testen die Geldabhebung eines Kontos
     */
    @Test
    public void geldAbhebenTest() throws KontoNichtExistiertException {
        long nummer = b.girokontoErstellen(kunde1);

        double kontostand = b.getKontostand(nummer);
        double betrag = 20;

        // max Abhebung = 20EUR.
        boolean erfolgreich = b.geldAbheben(nummer, betrag);
        assertTrue(erfolgreich);

        double kontostandAktuell = b.getKontostand(nummer);

        assertEquals(kontostandAktuell, kontostand - 20);

        erfolgreich = b.geldAbheben(nummer, 50); // Groesser als Dispo
        assertFalse(erfolgreich);
        assertEquals(kontostandAktuell, b.getKontostand(nummer));
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
            // leer
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
            // leer
        }
    }

    /**
     * Testen das Löschen eines Kontos.
     */
    @Test
    public void kontoLoeschenTest() {
        long nummer = b.girokontoErstellen(kunde1);
        boolean erfolgreich = b.kontoLoeschen(nummer);

       assertTrue(erfolgreich);

       List<Long> konten = b.getAlleKontonummern();
       assertFalse(konten.contains(nummer));
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
            // leer
        }
    }

    /**
     * Testen der erfolgreichen Überweisung
     */
    @Test
    public void ueberweisungTest() throws KontoNichtExistiertException {
        long absender = b.girokontoErstellen(kunde1); // überweisungsfähig
        long empfaenger = b.girokontoErstellen(new Kunde()); // überweisungsunfähig

        double betrag = 20;
        double absenderKontostand = b.getKontostand(absender);
        double empfaengerKontostand = b.getKontostand(empfaenger);

        boolean erfolgreich = b.geldUeberweisen(absender, empfaenger, betrag, "Hello, World!");
        assertTrue(erfolgreich);

        double absenderKontostandAktuell = b.getKontostand(absender);
        double empfaengerKontostandAktuell = b.getKontostand(empfaenger);

        assertEquals(absenderKontostandAktuell, absenderKontostand - betrag);
        assertEquals(empfaengerKontostandAktuell, empfaengerKontostand + betrag);

        // Testen die Überweisung, wenn der Betrag groesser als Dispo ist.
        erfolgreich = b.geldUeberweisen(absender, empfaenger, 50, "Hello, World!");
        assertFalse(erfolgreich);

        assertEquals(absenderKontostandAktuell, b.getKontostand(absender));
        assertEquals(empfaengerKontostandAktuell, b.getKontostand(empfaenger));
    }

    /**
     * Testen die Überweisung mit fehlerhaften Parametern.
     */
    public void ueberweisungFehlerhaftTest() throws KontoNichtExistiertException {
        long absender = b.girokontoErstellen(kunde1); // überweisungsfähig
        long empfaenger = b.girokontoErstellen(new Kunde()); // überweisungsunfähig

        double absenderKontostand = b.getKontostand(absender);
        double empfaengerKontostand = b.getKontostand(empfaenger);

        boolean erfolgreich = b.geldUeberweisen(absender, empfaenger, -10, null);
        assertFalse(erfolgreich);

        assertEquals(absenderKontostand, b.getKontostand(absender));
        assertEquals(empfaengerKontostand, b.getKontostand(empfaenger));
    }

    /**
     * Testen die Überweisung von Überweisungsunfähigem
     */
    public void ueberweisungVonUnfaehigTest() throws KontoNichtExistiertException {
        long sparbuch = b.sparbuchErstellen(kunde1); // überweisungsunfähig
        long giro = b.girokontoErstellen(new Kunde()); // auch unfaehig.

        double absenderKontostand = b.getKontostand(sparbuch);
        double empfaengerKontostand = b.getKontostand(giro);

        boolean erfolgreich = b.geldUeberweisen(sparbuch, giro, 5, "");
        assertFalse(erfolgreich);

        assertEquals(absenderKontostand, b.getKontostand(sparbuch));
        assertEquals(empfaengerKontostand, b.getKontostand(giro));
    }

    /**
     * Testen die Überweisung nach Überweisungsunfähigem
     */
    public void ueberweisungNachFaehigTest() throws KontoNichtExistiertException {
        long giro = b.girokontoErstellen(kunde1); // überweisungsfähig
        long sparbuch = b.sparbuchErstellen(new Kunde()); // auch ueberweisungsfaehig

        double absenderKontostand = b.getKontostand(giro);
        double empfaengerKontostand = b.getKontostand(sparbuch);

        boolean erfolgreich = b.geldUeberweisen(giro, sparbuch, 5, "");
        assertFalse(erfolgreich);

        assertEquals(absenderKontostand, b.getKontostand(giro));
        assertEquals(empfaengerKontostand, b.getKontostand(sparbuch));
    }

    /**
     * Testen die Überweisung von Überweisungsunfähigem nach Überweisungsfähigem
     */
    public void ueberweisungVonUnfaehigNachUnfaehigTest() throws KontoNichtExistiertException {
        long sparbuch1 = b.sparbuchErstellen(kunde1); // überweisungsfähig
        long sparbuch2 = b.sparbuchErstellen(new Kunde()); // überweisungsunfähig

        double absenderKontostand = b.getKontostand(sparbuch1);
        double empfaengerKontostand = b.getKontostand(sparbuch2);

        boolean erfolgreich = b.geldUeberweisen(sparbuch1, sparbuch2, 5, "");
        assertFalse(erfolgreich);

        assertEquals(absenderKontostand, b.getKontostand(sparbuch1));
        assertEquals(empfaengerKontostand, b.getKontostand(sparbuch2));
    }
}
